package controllers;

import entitys.OS;
import entitys.Windows;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static handlers.CacheCleaner.clearCacheByUser;
import static settings.BaseConfig.readBase;

public class MainWindowController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(MainWindowController.class);
    private UserList usersFromSystem;
    private Ignored_objects ignoredObjects;
    private OS operatingSystem;
    List<String> baseList;
    StringBuilder currentUser;

    @FXML
    ListView<String> userList;

    @FXML
    ListView<String> currentUserBaseList;

    @FXML
    Label length;

    @FXML
    Button clearCashButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usersFromSystem = new UserList();
        ignoredObjects = new Ignored_objects();
        userList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        operatingSystem = new Windows();
        clearCashButton.setFocusTraversable(false);
        currentUser = new StringBuilder();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (String u : usersFromSystem.getUserList()) {
                    userList.getItems().add(u);
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
        if (userList.getItems().size() > 0) {
            clearCacheByUser(userList.getSelectionModel().getSelectedItems(), ignoredObjects);
            calcCashSpace();
        }
    }

    //отслеживает список выделенных пользователей
    public void clickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            calcCashSpace();
            fillBaseList(currentUserBaseList, userList.getSelectionModel().getSelectedItem());
        }
    }

    private void calcCashSpace() {
        clearCashButton.setText("Очистить кэш: " + FileLengthCalculator
                .getOccupiedSpaceByUser(
                        operatingSystem
                                .cachePathConstructor(userList
                                        .getSelectionModel()
                                        .getSelectedItem())));
    }


}
