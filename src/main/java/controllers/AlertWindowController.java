package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    public void close(){
        Stage stage = ((Stage) closeButton.getScene().getWindow());
        stage.close();
    }
}
