package controllers;

import entities.*;
import entities.configStructure.Base;
import entities.configStructure.VirtualTree;
import entities.configStructure.Folder;
import handlers.FileLengthCalculator;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import service.BDGenerator;
import service.DataBaseService;
import settings.BaseConfig;
import settings.Ignored_objects;
import settings.UserList;
import stages.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

import static entities.Const.*;
import static handlers.CacheCleaner.clearCacheByUser;

public class MainWindowController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(MainWindowController.class);
    public UserList user_list;
    private List<User> systemUserList;
    private Ignored_objects ignoredObjects;
    private OS operatingSystem;
    public DataBaseService data_base;
    private User currentUser;
    public static String SYSTEM_LIST = "system";
    public static String LOCAL_LIST = "local";
    public static String BD_LIST = "BD";
    private boolean unSavedChanges = false;
    private static final List<File> externalWorkFiles = Arrays.asList(
            new File("users.txt"),
            new File("ignore.txt"),
            new File("base.db"));
    FileChooser dialog;
    File file;
    private final static StringBuilder version = new StringBuilder();

    //вкладка №1: управление базами 1С
    @FXML
    ListView<User> userList_MainTab;

    TreeItem<VirtualTree> root = new TreeItem<>(new Folder("root"));

    @FXML
    TreeView<VirtualTree> configList_MainTab = new TreeView<>(root);

    @FXML
    ComboBox<String> group_choice_box;

    @FXML
    SplitMenuButton addConfigButton;

    @FXML
    Button saveChangesButton;

    @FXML
    Button deleteConfigButton;

    @FXML
    Button editConfigButton;

    @FXML
    Button clearCacheButton;
    //----------------------------------

    //вкладка №2: редактирование пользователей
    @FXML
    ListView<User> userList_Local_ConfigTab;

    @FXML
    ListView<User> usersList_System_ConfigTab;

    @FXML
    TabPane tabControl;

    @FXML
    Button addUser_ConfigTab;

    @FXML
    Label loadingLabel;

    @FXML
    ProgressIndicator progressIndicator;

    @FXML
    Label userLoadingInfo;

    //----------------------------------

    //вкладка №3: редактирование конфигураций в хранилище
    @FXML
    TableView<Base> configCollection;

    public TableViewElement tableElement;

    @FXML
    Button addNewSQLConfig;

    @FXML
    Button editSQLConfig;

    @FXML
    Button deleteSQLConfig;

    final KeyCombination createElement = new KeyCodeCombination(KeyCode.W,
            KeyCombination.CONTROL_DOWN);
    final KeyCombination addFromSQLBase = new KeyCodeCombination(KeyCode.F,
            KeyCombination.CONTROL_DOWN);

    public OS getOperatingSystem() {
        return operatingSystem;
    }

    public boolean isUnSavedChanges() {
        return unSavedChanges;
    }

    public void setUnSavedChanges(boolean unSavedChanges) {
        this.unSavedChanges = unSavedChanges;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCV8ConfigPath() {
        return operatingSystem.getPlatformConfigDirectory(currentUser.toString());
    }

    public String getCeStartPath() {
        return operatingSystem.ceStartPathConstructor(currentUser.toString());
    }

    public String getLocationConfigPath() {
        return operatingSystem.getLocationConfig(currentUser.getName());
    }

    public static StringBuilder getVersion() {
        return version;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //ВЕРСИЯ ПРОГРАММЫ
        version.append("0.98 beta");

        group_choice_box.setVisible(false);
        tableElement = new TableViewElement(this, configCollection);
        configList_MainTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        addUser_ConfigTab.setFocusTraversable(false);
        disableSaveButton();

        //инициализация базы данных
        new Thread(() -> {
            userList_MainTab.setDisable(true);
            //проверка базы данных (если она пустая, то происходит заполнение)
            BDGenerator.connect();
            progressIndicator.setProgress(0.1);
            int versionBD = BDGenerator.checkBD();
            progressIndicator.setProgress(0.2);
            if (versionBD < 0) {
                BDGenerator.createBD();
                versionBD = BDGenerator.checkBD();
                progressIndicator.setProgress(0.4);
            }
            progressIndicator.setProgress(0.4);
            LOGGER.info(String.format("Версия базы данных: %s", versionBD));
            BDGenerator.disconnect();

            //инициализируем базу данных и начинаем с ней работать
            if (versionBD > 0) {
                data_base = new DataBaseService();
                progressIndicator.setProgress(0.6);
            } else {
                LOGGER.info("Проблема с базой данных");
                System.exit(0);
            }

            //загружаем список пользователей из локального файла
            user_list = new UserList(data_base, BD_LIST, group_choice_box);
            progressIndicator.setProgress(0.8);

            //инициализируем слушатели
            initListeners();
            ignoredObjects = new Ignored_objects();
            userList_MainTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            userList_Local_ConfigTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            usersList_System_ConfigTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            progressIndicator.setProgress(0.9);
            operatingSystem = new Windows();

            currentUser = new User();
            currentUser.setName("");
            userList_MainTab.setDisable(false);
            loadingLabel.setVisible(false);
            loadingLabel.setDisable(true);
            progressIndicator.setProgress(1);
            progressIndicator.setDisable(true);
            progressIndicator.setVisible(false);
            group_choice_box.setVisible(true);
            displayUserList(); //показываем список пользователей
        }).start();

        contextMenuConfigListMainTabInit();
        contextMenuConfigListSQLInit();
    }

    //горячие клавиши для user_list
    public void userListKeyListen(KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.UP) {
            event.consume();
            userListClickEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
                    0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true,
                    true, true, true, true, true, true, null));
        }
        if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            runPlatformEdit();
        }
    }

    //горячие клавиши для таблицы SQL конфигураций
    public void configListSQLKeyListen(KeyEvent event) {
        if (createElement.match(event)) {
            event.consume();
            addNewSQLConfig();
        }
        if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            editSQLElement();
        }
        if (event.getCode() == KeyCode.DELETE) {
            event.consume();
            deleteSQLElementFromBase();
        }
    }

    //горячие клавиши для списка пользователей во вкладке редактирование списка пользователей
    public void userListConfigKeyListen(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            event.consume();
            deleteFromLocalUserList();
        }
    }

    //контекстное меню в древе конфигурации
    private void contextMenuConfigListMainTabInit() {
        ContextMenu configTreeContextMenu = new ContextMenu();

        MenuItem openInExplorer = new MenuItem("Открыть в Проводнике");
        openInExplorer.setOnAction(event -> openInExplorer());

        MenuItem addElement = new MenuItem("Создать SHIFT+W");
        addElement.setOnAction(event -> addToTree());

        MenuItem moveElement = new MenuItem("Переместить");
        moveElement.setOnAction(event -> moveConfig());

        MenuItem editConfig = new MenuItem("Редактировать");
        editConfig.setOnAction(event -> editElement());

        MenuItem addToBase = new MenuItem("Добавить в хранилище");
        addToBase.setOnAction(event -> saveConfigToDataBase());

        MenuItem addFromBase = new MenuItem("Добавить из хранилища SHIFT+F");
        addFromBase.setOnAction(event -> addConfigFromBase());

        MenuItem deleteConfig = new MenuItem("Удалить");
        deleteConfig.setOnAction(event -> deleteElementFromTree());

        configTreeContextMenu.getItems().addAll(openInExplorer,
                addElement,
                editConfig,
                moveElement,
                addToBase,
                addFromBase,
                deleteConfig);
        configList_MainTab.setContextMenu(configTreeContextMenu);
    }

    private void openInExplorer() {
        List<TreeItem<VirtualTree>> choiceElement = configList_MainTab.getSelectionModel().getSelectedItems();
        Base base = null;
        try {
            if (choiceElement != null && choiceElement.size() != 0) {
                base = (Base) choiceElement.get(0).getValue();
                if (choiceElement.size() == 1 && base.getConnect().contains("File")) {
                    try {
                        Desktop.getDesktop().open(new File(base.getConnect().replace("File=", "")
                                .replace(";", "")
                                .replace("\"", "")));
                    } catch (IllegalArgumentException | IOException e) {
                        LOGGER.info(String.format("Выбранная директория недоступна [%s]", base.getConnect()));
                        alert("Директория недоступна или не существует");
                    }
                } else if (choiceElement.size() > 1) {
                    LOGGER.info("Выбрано более одной конфигурации");
                    alert("Необходимо выбрать одну конфигурацию");
                } else {
                    LOGGER.info(String.format("Выбранная конфигурация имеет недопустимый путь [%s]", base.getConnect()));
                    alert("Не выбрана файловая база");
                }
            }
        } catch (ClassCastException e) {
            LOGGER.info("Попытка открыть папку в проводнике");
            alert("Папку невозможно просмотреть в проводнике");
        }
    }

    //контекстное меню в древе конфигурации
    private void contextMenuConfigListSQLInit() {
        ContextMenu configTreeSQLContextMenu = new ContextMenu();

        MenuItem addElement = new MenuItem("Создать SHIFT+W");
        addElement.setOnAction(event -> addNewSQLConfig());

        MenuItem editConfig = new MenuItem("Редактировать");
        editConfig.setOnAction(event -> editSQLElement());

        MenuItem cloneConfig = new MenuItem("Клонировать");
        cloneConfig.setOnAction(event -> cloneSQLConfig());

        MenuItem deleteConfig = new MenuItem("Удалить");
        deleteConfig.setOnAction(event -> deleteSQLElementFromBase());

        configTreeSQLContextMenu.getItems().addAll(addElement,
                editConfig,
                cloneConfig,
                deleteConfig);
        configCollection.setContextMenu(configTreeSQLContextMenu);
    }

    //загрузка пользователей из системы
    public void loadUsersFromSystem() {
        systemUserList = user_list.getUserListFromSystem();
        if (systemUserList.size() > 0) {
            userLoadingInfo.setDisable(true);
            userLoadingInfo.setVisible(false);
            usersList_System_ConfigTab.setDisable(false);
            displaySystemUserList();
        }
    }

    //на вкладке настроек отображает список системных пользователей
    //!!!!!не забыть добавить отдельный поток для запуска операции и возможность прерывания потока
    public void displaySystemUserList() {
        Platform.runLater(() -> {
            usersList_System_ConfigTab.getItems().clear();
            for (User u : systemUserList) {
                usersList_System_ConfigTab.getItems().add(u);
            }
        });
    }

    private void initListeners() {
        //запускаем слушатель для групп
        group_choice_box.setOnAction(event -> {
            String query = group_choice_box.getSelectionModel().getSelectedItem();
            try {
                Const constant = getConst(query);
                switch (Objects.requireNonNull(constant)) {
                    case DEFAULT_GROUP:
                        user_list.loadUserListByGroup(DEFAULT_GROUP.getTitle());
                        displayUserList();
                        tableElement.loadSQLConfigListByGroup(group_choice_box);
                        break;
                    case RENAME_GROUP:
                        showGroupEditWindow(RENAME_GROUP);
                        break;
                    case DELETE_GROUP:
                        showGroupEditWindow(DELETE_GROUP);
                        break;
                    case CREATE_GROUP:
                        showGroupEditWindow(CREATE_GROUP);
                        break;
                    case GROUPS_DELIMITER:
                        Platform.runLater(() -> group_choice_box.setValue(user_list.getCurrentGroup()));
                        break;
                    case EMPTY:
                        break;
                }
            } catch (IllegalArgumentException e) {
                user_list.loadUserListByGroup(query);
                displayUserList();
                tableElement.loadSQLConfigListByGroup(group_choice_box);
            }
        });

        //запускаем слушатель горячих клавиш
        configList_MainTab.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                //очищает выделение
                if (event.getCode() == KeyCode.ESCAPE) {
                    event.consume();
                    configList_MainTab.getSelectionModel().clearSelection();
                }
                if (event.getCode() == KeyCode.DELETE) {
                    event.consume();
                    deleteElementFromTree();
                }
                if (event.getCode() == KeyCode.ENTER) {
                    event.consume();
                    editElement();
                }
                if (createElement.match(event)) {
                    event.consume();
                    addToTree();
                }
                if (addFromSQLBase.match(event)) {
                    event.consume();
                    addConfigFromBase();
                }
            }
        });
    }

    public static int editConfigControllerManager(Const action, TreeItem<VirtualTree> choiceElement, VirtualTree element) {
        switch (action) {
            case CREATE_TREE_ELEMENT:
                return BaseConfig.addElement(choiceElement, element);
        }
        return 0;
    }

    private void showEditConfigWindow(Const action, TreeItem<VirtualTree> choiceElement) {
        ConfigEditStage configEditStage = new ConfigEditStage(choiceElement, action, this);
        configEditStage.showAndWait();
    }

    /*позволяет редактировать группы в выпадающем меню, также универсально можно использовать
     и для добавления пользователя вручную*/
    private void showGroupEditWindow(Const action) {
        GroupEditStage groupEditStage = new GroupEditStage(action, this);
        groupEditStage.show();
    }

    public void moveConfig() {
        TreeViewDialogStage treeViewDialogStage = new TreeViewDialogStage(this);
        treeViewDialogStage.showAndWait();
    }

    public void showActionQuestion(Const action) {
        ActionWindowStage aw = new ActionWindowStage(action, this);
        aw.showAndWait();
    }


    //показывает локальный список пользователей, если пользователей нет,
    //то панели недоступны для выбора и редактирования
    public void displayUserList() {
        userList_Local_ConfigTab.getItems().clear();
        userList_MainTab.getItems().clear();
        userList_MainTab.setDisable(false);
        userList_Local_ConfigTab.setDisable(false);
        Platform.runLater(() -> {
            for (User u : user_list.getUserList()) {
                userList_MainTab.getItems().add(u);
                userList_Local_ConfigTab.getItems().add(u);
            }
        });
    }

    //удаляет пользователя из локальной базы пользователей
    public void deleteFromLocalUserList() {
        if (userList_Local_ConfigTab.getSelectionModel().getSelectedItems().size() > 0) {
            showActionQuestion(DELETE_USER);
        } else {
            alert("Необходимо выбрать элемент(ы)");
        }
    }

    //очистка списка пользователей
    public void cleanUserList() {
        if (userList_Local_ConfigTab.getSelectionModel().getSelectedItems().size() > 0) {
            showActionQuestion(CLEAN_USER_LIST);
        } else {
            alert("Необходимо выбрать элемент(ы)");
        }
    }

    //добавляет пользователя в базу из системы
    public void addToLocalList() {
        user_list.addUserToDataBase(usersList_System_ConfigTab.getSelectionModel().getSelectedItems(),
                group_choice_box.getSelectionModel().getSelectedItem());
        usersList_System_ConfigTab.getSelectionModel().clearSelection();
        displayUserList();
    }

    //заполняет лист доступных пользователю баз 1С
    public void fillBaseList(User userName) {
        if (!currentUser.getName().equals(userName.getName())) {
            currentUser = userName;
            BaseConfig.clearTree();
            File file = new File(operatingSystem.basePathConstructor(userName.getName()));
            BaseConfig.readConfigParameter(userName.getName(), file);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    root = BaseConfig.returnConfigStructure();
                    configList_MainTab.setRoot(root);
                }
            });
        }
    }

    //очищает кэш пользователя
    public void clearCache() {
        if (userList_MainTab.getItems().size() > 0) {
            clearCacheByUser(userList_MainTab.getSelectionModel().getSelectedItems(), ignoredObjects);
            calcCashSpace();
        }
    }

    //отслеживает список выделенных пользователей
    public void userListClickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1
                && userList_MainTab.getSelectionModel().getSelectedItems().size() > 0
                && !isUnSavedChanges()) {
            calcCashSpace();
            fillBaseList(userList_MainTab.getSelectionModel().getSelectedItem());
        } else if (isUnSavedChanges() && userList_MainTab.getSelectionModel().getSelectedItem() != currentUser) {
            showActionQuestion(CHECK_UNSAVED_DATA);
        }
        if (mouseEvent.getClickCount() == 2) {
            runPlatformEdit();
        }
    }

    public void ConfigList_MainTabClickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            editElement();
        }
    }

    public void ConfigList_SQLTabClickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            editSQLElement();
        }
    }

    //сохранить конфигурацию в файл
    public void saveChanges() {
        BaseConfig.saveConfigTo1CFile(currentUser.toString(), operatingSystem);
        disableSaveButton();
    }

    public void saveConfigToFile() {
        file = null;
        dialog = new FileChooser();
        dialog.setTitle("Выберете место хранения файла конфигурации?");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Файл конфигурации (*.v8i)",
                "*.v8i");
        dialog.getExtensionFilters().add(filter);
        dialog.setInitialDirectory(new File((new File("").getAbsolutePath())));
        file = dialog.showSaveDialog((saveChangesButton.getParent()).getScene().getWindow());
        if (file != null) {
            BaseConfig.saveConfToChoiceFile(file);
        }
    }

    public void loadConfigFromFile() {
        file = null;
        dialog = new FileChooser();
        dialog.setTitle("Выберете место расположения файла конфигурации");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Файл конфигурации (*.v8i)",
                "*.v8i");
        dialog.getExtensionFilters().add(filter);
        dialog.setInitialDirectory(new File((new File("").getAbsolutePath())));
        file = dialog.showOpenDialog((saveChangesButton.getParent()).getScene().getWindow());
        if (file != null) {
            BaseConfig.readConfigParameter(currentUser.getName(), file);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    root = BaseConfig.returnConfigStructure();
                    configList_MainTab.setRoot(root);
                }
            });
            enableSaveButton();
        }
    }

    //отключить кнопку сохранения конфигураций
    public void disableSaveButton() {
        setUnSavedChanges(false);
        saveChangesButton.setDisable(true);
    }

    //включить кнопку сохранения конфигураций
    public void enableSaveButton() {
        setUnSavedChanges(true);
        saveChangesButton.setDisable(false);
    }

    private void calcCashSpace() {
        StringBuilder user = new StringBuilder().append(userList_MainTab
                .getSelectionModel()
                .getSelectedItem().getName());
        File cv8 = new File(operatingSystem.cv8cachePathConstructor(user.toString()));
        File cv82 = new File(operatingSystem.cv82cachePathConstructor(user.toString()));
        File cv83 = new File(operatingSystem.cv83cachePathConstructor(user.toString()));
        float sum = calcIfExist(cv8)
                + calcIfExist(cv82)
                + calcIfExist(cv83);
        Platform.runLater(() -> clearCacheButton.setText("Очистить кэш: " + FileLengthCalculator.spaceToString(sum)));
    }

    private float calcIfExist(File file) {
        return file.exists() ? FileLengthCalculator
                .calcFileLength(file.getPath() + File.separator) : 0;
    }

    public void updateList() {
        configList_MainTab.setRoot(BaseConfig.returnConfigStructure());
    }

    public void addToTree() {
        if (userList_MainTab.getSelectionModel().getSelectedItems().size() == 1) {
            TreeItem<VirtualTree> choiceElement = configList_MainTab.getSelectionModel().getSelectedItem();
            showEditConfigWindow(CREATE_TREE_ELEMENT, choiceElement);
        } else {
            alert("Необходимо выбрать пользователя");
        }
    }

    public void addConfigFromBase() {
        Platform.runLater(() -> {
            if (userList_MainTab.getSelectionModel().getSelectedItem() != null) {
                AddConfigFromBaseStage addConfigFromBaseStage = new AddConfigFromBaseStage(MainWindowController.this);
                addConfigFromBaseStage.showAndWait();
            } else {
                alert("Необходимо выбрать пользователя");
            }
        });
    }

    public void editElement() {
        if (configList_MainTab.getSelectionModel().getSelectedItem() != null) {
            TreeItem<VirtualTree> choiceElement = configList_MainTab.getSelectionModel().getSelectedItem();
            showEditConfigWindow(choiceElement.getValue().isFolder() ? EDIT_TREE_FOLDER : EDIT_TREE_CONFIG, choiceElement);
        } else {
            alert("Необходимо выбрать редактируемый элемент");
        }
    }

    public void saveConfigToDataBase() {
        Platform.runLater(() -> {
            List<TreeItem<VirtualTree>> choiceElement = configList_MainTab.getSelectionModel().getSelectedItems();
            if (choiceElement != null && choiceElement.size() > 0 && !choiceElement.get(0).getValue().isFolder() && choiceElement.size() == 1) { //поправить выскакивает ошибка NullPointer когда пустое окно
                if (data_base.addConfigToBase((Base) choiceElement.get(0).getValue(), group_choice_box.getValue()) > 0) {
                    alert("Конфигурация добавлена в хранилище.");
                } else {
                    alert("При добавлении базы возникла ошибка! (подробнее см. журнал)");
                }
            } else {
                alert("Для добавления необходимо выбрать одну конфигурацию");
            }
        });
    }

    public void addNewSQLConfig() {
        showEditConfigWindow(CREATE_SQL_CONFIG, null);
    }

    public void editSQLElement() {
        if (configCollection.getSelectionModel().getSelectedItem() != null) {
            TreeItem<VirtualTree> choiceElement = new TreeItem<>(configCollection.getSelectionModel().getSelectedItem());
            choiceElement.getValue().setFolder(false);
            showEditConfigWindow(EDIT_SQL_CONFIG, choiceElement);
        }
    }

    public void showConfigs() {
        new Thread(() -> {
            tableElement.loadSQLConfigListByGroup(group_choice_box);
        }).start();
    }

    public void deleteSQLElementFromBase() {
        Platform.runLater(() -> {
            if (configCollection.getSelectionModel().getSelectedItem() != null) {
                showActionQuestion(DELETE_SQL_CONFIG);
            }
        });

    }

    //позволяет добавлять пользователя вручную
    public void addUserManually() {
        showGroupEditWindow(CREATE_USER);
    }

    public void deleteElementFromTree() {
        showActionQuestion(DELETE_ELEMENT);
    }

    public void alert(String message) {
        AlertWindowStage alert = new AlertWindowStage(message);
        alert.showAndWait();
    }

    //показывает настройки платформы для текущего пользователя
    public void runPlatformEdit() {
        if (userList_MainTab.getSelectionModel().getSelectedItem() != null) {
            PlatformEditorStage platformEditorStage = new PlatformEditorStage(this);
            platformEditorStage.showAndWait();
        } else {
            alert("Необходимо выбрать пользователя");
        }
    }

    public void cloneSQLConfig() {
        Base base = configCollection.getSelectionModel().getSelectedItem();
        if (base != null) {
            Base cloneBase = base.clone();
            cloneBase.setId(0);
            cloneBase.setGroups(null);
            data_base.addConfigToBase(cloneBase, group_choice_box.getSelectionModel().getSelectedItem());
            tableElement.loadSQLConfigListByGroup(group_choice_box);
        }
    }

    public void close() {
        Stage stage = (Stage) group_choice_box.getScene().getWindow();
        stage.close();
    }

    public void about() {
        AboutWindowStage aboutWindowStage = new AboutWindowStage();
        aboutWindowStage.showAndWait();
    }

}
