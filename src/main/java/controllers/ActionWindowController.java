package controllers;

import entities.Const;
import entities.configStructure.VirtualTree;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import settings.BaseConfig;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ActionWindowController implements Initializable {
    private final Const action;
    private final MainWindowController mainController;
    private final Stage stage;

    @FXML
    Button accept;

    @FXML
    Button cancel;

    @FXML
    Label message;

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
                break;
            case DELETE_ELEMENT:
            case DELETE_SQL_CONFIG:
                message.setText("Желаете удалить элемент(ы)?");
                break;
        }
    }

    public void accept() {
        switch (action) {
            case CHECK_UNSAVED_DATA:
                mainController.saveChanges();
                mainController.configList_MainTab.setRoot(BaseConfig.returnConfigStructure());
                mainController.userList_MainTab.getSelectionModel().clearSelection();
                mainController.userList_MainTab.getSelectionModel().select(mainController.getCurrentUser());
                break;
            case DELETE_ELEMENT:
                deleteElements();
                mainController.enableSaveButton();
                break;
            case DELETE_SQL_CONFIG:
                mainController.data_base.deleteConfig(mainController.configCollection
                        .getSelectionModel()
                        .getSelectedItem());
//                mainController.loadSQLConfigListByGroup();
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
        mainController.disableSaveButton();
        stage.close();
    }

}
