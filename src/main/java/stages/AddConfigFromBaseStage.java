package stages;

import controllers.AddConfigFromBaseController;
import controllers.MainWindowController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AddConfigFromBaseStage extends Stage {

    public AddConfigFromBaseStage(MainWindowController mainController) {
        Parent root = null;
        try {
            AddConfigFromBaseController configEditController = new AddConfigFromBaseController(mainController, this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddConfigFromBase.fxml"));
            loader.setController(configEditController);
            root = loader.load();
//            setResizable(false);
            initModality(Modality.APPLICATION_MODAL);
            setTitle("Добавление конфигурации из хранилища");
            Scene scene = new Scene(root, 670, 320);
            setMinHeight(320);
            setMinWidth(500);
            setMaxHeight(800);
            setMaxWidth(1000);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
