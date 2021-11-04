package controllers;

import entities.OS;
import entities.Windows;
import handlers.FileLengthCalculator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import org.apache.log4j.Logger;
import settings.BaseConfig;
import settings.Ignored_objects;
import settings.UserList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static handlers.CacheCleaner.clearCacheByUser;

public class MainWindowController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(MainWindowController.class);
    private UserList localUserList;
    private UserList systemUserList;
    private Ignored_objects ignoredObjects;
    private OS operatingSystem;
    List<String> baseList;
    StringBuilder currentUser;
    public static String SYSTEM_LIST = "system";
    public static String LOCAL_LIST = "local";
    private static final List<File> externalWorkFiles = Arrays.asList(
            new File("users.txt"),
            new File("ignore.txt"),
            new File("base.db"));

    @FXML
    ListView<String> userList_MainTab;

    @FXML
    ListView<String> userList_Local_ConfigTab;

    @FXML
    ListView<String> usersList_System_ConfigTab;

    @FXML
    ListView<String> configList_MainTab;

    @FXML
    Label length;

    @FXML
    Button clearCashButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkExternalWorkFiles();
        localUserList = new UserList(LOCAL_LIST);
        ignoredObjects = new Ignored_objects();
        userList_MainTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        userList_Local_ConfigTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        usersList_System_ConfigTab.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        operatingSystem = new Windows();
        clearCashButton.setFocusTraversable(false);
        currentUser = new StringBuilder();
        displayUserList(); //показываем список пользователей
    }

    public void loadUsersFromSystem(){
        systemUserList = new UserList(SYSTEM_LIST);
        displaySystemUserList();
    }

    //на вкладке настроек оторбражает список системных пользователей
    //!!!!!не забыть добавить отдельный поток для запуска операции и возможность прерывания потока
    private void displaySystemUserList(){
        usersList_System_ConfigTab.getItems().clear();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (String u : systemUserList.getUserList()) {
                    usersList_System_ConfigTab.getItems().add(u);
                }
            }
        });
    }

    //показывает локальный список пользователей, если пользователей нет,
    //то панели недоступны для выбора и редактирования
    private void displayUserList() {
        userList_Local_ConfigTab.getItems().clear();
        userList_MainTab.getItems().clear();
        userList_MainTab.setDisable(false);
        userList_Local_ConfigTab.setDisable(false);
        Platform.runLater(() -> {
            for (String u : localUserList.getUserList()) {
                    userList_MainTab.getItems().add(u);
                    userList_Local_ConfigTab.getItems().add(u);
                if ("Пользователи не найдены".equals(u)) {
                    userList_MainTab.setDisable(true);
                    userList_Local_ConfigTab.setDisable(true);
                }
            }
        });
    }

    //удаляет пользователя из локальной базы пользователей
    public void deleteFromLocalUserList(){
        localUserList.deleteFromLocalList(userList_Local_ConfigTab.getSelectionModel().getSelectedItems());
        displayUserList();
    }

    //сохраняет текущий список пользователей
    public void saveLocalUserList(){
        localUserList.saveUserList();
    }

    //добавляет пользователя в базу из системы
    public void addToLocalList(){
        localUserList.addUserToLocalList(usersList_System_ConfigTab.getSelectionModel().getSelectedItems());
        displayUserList();
    }

    //заполняет лист доступных пользователю баз 1С
    public void fillBaseList(ListView<String> listView, String userName) {
        if (!currentUser.toString().equals(userName)) {
            currentUser.delete(0, currentUser.length());
            currentUser.append(userName);
            BaseConfig.clearBaseList();
            BaseConfig.readBase(userName, operatingSystem);
            baseList = BaseConfig.getBaseList();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    listView.getItems().clear();
                    for (String b : baseList) {
                        listView.getItems().add(b);
                    }
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
    public void clickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            calcCashSpace();
            fillBaseList(configList_MainTab, userList_MainTab.getSelectionModel().getSelectedItem());
        }
    }

    private void calcCashSpace() {
        clearCashButton.setText("Очистить кэш: " + FileLengthCalculator
                .getOccupiedSpaceByUser(
                        operatingSystem
                                .cachePathConstructor(userList_MainTab
                                        .getSelectionModel()
                                        .getSelectedItem())));
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

}
