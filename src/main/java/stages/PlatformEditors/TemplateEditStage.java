package stages.PlatformEditors;

import controllers.PlatformEditorController;
import controllers.PlatformEditors.TemplateEditController;
import entities.PlatformParams.Templates;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class TemplateEditStage extends Stage {
    public TemplateEditStage(PlatformEditorController platformController, Templates template, String operation) {
        Parent root = null;
        try {
            TemplateEditController templateEditController = new TemplateEditController(platformController,template, this, operation);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PlatformEditors/TemplateEdit.fxml"));
            loader.setController(templateEditController);
            root = loader.load();
            setTitle(operation);
            initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 400, 90);
            setResizable(false);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
