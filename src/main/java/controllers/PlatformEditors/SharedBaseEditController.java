package controllers.PlatformEditors;

import controllers.PlatformEditorController;
import entities.PlatformParams.SharedBase;
import entities.WindowControllers;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.HotKeys;
import stages.ConfigEditStage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SharedBaseEditController extends WindowControllers implements Initializable {
    PlatformEditorController platformController;
    SharedBase sharedBase;
    Stage stage;
    String operation;

    @FXML
    TextField sharedBaseField;

    @FXML
    Button acceptButton;

    @FXML
    Button choiceButton;

    @FXML
    CheckBox ieService;

    @FXML
    Label infoLabel;

    @FXML
    AnchorPane sharedBaseWindow;

    public SharedBaseEditController(PlatformEditorController platformController,
                                    SharedBase sharedBase,
                                    Stage stage,
                                    String operation) {
        this.platformController = platformController;
        this.sharedBase = sharedBase;
        this.stage = stage;
        this.operation = operation;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (operation.equals("Изменить")) {
            ieService.setSelected(sharedBase.isIService());
            sharedBaseField.setText(sharedBase.getParamName());
            choiceButton.setDisable(sharedBase.isIService());
            acceptButton.setText("Изменить");
            infoLabel.setText("Адрес WEB-сервиса:");
        }
        initKeyListeners();
    }

    private void initKeyListeners() {
        //на ESCAPE
        HotKeys.closeListener(sharedBaseWindow, stage);
        HotKeys.closeListener(sharedBaseField, stage);
        HotKeys.closeListener(acceptButton, stage);

        //на ENTER
        HotKeys.enterListener(sharedBaseField, stage, this);
    }

    @Override
    public void action() {
        if (sharedBaseField.getText().length() > 0) {
            switch (operation) {
                case "Добавить":
                    SharedBase sb = new SharedBase(sharedBaseField.getText());
                    sb.setIService(ieService.isSelected());
                    platformController.getSharedBaseList().add(sb);
                    platformController.fillSharedBaseList();
                    break;
                case "Изменить":
                    sharedBase.setIService(ieService.isSelected());
                    sharedBase.setParamName(sharedBaseField.getText());
                    platformController.fillSharedBaseList();
                    break;
            }
        }
        stage.close();
    }

    public void checkBoxMouseListener(MouseEvent mouseEvent) {
        if (!ieService.isSelected()) {
            choiceButton.setDisable(false);
            sharedBaseField.setText("");
            infoLabel.setText("Расположение локально или в сети:");
        } else {
            choiceButton.setDisable(true);
            sharedBaseField.setText("https://");
            infoLabel.setText("Адрес WEB-сервиса:");
        }
    }

    public void openFileChoiceDialog() {
        FileChooser dialog = new FileChooser();
        File file;
        dialog.setTitle("Выберете список");
        FileChooser.ExtensionFilter filter = new FileChooser
                .ExtensionFilter("Общий список интернет-сервисов и информационных баз (*.v8i)", "*.v8i");
        dialog.getExtensionFilters().add(filter);
        file = dialog.showOpenDialog(stage);
        if (file != null) {
            sharedBaseField.setText(file.getPath());
        }
    }

}
