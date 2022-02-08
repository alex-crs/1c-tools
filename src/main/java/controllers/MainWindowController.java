package controllers;


import entities.Const;
import entities.OS;
import entities.User;
import entities.Windows;
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
import stages.ConfigEditStage;
import stages.AlertWindowStage;
import stages.GroupEditStage;
import stages.TreeViewDialogStage;
import sun.reflect.generics.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static entities.Const.*;
import static handlers.CacheCleaner.clearCacheByUser;

public class MainWindowController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(MainWindowController.class);
    public UserList user_list;
    private List<User> systemUserList;
    private Ignored_objects ignoredObjects;
    private OS operatingSystem;
    public DataBaseService data_base;
    StringBuilder currentUser;
    public static String SYSTEM_LIST = "system";
    public static String LOCAL_LIST = "local";
    public static String BD_LIST = "BD";
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
    Button addConfigButton;

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
    Button addUser_ConfigTab;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configList_MainTab.setShowRoot(false);
        configList_MainTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        addUser_ConfigTab.setFocusTraversable(false);

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

        currentUser = new StringBuilder();
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
        if (!currentUser.toString().equals(userName.getName())) {
            currentUser.delete(0, currentUser.length());
            currentUser.append(userName);
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
        BaseConfig.writeConfigToFile(userList_MainTab.getSelectionModel().getSelectedItem().getName(), operatingSystem);
        if (userList_MainTab.getItems().size() > 0) {
            clearCacheByUser(userList_MainTab.getSelectionModel().getSelectedItems(), ignoredObjects);
            calcCashSpace();
        }
    }

    //отслеживает список выделенных пользователей
    public void clickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1 && userList_MainTab.getSelectionModel().getSelectedItems().size() > 0) {
            calcCashSpace();
            fillBaseList(userList_MainTab.getSelectionModel().getSelectedItem());
        }
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

    private void updateList() {
        configList_MainTab.setRoot(BaseConfig.returnConfigStructure());
    }

    public void addToTree() {
        if (userList_MainTab.getSelectionModel().getSelectedItems().size() == 1) {
            TreeItem<VirtualTree> choiceElement = configList_MainTab.getSelectionModel().getSelectedItem();
            showEditConfigWindow(CREATE_TREE_ELEMENT, choiceElement);
        } else {
            alert("Необходимо выбрать одного пользователя.");
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

    //позволяет добавлять пользователя вручную
    public void addUserManually() {
        showGroupEditWindow(CREATE_USER);
    }

    public void deleteElementFromTree() {
        TreeItem<VirtualTree> choiceElement = configList_MainTab.getSelectionModel().getSelectedItem();
        if (choiceElement != null) {
            BaseConfig.deleteElement(choiceElement.getValue());
            updateList();
        }
    }

    public void alert(String message) {
        AlertWindowStage alert = new AlertWindowStage(message);
        alert.setResizable(false);
        alert.show();
    }
}
