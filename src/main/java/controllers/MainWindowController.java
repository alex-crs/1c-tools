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
    private UserList userList;
    private Ignored_objects ignoredObjects;
    private OS operatingSystem;
    List<String> baseList;
    StringBuilder currentUser;
    private String userListSource;
    public static String SYSTEM_LIST = "system";
    public static String LOCAL_LIST = "local";
    private static final List<File> externalWorkFiles = Arrays.asList(
            new File("users.txt"),
            new File("ignore.txt"),
            new File("base.db"));

    @FXML
    ListView<String> userListPanel;

    @FXML
    ListView<String> currentUserBaseList;

    @FXML
    Label length;

    @FXML
    Button clearCashButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkExternalWorkFiles();
        userListSource = LOCAL_LIST;
        userList = new UserList(userListSource);
        ignoredObjects = new Ignored_objects();
        userListPanel.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        operatingSystem = new Windows();
        clearCashButton.setFocusTraversable(false);
        currentUser = new StringBuilder();
        displayUserList(); //показываем список пользователей
    }

    private void displayUserList() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (String u : userList.getUserList()) {
                    userListPanel.getItems().add(u);
                }
            }
        });
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
        if (userListPanel.getItems().size() > 0) {
            clearCacheByUser(userListPanel.getSelectionModel().getSelectedItems(), ignoredObjects);
            calcCashSpace();
        }
    }

    //отслеживает список выделенных пользователей
    public void clickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            calcCashSpace();
            fillBaseList(currentUserBaseList, userListPanel.getSelectionModel().getSelectedItem());
        }
    }

    private void calcCashSpace() {
        clearCashButton.setText("Очистить кэш: " + FileLengthCalculator
                .getOccupiedSpaceByUser(
                        operatingSystem
                                .cachePathConstructor(userListPanel
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

    public void showUsersListBuilder(){
        UserWindowStage userWindowStage = new UserWindowStage();
        userWindowStage.show();
    }


}
