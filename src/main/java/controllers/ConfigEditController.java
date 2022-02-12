package controllers;

import entities.Const;
import entities.configStructure.Base;
import entities.configStructure.Folder;
import entities.configStructure.VirtualTree;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import settings.BaseConfig;
import stages.ConfigEditStage;

import java.io.File;
import java.net.URL;
import java.util.*;

public class ConfigEditController implements Initializable {
    @FXML
    Button choiceButton;

    @FXML
    TextField pathField;

    @FXML
    TextField configName;

    @FXML
    TextField sqlAddress;

    @FXML
    TextField sqlName;

    @FXML
    ChoiceBox<String> baseType;

    @FXML
    ChoiceBox<String> virtualTreeType;

    @FXML
    ChoiceBox<String> connectionSpeed;

    @FXML
    ChoiceBox<String> authChoice;

    @FXML
    ChoiceBox<String> startType;

    @FXML
    ChoiceBox<String> bitDepth;

    @FXML
    Label serverLabel1;

    @FXML
    Label serverLabel2;

    @FXML
    Label pathLabel;

    @FXML
    Label windowMessage;

    @FXML
    TextField defaultVersion;

    @FXML
    Button accept;

    private TreeItem<VirtualTree> choiceElement;
    private final Const action;
    private final MainWindowController mainController;

    //тип элемента (файл, база)
    HashMap<Boolean, String> virtualTreeTypeValues;

    //скорость соединения
    HashMap<String, String> connectionSpeedValues;

    //тип подключения к базе
    HashMap<String, String> baseTypeValues;

    //вариант аутентификации
    ArrayList<String> authChoiceValues;

    //режим запуска 1С
    HashMap<String, String> startTypeValues;

    //разрядность запускаемого клиента 1С
    HashMap<String, String> bitDepthValues;

    Stage stage;

    File file;

    VirtualTree element;

    String connectionType;

    public ConfigEditController(TreeItem<VirtualTree> choiceElement, Const action,
                                MainWindowController mainController, Stage stage) {
        this.choiceElement = choiceElement;
        this.action = action;
        this.mainController = mainController;
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //отключаем фокусировку на кнопку
        accept.setFocusTraversable(false);
        choiceButton.setFocusTraversable(false);
        baseType.setFocusTraversable(false);
        virtualTreeType.setFocusTraversable(false);
        connectionSpeed.setFocusTraversable(false);
        authChoice.setFocusTraversable(false);
        startType.setFocusTraversable(false);
        bitDepth.setFocusTraversable(false);
        //инициализируем элементы меню
        initChoiceBoxValues();
        switch (action) {
            //если происходит создание нового объекта
            case CREATE_TREE_ELEMENT:
                //пока не введен хотя бы один символ блокируются элементы управления
                element = new Folder("");
                setDefaultValueForChoiceBoxElements();
                hideSQLServerConfigElements();
                initListeners();
                nameNonNullInspector();
                break;
            //если редактируем существующую конфигурацию
            case EDIT_TREE_CONFIG:
            case EDIT_SQL_CONFIG:
                initEditingObject();
                initListeners();
                changeWindowSize(400);
                break;
            //если редактируем существующую папку
            case EDIT_TREE_FOLDER:
                initEditingObject();
                nameNonNullInspector();
                initListeners();
                changeWindowSize(100);
                break;
        }
    }

    //инициализируем редактируемый объект
    private void initEditingObject() {
        element = choiceElement.getValue();
        accept.setText("Изменить");
        virtualTreeType.setDisable(true);
        mappingObjectToFormElements();
    }

    //сопоставляет параметры существующего объекта элементам формы (заполняет карточку объекта)
    private void mappingObjectToFormElements() {
        configName.setText(element.getElementName());
        virtualTreeType.setValue(virtualTreeTypeValues.get(element.isFolder()));
        if (!element.isFolder()) {
            connectionChoiceHandler();
            connectionSpeed.setValue(connectionSpeedValues.get(((Base) element).getClientConnectionSpeed()));
            authChoice.setValue(authChoiceValues.get(((Base) element).getWa()));
            startType.setValue(startTypeValues.get(((Base) element).getApp()));
            bitDepth.setValue(bitDepthValues.get(((Base) element).getAppArch()));
            defaultVersion.setText(((Base) element).getVersion());
        }
    }

