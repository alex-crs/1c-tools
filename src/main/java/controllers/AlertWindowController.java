package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static stages.AlertWindowStage.*;

public class AlertWindowController implements Initializable {

    @FXML
    Label viewMsg;

    @FXML
    Button closeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewMsg.setText(getMessage());
    }

    public void keyListen(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
            event.consume();
            close();
        }
    }

    public void close() {
        Stage stage = ((Stage) closeButton.getScene().getWindow());
        stage.close();
    }
}
