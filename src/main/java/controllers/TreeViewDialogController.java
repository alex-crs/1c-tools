package controllers;

import entities.Const;
import entities.configStructure.VirtualTree;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import settings.BaseConfig;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class TreeViewDialogController implements Initializable {
    private MainWindowController mainWindowController;
    private Stage stage;

    @FXML
    TreeView<VirtualTree> treeViewDialog;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        treeViewDialog.setRoot(BaseConfig.returnFolderStructure());
    }

    public TreeViewDialogController(MainWindowController mainWindowController, Stage stage) {
        this.mainWindowController = mainWindowController;
        this.stage = stage;
    }

    public void accept() {
        TreeItem<VirtualTree> choiceFolder = treeViewDialog.getSelectionModel().getSelectedItem();
        mainWindowController.configList_MainTab.getSelectionModel().getSelectedItems().forEach(new Consumer<TreeItem<VirtualTree>>() {
            @Override
            public void accept(TreeItem<VirtualTree> virtualTreeTreeItem) {
                BaseConfig.moveElement(virtualTreeTreeItem.getValue(),choiceFolder);
            }
        });
        mainWindowController.configList_MainTab.setRoot(BaseConfig.returnConfigStructure());
        stage.close();
    }

    public void cancel() {
        stage.close();
    }

}