    //из объекта определяет тип соединения (формирует форму вывода и заполняет необходимые поля)
    private void connectionChoiceHandler() {
        windowMessage.setText("Настройка прокси не поддерживается. Доступен импорт конфигурации с указанием прокси.");
        String[] path = (((Base) element).getConnect().split("="));
        switch (path[0]) {
            case "File":
                pathField.setText(path[1].replaceAll("[;\"]", ""));
                hideSQLServerConfigElements();
                transformToPath();
                enablePath();
                connectionType = "File=";
                break;
            case "ws":
                pathField.setText(path[1].replaceAll("[;\"]", ""));
                hideSQLServerConfigElements();
                transformToHttpPath();
                enablePath();
                connectionType = "ws=";
                break;
            case "Srvr":
                sqlAddress.setText(path[1].replaceAll("[Ref;\"]", ""));
                sqlName.setText(path[2].replaceAll("[;\"]", ""));
                showSQLServerConfig();
                disablePath();
                connectionType = "Srvr=";
                break;
        }
        baseType.setValue(baseTypeValues.get(path[0] + "="));
    }

    //инициализирует базовые параметры информационной базы (возможно есть смысл перенести в какой то класс
    //который загружается один раз, не очень хорошо что инициализация происходит каждый раз при создании объекта)
    private void initChoiceBoxValues() {
        virtualTreeTypeValues = new HashMap<>();
        virtualTreeTypeValues.put(true, "Папка");
        virtualTreeTypeValues.put(false, "Конфигурация");
        virtualTreeType.getItems().addAll(new ArrayList<>(virtualTreeTypeValues.values()));

        connectionSpeedValues = new HashMap<>();
        connectionSpeedValues.put("", "Выбирать при запуске");
        connectionSpeedValues.put("Normal", "Обычная");
        connectionSpeedValues.put("Low", "Низкая");
        connectionSpeed.getItems().addAll(new ArrayList<>(connectionSpeedValues.values()));

        baseTypeValues = new HashMap<>();
        baseTypeValues.put("File=", "Файловая база");
        baseTypeValues.put("ws=", "WEB-клиент");
        baseTypeValues.put("Srvr=", "1С сервер");
        baseType.getItems().addAll(new ArrayList<>(baseTypeValues.values()));

        authChoiceValues = new ArrayList<>(Arrays.asList("Запрашивать имя и пароль", "Выбирать автоматически"));
        authChoice.getItems().addAll(authChoiceValues);

        startTypeValues = new HashMap<>();
        startTypeValues.put("", "Выбирать автоматически");
        startTypeValues.put("ThinClient", "Тонкий клиент");
        startTypeValues.put("ThickClient", "Толстый клиент");
        startType.getItems().addAll(new ArrayList<>(startTypeValues.values()));

        bitDepthValues = new HashMap<>();
        bitDepthValues.put("x86", "32 (x86)");
        bitDepthValues.put("x86_prt", "Приоритет 32 (x86)");
        bitDepthValues.put("x86_64", "64 (x86_64)");
        bitDepthValues.put("x86_64_prt", "Приоритет 64 (x86_64)");
        bitDepth.getItems().addAll(new ArrayList<>(bitDepthValues.values()));
    }

    //инициализирует параметры НОВОГО объекта
    private void setDefaultValueForChoiceBoxElements() {
        virtualTreeType.setValue("Папка");
        connectionSpeed.setValue("Обычная");
        baseType.setValue("Файловая база");
        authChoice.setValue("Выбирать автоматически");
        startType.setValue("Выбирать автоматически");
        bitDepth.setValue("Приоритет 64 (x86_64)");
    }

