package stages;

import controllers.ActionWindowController;
import controllers.MainWindowController;
import controllers.PlatformEditorController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PlatformEditorStage extends Stage {
    public PlatformEditorStage(MainWindowController mainWindowController) {
        Parent root = null;
        try {
            PlatformEditorController platformEditorController = new PlatformEditorController(
                    mainWindowController, this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PlatformEditor.fxml"));
            loader.setController(platformEditorController);
            root = loader.load();
            setTitle(String.format("Настройки 1C: [%s]", mainWindowController.getCurrentUser()));
            Scene scene = new Scene(root, 600, 750);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
