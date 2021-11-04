package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UserWindowStage extends Stage {

    public UserWindowStage() {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/UserWindow.fxml"));
            setTitle("Панель управления пользователями");
            Scene scene = new Scene(root, 450, 450);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
