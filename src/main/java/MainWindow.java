import controllers.MainWindowController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainWindow extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(500);
        primaryStage.setTitle("Cache cleaner");
        FXMLLoader mainWindow = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent main = mainWindow.load();
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(main, 915, 650));
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                MainWindowController controller = mainWindow.getController();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}