    private void initListeners() {
        virtualTreeType.setOnAction(event -> {
            if (virtualTreeType.getSelectionModel().getSelectedItem().equals("Конфигурация")) {
                changeWindowSize(400);
                windowMessage.setText("Настройка прокси не поддерживается. Доступен импорт конфигурации с указанием прокси.");
                connectionType = "File=";
                element = new Base(configName.getText());
                element.setFolder(false);
            }
            if (virtualTreeType.getSelectionModel().getSelectedItem().equals("Папка")) {
                changeWindowSize(100);
                windowMessage.setText("");
                connectionType = null;
                element = new Folder(configName.getText());
            }
        });

        connectionSpeed.setOnAction(event -> {
            if (connectionSpeed.getSelectionModel().getSelectedItem().equals("Выбирать при запуске")) {
                ((Base) element).setClientConnectionSpeed("");
            }
            if (connectionSpeed.getSelectionModel().getSelectedItem().equals("Обычная")) {
                ((Base) element).setClientConnectionSpeed("Normal");
            }
            if (connectionSpeed.getSelectionModel().getSelectedItem().equals("Низкая")) {
                ((Base) element).setClientConnectionSpeed("Low");
            }
        });

        baseType.setOnAction(event -> {
            if (baseType.getSelectionModel().getSelectedItem().equals("Файловая база")) {
                hideSQLServerConfigElements();
                transformToPath();
                enablePath();
                pathField.setText("");
                connectionType = "File=";
            }
            if (baseType.getSelectionModel().getSelectedItem().equals("WEB-клиент")) {
                hideSQLServerConfigElements();
                transformToHttpPath();
                enablePath();
                pathField.setText("http://");
                connectionType = "ws=";
            }
            if (baseType.getSelectionModel().getSelectedItem().equals("1С сервер")) {
                showSQLServerConfig();
                disablePath();
                pathField.setText("");
                connectionType = "Srvr=";
            }
        });

        authChoice.setOnAction(event -> {
            if (authChoice.getSelectionModel().getSelectedItem().equals("Выбирать автоматически")) {
                ((Base) element).setWa(1);
            }
            if (authChoice.getSelectionModel().getSelectedItem().equals("Запрашивать имя и пароль")) {
                ((Base) element).setWa(0);
            }
        });

        startType.setOnAction(event -> {
            if (startType.getSelectionModel().getSelectedItem().equals("Выбирать автоматически")) {
                ((Base) element).setApp("");
            }
            if (startType.getSelectionModel().getSelectedItem().equals("Тонкий клиент")) {
                ((Base) element).setApp("ThinClient");
            }
            if (startType.getSelectionModel().getSelectedItem().equals("Толстый клиент")) {
                ((Base) element).setApp("ThickClient");
            }
        });

        bitDepth.setOnAction(event -> {
            if (bitDepth.getSelectionModel().getSelectedItem().equals("32 (x86)")) {
                ((Base) element).setAppArch("x86");
            }
            if (bitDepth.getSelectionModel().getSelectedItem().equals("Приоритет 32 (x86)")) {
                ((Base) element).setAppArch("x86_prt");
            }
            if (bitDepth.getSelectionModel().getSelectedItem().equals("64 (x86_64)")) {
                ((Base) element).setAppArch("x86_64");
            }
            if (bitDepth.getSelectionModel().getSelectedItem().equals("Приоритет 64 (x86_64)")) {
                ((Base) element).setAppArch("x86_64_prt");
            }
        });
    }

    //Изменяет размер окна
    private void changeWindowSize(double height) {
        stage.setWidth(570);
        stage.setHeight(height);
    }

    //если длина имени элемента равна 0, то все настройки отключаются
    public void nameNonNullInspector() {
        if (configName.getText().length() == 0) {
            disableAllConfigElements();
        } else {
            enableAllConfigElements();
        }
    }

    //счетчик выполнения условия (на данный момент должно быть не менее 2х)
    private int fieldFillInspector() {
        int count = 2;
        if (connectionType.equals("File=") && pathField.getText().length() == 0) {
            mainController.alert("Не заполнен путь расположения файловой базы!");
            count--;
        }

        if (connectionType.equals("ws=") && pathField.getText().length() == 0) {
            mainController.alert("Не заполнен адрес WEB-сервера!");
            count--;
        }

        if (connectionType.equals("Srvr=") && sqlAddress.getText().length() == 0 && sqlName.getText().length() == 0) {
            mainController.alert("Не заполнены параметры 1С сервера!");
            count--;
        }
        if (versionFormatInspector() < 0) {
            mainController.alert("Недопустимые символы в версии программы!");
            count--;
        }
        return count;
    }

    //проверяет введено ли число в поле "Версия"
    private long versionFormatInspector() {
        String string = defaultVersion.getText().replaceAll("\\.", "");
        long result = -1;
        try {
            result = Long.parseLong(string.length() == 0 ? "1" : string);
        } catch (NumberFormatException e) {
            return -1;
        }
        return result;
    }

    //отключает элементы настройки SQL сервера
    private void hideSQLServerConfigElements() {
        serverLabel1.setVisible(false);
        serverLabel2.setVisible(false);
        sqlAddress.setVisible(false);
        sqlName.setVisible(false);
    }

    //включает элементы настройки SQL сервера
    private void showSQLServerConfig() {
        serverLabel1.setVisible(true);
        serverLabel2.setVisible(true);
        sqlAddress.setVisible(true);
        sqlName.setVisible(true);
    }

