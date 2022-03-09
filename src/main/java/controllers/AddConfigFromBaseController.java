package controllers;

import entities.TableViewElement;
import entities.configStructure.Base;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AddConfigFromBaseController implements Initializable {


    @FXML
    ComboBox<String> group_choice_box;

    @FXML
    TableView<Base> configList;
    TableViewElement tableViewElement;

//    final ObservableList<Base> configCollectionList = FXCollections.observableArrayList();

    MainWindowController mainWindowController;
    Stage stage;

    public AddConfigFromBaseController(MainWindowController mainWindowController, Stage stage) {
        this.mainWindowController = mainWindowController;
        this.stage = stage;

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableViewElement = new TableViewElement(mainWindowController, configList);
//        mainWindowController.SQLConfigListInit(configCollection);
//        configCollection.setItems(configCollectionList);
        group_choice_box.getItems().addAll(mainWindowController.data_base.getGroups());
        group_choice_box.setValue(mainWindowController.group_choice_box.getValue());
//        configCollectionList.addAll(mainWindowController.data_base.getBaseListByGroup(groups.getValue()));
        tableViewElement.loadSQLConfigListByGroup(group_choice_box);
    }

    public void show() {
        tableViewElement.loadSQLConfigListByGroup(group_choice_box);
    }

}
