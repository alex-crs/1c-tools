package controllers;

import entities.Const;
import entities.User;
import entities.WindowControllers;
import entities.configStructure.VirtualTree;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import service.HotKeys;
import settings.BaseConfig;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ActionWindowController extends WindowControllers implements Initializable {
    private final Const action;
    private final MainWindowController mainController;
    private final Stage stage;

    @FXML
    Button accept;

    @FXML
    Button cancel;

    @FXML
    Label message;

    @FXML
    AnchorPane actionWindow;

    public ActionWindowController(Const action, MainWindowController mainController, Stage stage) {
        this.action = action;
        this.mainController = mainController;
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        accept.setFocusTraversable(false);
        switch (action) {
            case CHECK_UNSAVED_DATA:
                message.setText("Имеются несохраненные данные, желаете сохранить?");
                accept.setText("Сохранить");
                cancel.setText("Закрыть без сохранения");
                break;
            case DELETE_ELEMENT:
            case DELETE_SQL_CONFIG:
                message.setText("Желаете удалить элемент(ы)?");
                break;
            case CLEAN_USER_LIST:
                message.setText("Очистить список пользователей?");
                break;
            case DELETE_USER:
                message.setText("Удалить выбранные элемент(ы)?");
                break;
        }
    }

    public void keyListen(KeyEvent event){
        if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            action();
            stage.close();
        }
        if (event.getCode() == KeyCode.ESCAPE) {
            event.consume();
            cancel();
        }
    }

    @Override
    public void action() {
        List<User> users;
        switch (action) {
            case CHECK_UNSAVED_DATA:
                mainController.saveChanges();
                mainController.configList_MainTab.setRoot(BaseConfig.returnConfigStructure());
                mainController.fillBaseList(mainController.userList_MainTab.getSelectionModel().getSelectedItem());
                break;
            case DELETE_ELEMENT:
                deleteElements();
                mainController.enableSaveButton();
                break;
            case DELETE_SQL_CONFIG:
                mainController.data_base.deleteConfig(mainController.configCollection
                        .getSelectionModel()
                        .getSelectedItem());
                mainController.tableElement.loadSQLConfigListByGroup(mainController.group_choice_box);
                break;
            case CLEAN_USER_LIST:
                users = mainController.userList_Local_ConfigTab.getItems();
                mainController.user_list.deleteFromLocalList(users);
                for (User u : users) {
                    mainController.data_base.deleteUserFromBase(u);
                }
                mainController.displayUserList();
                break;
            case DELETE_USER:
                users = mainController.userList_Local_ConfigTab.getSelectionModel().getSelectedItems();
                mainController.user_list.deleteFromLocalList(users);
                for (User u : users) {
                    mainController.data_base.deleteUserFromBase(u);
                }
                mainController.displayUserList();
                break;
        }
        stage.close();
    }

    private void deleteElements() {
        List<TreeItem<VirtualTree>> choiceElement = mainController.configList_MainTab
                .getSelectionModel()
                .getSelectedItems();
        if (choiceElement.size() > 0) {
            for (TreeItem<VirtualTree> e : choiceElement) {
                BaseConfig.deleteElement(e.getValue());
            }
        }
        mainController.updateList();
        mainController.enableSaveButton();
    }

    public void cancel() {
        if (action == Const.CHECK_UNSAVED_DATA && mainController.userList_MainTab.getItems().size() > 0) {
            mainController.fillBaseList(mainController.userList_MainTab.getSelectionModel().getSelectedItem());
        }
        mainController.disableSaveButton();
        stage.close();
    }

}
