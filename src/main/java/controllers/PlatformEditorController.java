package controllers;

import entities.PlatformParams.DefaultVersionObject;
import entities.PlatformParams.SharedBase;
import entities.PlatformParams.Templates;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import org.apache.log4j.Logger;
import stages.PlatformEditors.DefaultVersionEditStage;
import stages.PlatformEditors.SharedBaseEditStage;
import stages.PlatformEditors.TemplateEditStage;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Getter
public class PlatformEditorController implements Initializable {

    private final MainWindowController mainController;
    private static final Logger LOGGER = Logger.getLogger(PlatformEditorController.class);
    private final Stage stage;

    private File cv8Start;
    private File ceStart;
    private File cv8FilePathLocation;

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

    @FXML
    Label lRInfoLabel;

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

    @FXML
    AnchorPane configWindow;

    StringBuilder cv8DirectoryLocation = new StringBuilder();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cv8FilePathLocation = new File(mainController.getLocationConfigPath());
        if (cv8FilePathLocation.exists()) {
            cv8DirectoryLocation.append(readLocationConfigParams(cv8FilePathLocation));
        }
        if (cv8DirectoryLocation.length() == 0) {
            cv8DirectoryLocation.append("1cv8");
        }
        cv8Start = new File(mainController.getCV8ConfigPath() + File.separator
                + cv8DirectoryLocation + File.separator + "1cv8strt.pfl");
        ceStart = new File(mainController.getCeStartPath());
        try {
            createPathsIfNotExist();
            if (!cv8Start.exists()) {
                cv8Start.createNewFile();
                initNewC8ConfigFile();
                saveParametersToFile(cv8Start, cv8config, 1);
            }
            if (!ceStart.exists()) {
                ceStart.createNewFile();
            }
        } catch (IOException e) {
            LOGGER.error("Отсутствует доступ к файлу");
        }

        readFileParams(cv8Start, 1);
        readFileParams(ceStart, 2);

        //заполняем список шаблонов
        fillTemplatesList();

        //заполняем список интернет сервисов
        fillSharedBaseList();

        //заполняем список версий по умолчанию
        fillDefaultVersionList();

        try {
            if (cv8config.size() < 6) {
                initNewC8ConfigFile();
                saveParametersToFile(cv8Start, cv8config, 1);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Отсутствует доступ: %s", cv8Start.getPath()));
            LOGGER.error(String.format("Отсутствует доступ: %s", ceStart.getPath()));
        }

        cv8ModelViewInit(cv8config);

        //установим фокус на панель шаблонов
        templatesListView.focusedProperty();
    }

    public PlatformEditorController(MainWindowController mainWindowController, Stage stage) {
        this.mainController = mainWindowController;
        this.stage = stage;
        cv8config = new ArrayList<>();
        configurationTemplatesLocation = new ArrayList<>();
        sharedBaseList = new ArrayList<>();
        defaultVersion = new ArrayList<>();
    }

