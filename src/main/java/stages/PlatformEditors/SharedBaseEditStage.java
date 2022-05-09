package stages.PlatformEditors;

import controllers.PlatformEditorController;
import controllers.PlatformEditors.SharedBaseEditController;
import entities.PlatformParams.SharedBase;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SharedBaseEditStage extends Stage {
    public SharedBaseEditStage(PlatformEditorController platformController, SharedBase sharedBase, String operation) {
        Parent root = null;
        try {
            SharedBaseEditController sharedBaseEditController = new SharedBaseEditController(platformController,
                    sharedBase, this, operation);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PlatformEditors/SharedBaseEdit.fxml"));
            loader.setController(sharedBaseEditController);
            root = loader.load();
            setTitle(operation);
            Scene scene = new Scene(root, 400, 90);
            setResizable(false);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
