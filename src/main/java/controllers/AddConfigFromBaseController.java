package controllers;

import entities.TableViewElement;
import entities.configStructure.Base;
import entities.configStructure.VirtualTree;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import settings.BaseConfig;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static entities.Const.CREATE_TREE_ELEMENT;

public class AddConfigFromBaseController implements Initializable {


    @FXML
    ComboBox<String> group_choice_box;

    @FXML
    TableView<Base> configList;

    TableViewElement tableViewElement;
    MainWindowController mainWindowController;
    Stage stage;

    public AddConfigFromBaseController(MainWindowController mainWindowController, Stage stage) {
        this.mainWindowController = mainWindowController;
        this.stage = stage;

    }

    public void initListeners() {
        group_choice_box.setOnAction(event -> {
            tableViewElement.loadSQLConfigListByGroup(group_choice_box);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableViewElement = new TableViewElement(mainWindowController, configList);
        group_choice_box.getItems().addAll(mainWindowController.data_base.getGroups());
        group_choice_box.setValue(mainWindowController.group_choice_box.getValue());
        tableViewElement.loadSQLConfigListByGroup(group_choice_box);
        configList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        initListeners();
    }

    public void addToConfigList_MainTab() {
        List<Base> element = configList.getSelectionModel().getSelectedItems();
//        element.setFolder(false);
        TreeItem<VirtualTree> choiceElement = mainWindowController.configList_MainTab.getSelectionModel().getSelectedItem();
        if (choiceElement == null) {
            choiceElement = mainWindowController.configList_MainTab.getRoot();
        }
        for (Base l : element) {
            l.setFolder(false);
            int answer = MainWindowController.editConfigControllerManager(CREATE_TREE_ELEMENT, choiceElement, l);
            if (answer > 0) {
                mainWindowController.configList_MainTab.setRoot(BaseConfig.returnConfigStructure());
                close();
            } else {
                mainWindowController.alert(String.format("[%s] уже присутствует в древе", l.getElementName()));
            }
        }
    }

    public void close() {
        this.stage.close();
    }

}
