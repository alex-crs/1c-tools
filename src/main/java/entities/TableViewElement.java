package entities;

import controllers.MainWindowController;
import entities.configStructure.Base;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import service.DataBaseService;

public class TableViewElement {
    TableView<Base> configCollection;

    final ObservableList<Base> configCollectionList = FXCollections.observableArrayList();

    MainWindowController mainWindowController;

    public TableViewElement(MainWindowController mainWindowController, TableView<Base> configCollection) {
        this.mainWindowController = mainWindowController;
        this.configCollection = configCollection;
        SQLConfigListInit();
    }


    public void SQLConfigListInit() {
        TableColumn<Base, String> configName = new TableColumn<>("Имя конфигурации");
        configName.setMinWidth(200);
        TableColumn<Base, String> baseType = new TableColumn<>("Тип базы");
        baseType.setMinWidth(100);
        TableColumn<Base, String> configPath = new TableColumn<>("Путь");
        configPath.setMinWidth(400);
        configCollection.getColumns().add(configName);
        configCollection.getColumns().add(baseType);
        configCollection.getColumns().add(configPath);
        configName.setCellValueFactory(new PropertyValueFactory<>("elementName"));

        baseType.setCellValueFactory(param -> {
            String baseType1 = null;
            if (param.getValue().getConnect().contains("File")) {
                baseType1 = "Файловая база";
            }
            if (param.getValue().getConnect().contains("ws")) {
                baseType1 = "WEB-сервер";
            }
            if (param.getValue().getConnect().contains("Srvr")) {
                baseType1 = "1С сервер";
            }
            String finalBaseType = baseType1;
            return getStringObservableValue(finalBaseType);
        });
        configPath.setCellValueFactory(param -> {
            String path = null;
            String connect = param.getValue().getConnect();
            if (param.getValue().getConnect().contains("File")) {
                path = connect.substring(5).replaceAll("[\";]", "");
            }
            if (param.getValue().getConnect().contains("ws")) {
                path = connect.substring(3).replaceAll("[\";]", "");
            }
            if (param.getValue().getConnect().contains("Srvr")) {
                String[] server = connect.split("=");
                path = "Кластер серверов = " + server[1].replaceAll("[Ref;\"]", "")
                        + ", Имя базы = " + server[2].replaceAll("[;\"]", "");
            }
            final String exitPath = path;
            return getStringObservableValue(exitPath);
        });
        configCollection.setItems(configCollectionList);
    }

    public void loadSQLConfigListByGroup(ComboBox<String> comboBox) {
        configCollectionList.clear();
        configCollectionList.addAll(mainWindowController.data_base.getBaseListByGroup(comboBox.getValue()));
    }

    private ObservableValue<String> getStringObservableValue(String exitPath) {
        return new ObservableValue<String>() {
            @Override
            public void addListener(ChangeListener<? super String> listener) {

            }

            @Override
            public void removeListener(ChangeListener<? super String> listener) {

            }

            @Override
            public String getValue() {
                return exitPath;
            }

            @Override
            public void addListener(InvalidationListener listener) {

            }

            @Override
            public void removeListener(InvalidationListener listener) {

            }
        };
    }
}
