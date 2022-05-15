package controllers.PlatformEditors;

import controllers.PlatformEditorController;
import entities.PlatformParams.DefaultVersionObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

public class DefaultVersionEditController implements Initializable {

    PlatformEditorController platformController;
    DefaultVersionObject defaultObject;
    Stage stage;
    String operation;

    //разрядность запускаемого клиента 1С
    HashMap<String, String> bitDepthValues;

    @FXML
    ChoiceBox<String> bitDepth;

    @FXML
    TextField target;

    @FXML
    TextField use;

    @FXML
    Label targetLabel;

    @FXML
    Label useLabel;

    public DefaultVersionEditController(PlatformEditorController platformController,
                                        DefaultVersionObject defaultObject,
                                        Stage stage,
                                        String operation) {
        this.platformController = platformController;
        this.defaultObject = defaultObject;
        this.stage = stage;
        this.operation = operation;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initChoiceBox();
        if (operation.equals("Изменить") || operation.equals("Скопировать")) {
            target.setText(defaultObject.getTargetVersion());
            use.setText(defaultObject.getUsedVersion());
            bitDepth.setValue(bitDepthValues.get(defaultObject.getBitDepth()));
        }
    }

    private void initChoiceBox() {
        bitDepthValues = new HashMap<>();
        bitDepthValues.put("x86", "32 (x86)");
        bitDepthValues.put("x86_prt", "Приоритет 32 (x86)");
        bitDepthValues.put("x86_64", "64 (x86_64)");
        bitDepthValues.put("x86_64_prt", "Приоритет 64 (x86_64)");
        bitDepth.getItems().addAll(new ArrayList<>(bitDepthValues.values()));
    }

    public void action() {
        String targetParam = target.getText();
        String useParam = use.getText();

        //проверка параметров на валидность
        long validTargetParam = versionFormatInspector(targetParam);
        if (validTargetParam < 0) {
            targetLabel.setTextFill(Color.web("red"));
            targetLabel.setText("Неверный ввод!");
        } else {
            targetLabel.setTextFill(Color.web("black"));
            targetLabel.setText("Для версии:");
        }
        long validUseParam = versionFormatInspector(useParam);
        if (validUseParam < 2) {
            useLabel.setTextFill(Color.web("red"));
            useLabel.setText("Неверный ввод!");
        } else {
            useLabel.setTextFill(Color.web("black"));
            useLabel.setText("Использовать версию:");
        }
        //------------------------------------

        String bitDepthParam = (String) bitDepthValues
                .entrySet()
                .stream()
                .filter(stringStringEntry -> stringStringEntry
                        .getValue().equals(bitDepth.getSelectionModel()
                                .getSelectedItem()))
                .map((Function<Map.Entry<String, String>, Object>) Map.Entry::getKey)
                .findFirst().orElse("");

        if (use.getText().length() > 0 && validTargetParam >= 1 && validUseParam > 0) {
            String params = (targetParam != null && targetParam.length() > 0 ? targetParam + "-" : "") + useParam + ";" + bitDepthParam;
            switch (operation) {
                case "Добавить":
                case "Скопировать":
                    DefaultVersionObject dvo = new DefaultVersionObject(params);
                    platformController.getDefaultVersion().add(dvo);
                    platformController.fillDefaultVersionList();
                    break;
                case "Изменить":
                    defaultObject.setTargetVersion(targetParam);
                    defaultObject.setUsedVersion(useParam);
                    defaultObject.setBitDepth(bitDepthParam);
                    platformController.fillDefaultVersionList();
                    break;
            }
            stage.close();
        }
    }

    //проверяет введено ли число в поле "Версия"
    private long versionFormatInspector(String param) {
        String string;
        long result = -1;
        try {
            string = param.replaceAll("\\.", "");
            result = Long.parseLong(string.length() == 0 ? "1" : string);
        } catch (NumberFormatException e) {
            return -1;
        } catch (NullPointerException e) {
            return 1;
        }
        return result;
    }

}
