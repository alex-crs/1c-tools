package stages;

import controllers.ConfigEditController;
import controllers.MainWindowController;
import entities.Const;
import entities.configStructure.VirtualTree;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class ConfigEditStage extends Stage {
    public ConfigEditStage(TreeItem<VirtualTree> choiceElement, Const action, MainWindowController mainController) {
        Parent root = null;
        try {
            ConfigEditController configEditController = new ConfigEditController(choiceElement, action, mainController, this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConfigEditWindow.fxml"));
            loader.setController(configEditController);
            root = loader.load();
            setTitle(action.getTitle());
            setResizable(false);
            initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 560, 70);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
