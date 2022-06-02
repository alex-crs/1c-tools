package controllers;

import entities.TableViewElement;
import entities.WindowControllers;
import entities.configStructure.Base;
import entities.configStructure.VirtualTree;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import service.HotKeys;
import settings.BaseConfig;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static entities.Const.CREATE_TREE_ELEMENT;

public class AddConfigFromBaseController extends WindowControllers implements Initializable {


    @FXML
    ComboBox<String> group_choice_box;

    @FXML
    TableView<Base> configList;

    @FXML
    AnchorPane addConfigFromBaseWindow;

    @FXML
    Button actionButton;

    TableViewElement tableViewElement;
    MainWindowController mainWindowController;
    Stage stage;

    public AddConfigFromBaseController(MainWindowController mainWindowController, Stage stage) {
        this.mainWindowController = mainWindowController;
        this.stage = stage;

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        tableViewElement = new TableViewElement(mainWindowController, configList);
        group_choice_box.getItems().addAll(mainWindowController.data_base.getGroups());
        group_choice_box.setValue(mainWindowController.group_choice_box.getValue());

        Platform.runLater(() -> tableViewElement.loadSQLConfigListByGroup(group_choice_box));

        configList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        initListeners();
    }

    public void keyListen(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            action();
            stage.close();
        }
        if (event.getCode() == KeyCode.ESCAPE) {
            event.consume();
            stage.close();
        }
    }

    public void initListeners() {
        group_choice_box.setOnAction(event -> {
            tableViewElement.loadSQLConfigListByGroup(group_choice_box);
        });
    }

    public void configListMouseListener(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            action();
        }
    }

    @Override
    public void action() {
        List<Base> element = configList.getSelectionModel().getSelectedItems();
        TreeItem<VirtualTree> choiceElement = mainWindowController.configList_MainTab.getSelectionModel().getSelectedItem();
        if (choiceElement == null) {
            choiceElement = mainWindowController.configList_MainTab.getRoot();
        }
        for (Base l : element) {
            l.setFolder(false);
            int answer = MainWindowController.editConfigControllerManager(CREATE_TREE_ELEMENT, choiceElement, l);
            if (answer > 0) {
                mainWindowController.configList_MainTab.setRoot(BaseConfig.returnConfigStructure());
                stage.close();
            } else {
                mainWindowController.alert(String.format("[%s] уже присутствует в древе", l.getElementName()));
            }
        }
    }
}
