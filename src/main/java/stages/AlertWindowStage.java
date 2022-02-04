package stages;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class AlertWindowStage extends Stage {

    public static String message;

    public static String getMessage() {
        return message;
    }

    public AlertWindowStage(String info) {
        message = info;
        Parent root = null;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/AlertWindow.fxml")));
            setTitle("Внимание!");
            Scene scene = new Scene(root, 400, 70);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
