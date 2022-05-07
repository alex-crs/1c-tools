package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PlatformEditorController implements Initializable {

    private final MainWindowController mainController;
    private final ArrayList<String> cv8config; //1
    private final ArrayList<String> ceStart; //2
    private static final Logger LOGGER = Logger.getLogger(PlatformEditorController.class);

    @FXML
    CheckBox treeView;

    @FXML
    TextField numberOfLatestConfig;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        readAllParams(new File(mainController.getPlatformConfigPath()), cv8config, 1);
    }

    public PlatformEditorController(MainWindowController mainWindowController, Stage stage) {
        this.mainController = mainWindowController;
        cv8config = new ArrayList<>();
        ceStart = new ArrayList<>();
    }

    public void readAllParams(File file, ArrayList<String> paramsCollection, int config) {
        paramsCollection.clear();
        int stringCount = 0;
        StringBuilder string = new StringBuilder();
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
                while (reader.ready()) {
                    string.append(reader.readLine());

                    switch (config) {
                        case 1:
                            cv8configReader(string.toString());
                            break;
                        case 2:
                            ceStartConfigReader(string.toString());
                            break;
                    }

                    string.delete(0, string.length());
                    stringCount++;
                }
                LOGGER.info(String.format("Прочитано [%s] строк(а) из файла %s", stringCount, file.getPath()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void cv8configReader(String string) {
        if (string.contains("AutoSortIBs") && string.charAt(5) == '1') {
            treeView.setSelected(true);
        }
        if (string.contains("ShowIBsAsTree")) {
            numberOfLatestConfig.setText(string.substring(5, 6));
        }
        cv8config.add(string);

    }

    private void ceStartConfigReader(String string) {
        ceStart.add(string);
    }

}
