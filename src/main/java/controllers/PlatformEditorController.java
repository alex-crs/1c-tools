package controllers;

import entities.PlatformParams.DefaultVersionObject;
import entities.PlatformParams.SharedBase;
import entities.PlatformParams.Templates;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import org.apache.log4j.Logger;
import stages.PlatformEditors.DefaultVersionEditStage;
import stages.PlatformEditors.SharedBaseEditStage;
import stages.PlatformEditors.TemplateEditStage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;

@Getter
public class PlatformEditorController implements Initializable {

    private final MainWindowController mainController;
    private static final Logger LOGGER = Logger.getLogger(PlatformEditorController.class);

    //массивы с параметрами для 1cv8strt.pfl (параметр 1)
    private final ArrayList<String> cv8config;

    @FXML
    CheckBox showIBsAsTree;

    @FXML
    CheckBox autoSortIBs;

    @FXML
    TextField lRInfoBaseIDListSize;

    @FXML
    CheckBox showRecentIBs;

    @FXML
    CheckBox showStartEDTButton;

    //параметры для файла 1CEStart.cfg (параметр 2)
    String commonCfgLocation;

    @Getter
    ArrayList<DefaultVersionObject> defaultVersion;

    @Getter
    ArrayList<Templates> configurationTemplatesLocation;

    @Getter
    ArrayList<SharedBase> sharedBaseList;

    @FXML
    CheckBox useHWLicenses; //1 - true, 0 - false

    @FXML
    CheckBox appAutoInstallLastVersion; //1 - true, 0 - false

    @FXML
    ListView<Templates> templatesListView;

    @FXML
    ListView<SharedBase> sharedBaseListView;

    @FXML
    ListView<DefaultVersionObject> defaultVersionListView;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        readFileParams(new File(mainController.getPlatformConfigPath()), 1);
        readFileParams(new File(mainController.getCeStartPath()), 2);

        //заполняем список шаблонов
        fillTemplatesList();

        //заполняем список интернет сервисов
        fillSharedBaseList();

        //заполняем список версий по умолчанию
        fillDefaultVersionList();

