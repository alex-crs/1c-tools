package stages;

import controllers.MainWindowController;
import controllers.TreeViewDialogController;
import entities.Const;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class TreeViewDialogStage extends Stage {


    public TreeViewDialogStage(MainWindowController mainController) {
        Parent root = null;
        try {
            TreeViewDialogController tvd = new TreeViewDialogController(mainController, this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TreeViewDialogWindow.fxml"));
            loader.setController(tvd);
            root = loader.load();
            setTitle("Перемещение элемента");
            Scene scene = new Scene(root, 400, 250);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
