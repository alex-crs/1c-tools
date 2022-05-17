package stages;

import controllers.AboutWindowController;
import controllers.ConfigEditController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AboutWindowStage extends Stage {
    public AboutWindowStage() {
        Parent root = null;
        try {
            AboutWindowController aboutWindowController = new AboutWindowController(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AboutWindow.fxml"));
            loader.setController(aboutWindowController);
            root = loader.load();
            setTitle("О программе:");
            setResizable(false);
            initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 300, 200);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
