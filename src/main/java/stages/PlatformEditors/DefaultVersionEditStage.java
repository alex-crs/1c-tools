package stages.PlatformEditors;

import controllers.PlatformEditorController;
import controllers.PlatformEditors.DefaultVersionEditController;
import controllers.PlatformEditors.SharedBaseEditController;
import entities.PlatformParams.DefaultVersionObject;
import entities.PlatformParams.SharedBase;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DefaultVersionEditStage extends Stage {

    public DefaultVersionEditStage(PlatformEditorController platformController,
                                   DefaultVersionObject defaultObject,
                                   String operation) {
        Parent root = null;
        try {
            DefaultVersionEditController defaultVersionEditController = new DefaultVersionEditController(
                    platformController, defaultObject, this, operation);
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/fxml/PlatformEditors/DefaultVersionEdit.fxml"));
            loader.setController(defaultVersionEditController);
            root = loader.load();
            setTitle(operation);
            setResizable(false);
            Scene scene = new Scene(root, 300, 170);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
