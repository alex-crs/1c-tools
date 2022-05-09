package stages;

import controllers.ActionWindowController;
import controllers.MainWindowController;
import entities.Const;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ActionWindowStage extends Stage {
    public ActionWindowStage(Const action, MainWindowController mainWindowController) {
        Parent root = null;
        try {
            ActionWindowController groupEditController = new ActionWindowController(action,
                    mainWindowController, this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ActionWindow.fxml"));
            loader.setController(groupEditController);
            root = loader.load();
            setTitle(action.getTitle());
            setResizable(false);
            initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 400, 70);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
