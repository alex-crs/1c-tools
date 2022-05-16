package controllers.PlatformEditors;

import controllers.PlatformEditorController;
import entities.PlatformParams.Templates;
import entities.WindowControllers;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import service.HotKeys;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class TemplateEditController extends WindowControllers implements Initializable {
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

    @FXML
    AnchorPane templateWindow;

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
        initKeyListeners();
    }

    private void initKeyListeners() {
        HotKeys.closeListener(templateWindow, stage);
        HotKeys.closeListener(templateField, stage);
        HotKeys.enterListener(templateField, stage, this);
    }

    @Override
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