    public void templateKeyListener(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            keyEvent.consume();
            deleteTemplate();
        }
        if (keyEvent.getCode() == KeyCode.ENTER) {
            keyEvent.consume();
            editTemplate();
        }
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            keyEvent.consume();
            stage.close();
        }
    }

    public void sharedBaseKeyListener(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            keyEvent.consume();
            deleteService();
        }
        if (keyEvent.getCode() == KeyCode.ENTER) {
            keyEvent.consume();
            editService();
        }
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            keyEvent.consume();
            stage.close();
        }
    }

    public void defaultVersionKeyListener(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            keyEvent.consume();
            deleteDefaultVersion();
        }
        if (keyEvent.getCode() == KeyCode.ENTER) {
            keyEvent.consume();
            editDefaultVersion();
        }
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            keyEvent.consume();
            stage.close();
        }
    }

    public void templateClickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            editTemplate();
        }
    }

    public void serviceClickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            editService();
        }
    }

    public void defaultVersionClickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            editDefaultVersion();
        }
    }

    /*
    В разных версиях 1С файл location.cfg в разных кодировках, в текущем методе мы пробуем читать файл
    в разных кодировках, если не данные и там и там отрицательные возвращаем строку нулевой длины.
    */
    private StringBuilder readLocationConfigParams(File file) {
        StringBuilder string = new StringBuilder();
        StringBuilder cv8ParamLocation = new StringBuilder();
        String[] param;
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis,
                         StandardCharsets.UTF_16LE))) {
                string.append(reader.readLine());
                param = string.toString().split("/");
                cv8ParamLocation.append(param[param.length - 1]);
                LOGGER.info(String.format("Прочитан файл %s", file.getPath()));
                LOGGER.info(String.format("Содержимое файла %s", cv8ParamLocation));
            } catch (Exception e) {
                LOGGER.error(String.format("Ошибка при чтении файла %s", cv8ParamLocation));
            }

            if (codePageChecker(cv8ParamLocation.toString())) {
                LOGGER.info("Файл прочитан успешно");
                return cv8ParamLocation;
            } else {
                string.delete(0, string.length());
                cv8ParamLocation.delete(0, cv8ParamLocation.length());
                LOGGER.info(String.format("Иная кодировка файла, повторное чтение %s", file.getPath()));
                try (FileInputStream fis = new FileInputStream(file);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(fis,
                             StandardCharsets.UTF_8))) {
                    string.append(reader.readLine());
                    param = string.toString().split("/");
                    cv8ParamLocation.append(param[param.length - 1]);
                    LOGGER.info(String.format("Прочитан файл %s", file.getPath()));
                    LOGGER.info(String.format("Содержимое файла %s", cv8ParamLocation));
                } catch (Exception e) {
                    LOGGER.error(String.format("Ошибка при чтении файла %s", cv8ParamLocation));
                }
            }
            if (codePageChecker(cv8ParamLocation.toString())) {
                LOGGER.info("Файл прочитан успешно");
                return cv8ParamLocation;
            } else {
                LOGGER.info(String.format("Не удалось корректно прочесть файл [%s]," +
                        " в файле не содержится ключевых строк. Файла содержит: [%s]", file.getPath(), cv8ParamLocation));
            }

        }
        //возвращаем строку нулевой длины
        return cv8ParamLocation.delete(0, cv8ParamLocation.length());
    }

    //проверяем строку на наличие ключевых элементов
    private boolean codePageChecker(String param) {
        return param.toLowerCase().contains("1c");
    }

    private void readFileParams(File file, int config) {
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

    public void action() {
        LinkedList<String> cache = new LinkedList<>();
        if (numberFormatInspector()) {
            updateC8ConfigList();
            build_1CeStartConfig(cache);
            saveParametersToFile(cv8Start, cv8config, 1);
            saveParametersToFile(ceStart, cache, 2);
            stage.close();
        }
    }

    private void createPathsIfNotExist() {
        File cEStartDir = new File(mainController
                .getOperatingSystem()
                .getCEStartDirectory(mainController.getCurrentUser().getName()));
        File cv8StartDir = new File(mainController
                .getOperatingSystem()
                .getPlatformConfigDirectory(mainController.getCurrentUser().getName()) + File.separator + cv8DirectoryLocation);
        File configLocation = new File(mainController
                .getOperatingSystem()
                .getPlatformConfigDirectory(mainController.getCurrentUser().getName()) + File.separator + "1cv8");
        try {
            if (!cEStartDir.exists()) {
                Files.createDirectories(Paths.get(String.valueOf(cEStartDir)));
            }
            if (!cv8StartDir.exists()) {
                Files.createDirectories(Paths.get(String.valueOf(cv8StartDir)));
            }
//            if (!configLocation.exists()) {
//                Files.createDirectories(Paths.get(String.valueOf(configLocation)));
//                saveLocationConfigParams(cv8FilePathLocation);
//            }
        } catch (IOException e) {
            LOGGER.error(String.format("Отсутствует доступ: %s, %s", cEStartDir.getPath(), cv8StartDir.getPath()));
        }
    }

    private void updateC8ConfigList() {
        StringBuilder param = new StringBuilder();
        for (int i = 0; i < cv8config.size(); i++) {
            if (cv8config.get(i).contains("LRInfoBaseIDListSize")) {
                param.append(cv8config.get(i + 1));
                param.replace(5, 6, lRInfoBaseIDListSize.getText());
                cv8config.set(i + 1, param.toString());
            }
            if (cv8config.get(i).contains("ShowIBsAsTree")) {
                param.append(cv8config.get(i + 1));
                param.replace(5, 6, showIBsAsTree.isSelected() ? "1" : "0");
                cv8config.set(i + 1, param.toString());
            }
            if (cv8config.get(i).contains("AutoSortIBs")) {
                param.append(cv8config.get(i + 1));
                param.replace(5, 6, autoSortIBs.isSelected() ? "1" : "0");
                cv8config.set(i + 1, param.toString());
            }
            if (cv8config.get(i).contains("ShowRecentIBs")) {
                param.append(cv8config.get(i + 1));
                param.replace(5, 6, showRecentIBs.isSelected() ? "1" : "0");
                cv8config.set(i + 1, param.toString());
            }
            if (cv8config.get(i).contains("ShowStartEDTButton")) {
                param.append(cv8config.get(i + 1));
                param.replace(5, 6, showStartEDTButton.isSelected() ? "1" : "0");
                cv8config.set(i + 1, param.toString());
                i = cv8config.size();
            }
            param.delete(0, param.length());
        }
    }

    private boolean numberFormatInspector() {
        String string = lRInfoBaseIDListSize.getText();
        int result = -1;
        try {
            result = Integer.parseInt(string);
            if (result < 0) {
                mainController.alert("Необходимо ввести целое число");
                lRInfoLabel.setTextFill(Color.web("red"));
            } else {
                lRInfoLabel.setTextFill(Color.web("black"));
            }
        } catch (NumberFormatException e) {
            mainController.alert("Необходимо ввести целое число");
            lRInfoLabel.setTextFill(Color.web("red"));
            return false;
        }
        return true;
    }

    private void build_1CeStartConfig(LinkedList<String> cache) {
        if (commonCfgLocation != null) {
            cache.add("CommonCfgLocation=" + commonCfgLocation);
        }
        for (DefaultVersionObject dob : defaultVersion) {
            cache.add(dob.returnParam());
        }
        for (SharedBase sb : sharedBaseList) {
            if (sb.isIService()) {
                cache.add(sb.returnParam());
            }
        }
        for (Templates t : configurationTemplatesLocation) {
            cache.add(t.returnParam());
        }
        for (SharedBase sb : sharedBaseList) {
            if (!sb.isIService()) {
                cache.add(sb.returnParam());
            }
        }
        cache.add("UseHWLicenses=" + (useHWLicenses.isSelected() ? "1" : "0"));
        cache.add("AppAutoInstallLastVersion=" + (appAutoInstallLastVersion.isSelected() ? "1" : "0"));
    }

    private LinkedList<String> build_1Cv8StartConfig() {
        LinkedList<String> cache = new LinkedList<>();
        cache.add("LRInfoBaseIDListSize=" + lRInfoBaseIDListSize.getText());
        cache.add("ShowIBsAsTree=" + (showIBsAsTree.isSelected() ? "1" : "0"));
        cache.add("AutoSortIBs=" + (autoSortIBs.isSelected() ? "1" : "0"));
        cache.add("ShowRecentIBs=" + (showRecentIBs.isSelected() ? "1" : "0"));
        cache.add("ShowStartEDTButton=" + (showStartEDTButton.isSelected() ? "1" : "0"));
        return cache;
    }

    private void loadParametersFromChosenFile(File file) {
        defaultVersion.clear();
        configurationTemplatesLocation.clear();
        sharedBaseList.clear();

        StringBuilder string = new StringBuilder();
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
                while (reader.ready()) {
                    string.append(reader.readLine());
                    ceStartConfigReader(string.toString());
                    string.delete(0, string.length());
                }
            } catch (FileNotFoundException e) {
                LOGGER.error(String.format("Файл не обнаружен: %s", file.getPath()));
            } catch (IOException e) {
                LOGGER.error("Проблема с доступом к файлу");
            }
        }

        fillTemplatesList();
        fillDefaultVersionList();
        fillSharedBaseList();
    }

    public void openLoadFileDialog() {
        FileChooser dialog = new FileChooser();
        File file;
        dialog.setTitle("Выберете место расположения шаблонов");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Параметры платформы", "*.pconf");
        dialog.getExtensionFilters().add(filter);
        dialog.setInitialDirectory(new File((new File("").getAbsolutePath())));
        file = dialog.showOpenDialog(stage);
        if (file != null) {
            loadParametersFromChosenFile(file);
        }
    }

    //если параметр 1, то пишем в UTF-8, если 2, то в UTF-16LE
    private void saveParametersToFile(File file, List<String> cache, int param) {
        try (FileOutputStream fis = new FileOutputStream(file);
             BufferedWriter writer = new BufferedWriter((new OutputStreamWriter(fis,
                     (param == 1 ? StandardCharsets.UTF_8 : StandardCharsets.UTF_16LE))))) {
            if (param == 2) {
                writer.write(65279);
            }
            for (String l : cache) {
                writer.write(l);
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Файл не обнаружен: %s", file.getPath()));
        }
    }

    public void openSaveFileDialog() {
        if (numberFormatInspector()) {
            LinkedList<String> cache = new LinkedList<>();
            FileChooser dialog = new FileChooser();
            File file;
            dialog.setTitle("Выберете место расположения настроек платформы");
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Параметры платформы", "*.pconf");
            dialog.getExtensionFilters().add(filter);
            dialog.setInitialDirectory(new File((new File("").getAbsolutePath())));
            file = dialog.showSaveDialog(stage);
            if (file != null) {
                build_1CeStartConfig(cache);
                cache.addAll(build_1Cv8StartConfig());
                saveParametersToFile(file, cache, 1);
            }
        }
    }

    private void cv8configReader(String string) {
        cv8config.add(string);
    }


    private void cv8ModelViewInit(ArrayList<String> config) {
        for (int i = 0; i < config.size(); i++) {
            if (config.get(i).contains("LRInfoBaseIDListSize")) {
                lRInfoBaseIDListSize.setText(config.get(i + 1).substring(5, 6));
            }
            if (config.get(i).contains("ShowIBsAsTree")) {
                showIBsAsTree.setSelected(config.get(i + 1).charAt(5) == '1');
            }
            if (config.get(i).contains("AutoSortIBs")) {
                autoSortIBs.setSelected(config.get(i + 1).charAt(5) == '1');
            }
            if (config.get(i).contains("ShowRecentIBs")) {
                showRecentIBs.setSelected(config.get(i + 1).charAt(5) == '1');
            }
            if (config.get(i).contains("ShowStartEDTButton")) {
                showStartEDTButton.setSelected(config.get(i + 1).charAt(5) == '1');
                break;
            }
        }
    }


    private void ceStartConfigReader(String string) {
        if (string.charAt(0) == 65279) { //это ZERO WIDTH NO-BREAK SPACE по сути это char -
            // символ кодировки UTF-8 без BOM (Byte order Mark) знак порядка байтов
            ; //удалим его и добавим при записи
            string = string.substring(1);
        }
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
            case "LRInfoBaseIDListSize":
                lRInfoBaseIDListSize.setText(paramArray[1]);
                break;
            case "ShowIBsAsTree":
                showIBsAsTree.setSelected(Integer.parseInt(paramArray[1]) == 1);
                break;
            case "AutoSortIBs":
                autoSortIBs.setSelected(Integer.parseInt(paramArray[1]) == 1);
                break;
            case "ShowRecentIBs":
                showRecentIBs.setSelected(Integer.parseInt(paramArray[1]) == 1);
                break;
            case "ShowStartEDTButton":
                showStartEDTButton.setSelected(Integer.parseInt(paramArray[1]) == 1);
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

    private void initNewC8ConfigFile() {
        List<String> c8ConfigNew = Arrays.asList("{",
                "{\"LRInfoBaseIDListSize\",",
                "{\"N\",4},\"ShowIBsAsTree\",",
                "{\"B\",0},\"AutoSortIBs\",",
                "{\"B\",1},\"ShowRecentIBs\",",
                "{\"B\",0},\"DefaultConnectionSpeed\",",
                "{\"N\",1},\"ShowStartEDTButton\",",
                "{\"B\",0},\"\"},",
                "{",
                "{\"OfflineCustomizationStorage\",",
                "{\"StartupDlgWindowPos\",",
                "{\"S\",\"{1,1,\"\"StartUpDlg.f\"\",\"\"{3,1,\"\"\"\"TopLevel" +
                        "TaxiPlus/_TDI\"\"\"\",\"\"\"\"{7,1,1034,505,1526,896,490,349,0,0,0," +
                        "00000000-0000-0000-0000-000000000000,0," +
                        "AAAAAAAAAAAAAAAAAAAAAAAAAAA=,0,0,0,0,0,1,0}\"\"\"\"}\"\"}\"},\"\"},",
                "{",
                "{\"\"}",
                "}",
                "},",
                "{\"ModalViewsTaxiPlus\",",
                "{\"Запуск 1С:Предприятия\",",
                "{\"S\",\"{9,1,490,349,-2147483648,-2147483648,0,0,\"\"{1,0,149}\"\",1}\"},\"\"},",
                "{",
                "{\"\"}",
                "}",
                "},",
                "{\"\"}",
                "}",
                "}");
        cv8config.addAll(c8ConfigNew);
    }

    public void close() {
        stage.close();
    }
}
