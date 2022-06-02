package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutWindowController implements Initializable {
    private Stage stage;

    @FXML
    Label version;

    public AboutWindowController(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        version.setText("1С-tools ver " + MainWindowController.getVersion());
    }

    public void close(){
        stage.close();
    }

}
