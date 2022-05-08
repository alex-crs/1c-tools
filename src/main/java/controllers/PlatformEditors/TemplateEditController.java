package controllers.PlatformEditors;

import controllers.PlatformEditorController;
import entities.PlatformParams.Templates;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class TemplateEditController implements Initializable {
    PlatformEditorController platformController;
    Templates template;
    Stage stage;
    String operation;

    @FXML
    TextField templateField;

    @FXML
    Button acceptButton;

    @FXML
    Button choiceButton;

    public TemplateEditController(PlatformEditorController platformController,
                                  Templates template,
                                  Stage stage,
                                  String operation) {
        this.platformController = platformController;
        this.template = template;
        this.stage = stage;
        this.operation = operation;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (operation.equals("Изменить")) {
            templateField.setText(template.toString());
            acceptButton.setText("Изменить");
        }
    }

    public void action() {
        switch (operation) {
            case "Добавить":
                if (templateField.getText().length() != 0) {
                    platformController.getConfigurationTemplatesLocation().add(new Templates(templateField.getText()));
                    platformController.fillTemplatesList();
                }
                stage.close();
                break;
            case "Изменить":
                if (templateField.getText().length() != 0) {
                    template.setConfigurationTemplatesLocation(templateField.getText());
                    platformController.fillTemplatesList();
                }
                stage.close();
                break;
        }
    }

    public void openFileChoiceDialog() {
        DirectoryChooser dialog = new DirectoryChooser();
        File file;
        dialog.setTitle("Выберете место расположения шаблонов");
        file = dialog.showDialog(stage);
        if (file != null) {
            templateField.setText(file.getPath());
        }
    }


}
