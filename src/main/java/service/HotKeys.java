package service;

import entities.WindowControllers;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class HotKeys {
    public static void closeListener(Parent pane, Stage stage){
        pane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    event.consume();
                    stage.close();
                }
            }
        });
    }

    public static void enterListener(Parent pane, Stage stage, WindowControllers controllers){
        pane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    event.consume();
                    controllers.action();
                    stage.close();
                }
            }
        });
    }
}
