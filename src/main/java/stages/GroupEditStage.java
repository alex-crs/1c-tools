package stages;

import controllers.GroupEditController;
import controllers.MainWindowController;
import entities.Const;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class GroupEditStage extends Stage {
    public GroupEditStage(Const aConst, MainWindowController mainWindowController) {
        Parent root = null;
        try {
            GroupEditController groupEditController = new GroupEditController();
            groupEditController.setMainWindowController(mainWindowController);
            groupEditController.setAConst(aConst);
            groupEditController.setStage(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GroupEditWindow.fxml"));
            loader.setController(groupEditController);
            root = loader.load();
            setTitle(aConst.getTitle());
            setResizable(false);
            initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 400, 60);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setOnCloseRequest(event -> mainWindowController.user_list.returnToCurrentGroup());
    }
}