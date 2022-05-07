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
import javafx.scene.input.*;
import org.apache.log4j.Logger;
import service.DataBaseService;
import settings.BaseConfig;
import settings.Ignored_objects;
import settings.UserList;
import stages.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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
    //----------------------------------

    //вкладка №3: редактирование конфигураций в хранилище
    @FXML
    TableView<Base> configCollection;

//    final ObservableList<Base> configCollectionList = FXCollections.observableArrayList();

    public TableViewElement tableElement;

    @FXML
    Button addNewSQLConfig;

    @FXML
    Button editSQLConfig;

    @FXML
    Button deleteSQLConfig;

    public boolean isUnSavedChanges() {
        return unSavedChanges;
    }

    public void setUnSavedChanges(boolean unSavedChanges) {
        this.unSavedChanges = unSavedChanges;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getPlatformConfigPath(){
        return operatingSystem.platformConfigPathConstructor(currentUser.toString());
    }
    public String getCeStartPath(){
        return operatingSystem.ceStartPathConstructor(currentUser.toString());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        SQLConfigListInit(configCollection);
//        configCollection.setItems(configCollectionList);
        tableElement = new TableViewElement(this, configCollection);
        configList_MainTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        addUser_ConfigTab.setFocusTraversable(false);
        disableSaveButton();

        //проверяем наличие необходимых файлов, если таковых нет - создаем
        checkExternalWorkFiles();

        //инициализируем базу данных и начинаем с ней работать
        data_base = new DataBaseService();

        //загружаем список пользователей из локального файла
        user_list = new UserList(data_base, BD_LIST, group_choice_box);

        //инициализируем слушатели
        initListeners();
        ignoredObjects = new Ignored_objects();
        ignoredObjects.saveExcludeList();
        userList_MainTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        userList_Local_ConfigTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        usersList_System_ConfigTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        operatingSystem = new Windows();

        //отключаем фокусировку на кнопках
        clearCacheButton.setFocusTraversable(false);
        addConfigButton.setFocusTraversable(false);
        editConfigButton.setFocusTraversable(false);
        deleteConfigButton.setFocusTraversable(false);
        saveChangesButton.setFocusTraversable(false);
        addNewSQLConfig.setFocusTraversable(false);
        editSQLConfig.setFocusTraversable(false);
        deleteSQLConfig.setFocusTraversable(false);

        currentUser = new User();
        currentUser.setName("");
        displayUserList(); //показываем список пользователей
    }

    //загрузка пользователей из системы
    public void loadUsersFromSystem() {
        systemUserList = user_list.getUserListFromSystem();
        displaySystemUserList();
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
        configEditStage.setResizable(false);
        configEditStage.show();
    }

    /*позволяет редактировать группы в выпадающем меню, также универсально можно использовать
     и для добавления пользователя вручную*/
    private void showGroupEditWindow(Const action) {
        GroupEditStage groupEditStage = new GroupEditStage(action, this);
        groupEditStage.setResizable(false);
        groupEditStage.show();
    }

    public void moveConfig() {
        TreeViewDialogStage treeViewDialogStage = new TreeViewDialogStage(this);
        treeViewDialogStage.setResizable(false);
        treeViewDialogStage.show();
    }

    public void showActionQuestion(Const action) {
        ActionWindowStage aw = new ActionWindowStage(action, this);
        aw.setResizable(false);
        aw.show();
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
        List<User> users = userList_Local_ConfigTab.getSelectionModel().getSelectedItems();
        user_list.deleteFromLocalList(users);
        for (User u : users) {
            data_base.deleteUserFromBase(u);
        }
        displayUserList();
    }

    //добавляет пользователя в базу из системы
    public void addToLocalList() {
        user_list.addUserToDataBase(usersList_System_ConfigTab.getSelectionModel().getSelectedItems(),
                group_choice_box.getSelectionModel().getSelectedItem());
        displayUserList();
    }

    //заполняет лист доступных пользователю баз 1С
    public void fillBaseList(User userName) {
        if (!currentUser.getName().equals(userName.getName())) {
            currentUser = userName;
            BaseConfig.clearTree();
            BaseConfig.readConfigParameter(userName.getName(), operatingSystem);
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
        if (mouseEvent.getClickCount() == 1 && userList_MainTab.getSelectionModel().getSelectedItems().size() > 0 && !isUnSavedChanges()) {
            calcCashSpace();
            fillBaseList(userList_MainTab.getSelectionModel().getSelectedItem());
        } else if (isUnSavedChanges() && userList_MainTab.getSelectionModel().getSelectedItem() != currentUser) {
            showActionQuestion(CHECK_UNSAVED_DATA);
        }
    }

    public void setConfigList_MainTabClickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            editElement();
        }
    }

    public void setConfigList_SQLTabClickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            editSQLElement();
        }
    }

    //сохранить конфигурацию в файл
    public void saveChanges() {
        BaseConfig.writeConfigToFile(currentUser.toString(), operatingSystem);
        disableSaveButton();
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
        clearCacheButton.setText("Очистить кэш: " + FileLengthCalculator
                .getOccupiedSpaceByUser(
                        operatingSystem
                                .cachePathConstructor(userList_MainTab
                                        .getSelectionModel()
                                        .getSelectedItem().getName())));
    }

    //Создаем недостающие файлы в случае отсутствия создаем
    private void checkExternalWorkFiles() {
        externalWorkFiles.stream().forEach(file -> {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
        if (userList_MainTab.getSelectionModel().getSelectedItem() != null) {
            AddConfigFromBaseStage addConfigFromBaseStage = new AddConfigFromBaseStage(this);
            addConfigFromBaseStage.show();
        } else {
            alert("Необходимо выбрать пользователя");
        }
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
        TreeItem<VirtualTree> choiceElement = configList_MainTab.getSelectionModel().getSelectedItem();
        if (choiceElement != null && !choiceElement.getValue().isFolder()) {
            if (data_base.addConfigToBase((Base) choiceElement.getValue(), group_choice_box.getValue()) > 0) {
                alert("Конфигурация добавлена в хранилище.");
            } else {
                alert("При добавлении базы возникла ошибка! (подробнее см. журнал)");
            }
        } else {
            alert("Выберете конфигурацию для добавления в хранилище.");
        }
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
        tableElement.loadSQLConfigListByGroup(group_choice_box);
    }

    public void deleteSQLElementFromBase() {
        if (configCollection.getSelectionModel().getSelectedItem() != null) {
            showActionQuestion(DELETE_SQL_CONFIG);
        }
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
        alert.setResizable(false);
        alert.show();
    }

    //показывает настройки платформы для текущего пользователя
    public void runPlatformEdit(){
        if (userList_MainTab.getSelectionModel().getSelectedItem()!=null){
            PlatformEditorStage platformEditorStage = new PlatformEditorStage(this);
            platformEditorStage.show();
        }else {
            alert("Необходимо выбрать пользователя");
        }
    }

}