    //отключает кнопку выбора пути файловой базы и увеличивает поле ввода адреса базы
    private void transformToHttpPath() {
        choiceButton.setVisible(false);
        pathField.setPrefWidth(246);
    }

    //включает кнопку выбора пути файловой базы и уменьшает поле ввода адреса базы
    private void transformToPath() {
        choiceButton.setVisible(true);
        pathField.setPrefWidth(149);
    }

    //отключает кнопку выбора пути и поле ввода расположения базы
    private void disablePath() {
        pathLabel.setVisible(false);
        choiceButton.setVisible(false);
        pathField.setVisible(false);
    }

    //включает кнопку выбора пути и поле ввода расположения базы
    private void enablePath() {
        pathLabel.setVisible(true);
        choiceButton.setVisible(true);
        pathField.setVisible(true);
    }

    private void disableAllConfigElements() {
        choiceButton.setDisable(true);
        pathField.setDisable(true);
        sqlAddress.setDisable(true);
        sqlName.setDisable(true);
        baseType.setDisable(true);
        virtualTreeType.setDisable(true);
        connectionSpeed.setDisable(true);
        authChoice.setDisable(true);
        startType.setDisable(true);
        bitDepth.setDisable(true);
        accept.setDisable(true);
    }

    private void enableAllConfigElements() {
        choiceButton.setDisable(false);
        pathField.setDisable(false);
        sqlAddress.setDisable(false);
        sqlName.setDisable(false);
        baseType.setDisable(false);
        //должен оставаться заблокированным, если элемент редактируется
        if (action == Const.CREATE_TREE_ELEMENT) {
            virtualTreeType.setDisable(false);
        }
        connectionSpeed.setDisable(false);
        authChoice.setDisable(false);
        startType.setDisable(false);
        bitDepth.setDisable(false);
        accept.setDisable(false);
    }

    //открывает диалог выбора файла конфигурации
    public void openFileChoiceDialog() {
        FileChooser dialog = new FileChooser();
        Stage stage = ((ConfigEditStage) choiceButton.getScene().getWindow());
        dialog.setTitle("1Cv8.1CD");
        file = dialog.showOpenDialog(stage);
        if (file != null) {
            pathField.setText(file.getPath());
        }
    }

    public void accept() {
        if (element.isFolder() || fieldFillInspector() == 2) {
            action();
        }
    }

    private void action() {
        switch (action) {
            case CREATE_TREE_ELEMENT:
                choiceElementNonNullInspector();
                connectionPathConstructor();
                //формируем путь хранения конфигурации в древе
                element.setPath(choiceElement.getValue().getPath());
                element.setElementName(configName.getText());
                addToConfigTree();
                break;
            case EDIT_TREE_CONFIG:
            case EDIT_TREE_FOLDER:
                connectionPathConstructor();
                mainController.configList_MainTab.setRoot(BaseConfig.returnConfigStructure());
                element.setElementName(configName.getText());
                ((Base) element).setVersion(defaultVersion.getText());
                mainController.enableSaveButton();
                stage.close();
                break;
            case EDIT_SQL_CONFIG:
                connectionPathConstructor();
                element.setElementName(configName.getText());
                ((Base) element).setVersion(defaultVersion.getText());
                mainController.data_base.editConfig((Base) element);
                mainController.loadSQLConfigListByGroup();
                stage.close();
                break;
        }
    }

    //если элемент не выбран, создается элемент с пустым путем (иначе метод добавления выдаст ошибку)
    private void choiceElementNonNullInspector() {
        if (choiceElement == null) {
            choiceElement = new TreeItem<>();
            choiceElement.setValue(new VirtualTree(""));
            choiceElement.getValue().setPath("");
        }
    }

    //конструирует пути подключения к базе и присваивает созданному объекту
    private void connectionPathConstructor() {
        if (!element.isFolder()) {
            if (connectionType.equals("Srvr=")) {
                ((Base) element).setConnect(connectionType + "\"" +
                        sqlAddress.getText() + "\";Ref=\"" + sqlName.getText() + "\";");
            } else {
                ((Base) element).setConnect(connectionType + "\"" + pathField.getText() + "\";");
            }
        }
    }

    //передает команду центральному методу добавления элементов в базу
    private void addToConfigTree() {
        int answer = MainWindowController.editConfigControllerManager(action, choiceElement, element);
        if (answer > 0) {
            mainController.configList_MainTab.setRoot(BaseConfig.returnConfigStructure());
            mainController.enableSaveButton();
            stage.close();
        } else {
            mainController.alert("База или папка с таким именем уже существует.");
        }
    }
}