        cv8init();
    }

    public PlatformEditorController(MainWindowController mainWindowController, Stage stage) {
        this.mainController = mainWindowController;
        cv8config = new ArrayList<>();
        configurationTemplatesLocation = new ArrayList<>();
        sharedBaseList = new ArrayList<>();
        defaultVersion = new ArrayList<>();
    }

    public void readFileParams(File file, int config) {
        int stringCount = 0;
        StringBuilder string = new StringBuilder();
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis,
                         config == 2 ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_8))) {
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
        cv8config.add(string);
    }

    //пока не понятно
    private void cv8init() {
        lRInfoBaseIDListSize.setText(cv8config.get(2).substring(5, 6));
        showIBsAsTree.setSelected(cv8config.get(3).charAt(5) == '1');
        autoSortIBs.setSelected(cv8config.get(4).charAt(5) == '1');
        showRecentIBs.setSelected(cv8config.get(5).charAt(5) == '1');
        showStartEDTButton.setSelected(cv8config.get(7).charAt(5) == '1');
    }

    private void ceStartConfigReader(String string) {
        String[] paramArray = string.split("=", 2);
        switch (paramArray[0]) {
            case "CommonCfgLocation":
                commonCfgLocation = paramArray[1];
                break;
            case "DefaultVersion":
                defaultVersion.add(new DefaultVersionObject(paramArray[1]));
                break;
            case "InternetService":
                SharedBase sb = new SharedBase(paramArray[1]);
                sb.setIService(true);
                sharedBaseList.add(sb);
                break;
            case "CommonInfoBases":
                sharedBaseList.add(new SharedBase(paramArray[1]));
                break;
            case "ConfigurationTemplatesLocation":
                configurationTemplatesLocation.add(new Templates(paramArray[1]));
                break;
            case "UseHWLicenses":
                useHWLicenses.setSelected(Integer.parseInt(paramArray[1]) == 1);
                break;
            case "AppAutoInstallLastVersion":
                appAutoInstallLastVersion.setSelected(Integer.parseInt(paramArray[1]) == 1);
                break;
        }
    }

    public void fillTemplatesList() {
        Platform.runLater(() -> {
            templatesListView.getItems().clear();
            for (Templates u : configurationTemplatesLocation) {
                templatesListView.getItems().add(u);
            }
        });
    }

    public void fillSharedBaseList() {
        Platform.runLater(() -> {
            sharedBaseListView.getItems().clear();
            for (SharedBase u : sharedBaseList) {
                sharedBaseListView.getItems().add(u);
            }
        });
    }

    public void fillDefaultVersionList() {
        Platform.runLater(() -> {
            defaultVersionListView.getItems().clear();
            for (DefaultVersionObject u : defaultVersion) {
                defaultVersionListView.getItems().add(u);
            }
        });
    }

    //операции со списком шаблонов
    public void addTemplate() {
        TemplateEditStage templateEditStage = new TemplateEditStage(this, null, "Добавить");
        templateEditStage.showAndWait();
    }

    public void moveUpTemplateFromList() {
        Templates selectedElement = templatesListView.getSelectionModel().getSelectedItem();
        int currentElementPosition = configurationTemplatesLocation.indexOf(selectedElement);
        int upElementPosition = 0;
        if (selectedElement != null && currentElementPosition != 0) {
            upElementPosition = currentElementPosition - 1;
            moveElement(selectedElement, currentElementPosition, upElementPosition);
        }
    }

    public void moveDownTemplateFromList() {
        Templates selectedElement = templatesListView.getSelectionModel().getSelectedItem();
        int currentElementPosition = configurationTemplatesLocation.indexOf(selectedElement);
        int downElementPosition = 0;
        if (selectedElement != null && currentElementPosition != configurationTemplatesLocation.size() - 1) {
            downElementPosition = currentElementPosition + 1;
            moveElement(selectedElement, currentElementPosition, downElementPosition);
        }
    }

    private void moveElement(Templates selectedElement, int currentElementPosition, int downElementPosition) {
        Templates upElement = configurationTemplatesLocation.get(downElementPosition);
        configurationTemplatesLocation.set(downElementPosition, selectedElement);
        configurationTemplatesLocation.set(currentElementPosition, upElement);
        fillTemplatesList();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                templatesListView.scrollTo(selectedElement);
                templatesListView.getSelectionModel().select(selectedElement);
            }
        });
    }

    public void deleteTemplate() {
        Templates element = templatesListView.getSelectionModel().getSelectedItem();
        int previousElement = configurationTemplatesLocation.indexOf(element) - 1;
        if (element != null) {
            configurationTemplatesLocation.remove(element);
            fillTemplatesList();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    templatesListView.scrollTo(previousElement);
                    templatesListView.getSelectionModel().select(previousElement);
                }
            });
        } else {
            mainController.alert("Для удаления необходимо выбрать элемент");
        }
    }

    public void editTemplate() {
        Templates element = templatesListView.getSelectionModel().getSelectedItem();
        if (element != null) {
            TemplateEditStage templateEditStage = new TemplateEditStage(this, element, "Изменить");
            templateEditStage.showAndWait();
        } else {
            mainController.alert("Необходимо выбрать элемент");
        }
    }

    //операции со списком интернет сервисов
    public void addService() {
        SharedBaseEditStage sharedBaseEditStage = new SharedBaseEditStage(this,
                null,
                "Добавить");
        sharedBaseEditStage.showAndWait();
    }

    public void deleteService() {
        SharedBase element = sharedBaseListView.getSelectionModel().getSelectedItem();
        int previousElement = sharedBaseList.indexOf(element) - 1;
        if (element != null) {
            sharedBaseList.remove(element);
            fillSharedBaseList();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    sharedBaseListView.scrollTo(previousElement);
                    sharedBaseListView.getSelectionModel().select(previousElement);
                }
            });
        } else {
            mainController.alert("Для удаления необходимо выбрать элемент");
        }
    }

    public void editService() {
        SharedBase element = sharedBaseListView.getSelectionModel().getSelectedItem();
        if (element != null) {
            SharedBaseEditStage sharedBaseEditStage = new SharedBaseEditStage(this,
                    element, "Изменить");
            sharedBaseEditStage.showAndWait();
        } else {
            mainController.alert("Для изменения необходимо выбрать элемент");
        }
    }

    //операции со списком версий по умолчанию
    public void addDefaultVersion() {
        DefaultVersionEditStage defaultStage = new DefaultVersionEditStage(this,
                null,
                "Добавить");
        defaultStage.showAndWait();
    }

    public void deleteDefaultVersion() {
        DefaultVersionObject element = defaultVersionListView.getSelectionModel().getSelectedItem();
        int previousElement = defaultVersion.indexOf(element) - 1;
        if (element != null) {
            defaultVersion.remove(element);
            fillDefaultVersionList();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    defaultVersionListView.scrollTo(previousElement);
                    defaultVersionListView.getSelectionModel().select(previousElement);
                }
            });
        } else {
            mainController.alert("Для удаления необходимо выбрать элемент");
        }
    }

    public void editDefaultVersion() {
        DefaultVersionObject element = defaultVersionListView.getSelectionModel().getSelectedItem();
        if (element != null) {
            DefaultVersionEditStage defaultStage = new DefaultVersionEditStage(this,
                    element,
                    "Изменить");
            defaultStage.showAndWait();
        } else {
            mainController.alert("Для редактирования необходимо выбрать элемент");
        }
    }

    public void copyDefaultVersion() {
        DefaultVersionObject element = defaultVersionListView.getSelectionModel().getSelectedItem();
        if (element != null) {
            DefaultVersionEditStage defaultStage = new DefaultVersionEditStage(this,
                    element,
                    "Скопировать");
            defaultStage.showAndWait();
        } else {
            mainController.alert("Для копирования необходимо выбрать элемент");
        }
    }

}
