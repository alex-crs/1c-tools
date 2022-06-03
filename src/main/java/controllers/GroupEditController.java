package controllers;

import entities.Const;
import entities.User;
import entities.configStructure.VirtualTree;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import service.DataBaseService;
import settings.UserList;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static entities.Const.DEFAULT_GROUP;
import static entities.Const.DELETE_GROUP;

public class GroupEditController implements Initializable {

    @FXML
    TextField name;

    @FXML
    Button apply;

    @FXML
    Button cancel;

    @FXML
    Label message;

    @FXML
    ComboBox<String> currentWindow_group_box;

    Const action;

    DataBaseService dataBase;

    Stage stage;

    UserList userList;

    ComboBox<String> mainWindow_group_choice_box;

    MainWindowController mainWindowController;

    VirtualTree element;

    List<String> groupList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        groupList = dataBase.getGroups();
        groupList.remove(DEFAULT_GROUP.getTitle());
        //инициализируем выводимое сообщение
        switch (action) {
            case CREATE_GROUP:
                message.setText("Введите имя новой группы:");
                apply.setDisable(true);
                currentWindow_group_box.setVisible(false);
                break;
            case RENAME_GROUP:
                initGroupBox();
                message.setText("Выберете группу для переименования:");
                name.setVisible(false);
                choiceListener();
                break;
            case DELETE_GROUP:
                initGroupBox();
                message.setText("Выберете группу для удаления:");
                name.setVisible(false);
                break;
            case CREATE_USER:
                apply.setDisable(true);
                message.setText("Введите имя пользователя:");
                currentWindow_group_box.setVisible(false);
                break;
        }
    }

    public void keyListen(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            action();
        }
        if (event.getCode() == KeyCode.ESCAPE) {
            event.consume();
            stage.close();
        }
    }

    //после выбора элемента из ComboBox этот элемент передается в TextField
    private void choiceListener() {
        currentWindow_group_box.setOnAction(event -> {
            currentWindow_group_box.setVisible(false);
            name.setVisible(true);
            name.setText(currentWindow_group_box.getSelectionModel().getSelectedItem());
            message.setText("Двойной клик по полю ввода вернет к выбору группы.");
        });
    }

    //после двойного клика TextField переключается обратно в ComboBox
    public void clickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 && action.equals(Const.RENAME_GROUP)) {
            currentWindow_group_box.setVisible(true);
            name.setVisible(false);
            message.setText("Выберете группу для переименования:");
        }
    }

    //если длина имени элемента равна 0, то все настройки отключаются
    public void nameNonNullInspector() {
        if (name.getText().length() == 0 && !action.equals(DELETE_GROUP)) {
            apply.setDisable(true);
        } else {
            apply.setDisable(false);
        }
    }

    public void action() {
        String value;
        switch (action) {
            case CREATE_GROUP:
                value = name.getText();
                createGroup(value);
                break;
            case RENAME_GROUP:
                String oldName = currentWindow_group_box.getSelectionModel().getSelectedItem();
                String newName = name.getText();
                dataBase.renameGroup(oldName, newName);
                userList.updateUserList(newName);
                stage.close();
                break;
            case DELETE_GROUP:
                value = currentWindow_group_box.getSelectionModel().getSelectedItem();
                dataBase.deleteGroup(value);
                userList.updateUserList(value);
                stage.close();
                break;
            case CREATE_USER:
                value = name.getText();
                User user = new User();
                user.setName(value);
                userList.addUserToDataBase(Collections.singletonList(user),
                        mainWindow_group_choice_box.getSelectionModel().getSelectedItem());
                mainWindowController.displayUserList();
                stage.close();
                break;
        }
    }

    private void createGroup(String value) {
        if (groupList.contains(value)) {
            mainWindowController.alert("Группа с таким именем уже существует!");
        } else {
            dataBase.addGroup(value);
            userList.updateUserList(value);
            stage.close();
        }
    }

    public void setAConst(Const aConst) {
        this.action = aConst;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setMainWindowController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
        dataBase = mainWindowController.data_base;
        userList = mainWindowController.user_list;
        mainWindow_group_choice_box = mainWindowController.group_choice_box;
    }

    public void initGroupBox() {
        currentWindow_group_box.getItems().clear();
        currentWindow_group_box.getItems().addAll(groupList);
    }

    public void cancel() {
        mainWindowController.user_list.returnToCurrentGroup();
        stage.close();
    }
}
