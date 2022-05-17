package controllers;

import javafx.stage.Stage;

public class AboutWindowController {
    private Stage stage;

    public AboutWindowController(Stage stage) {
        this.stage = stage;
    }

    public void close(){
        stage.close();
    }

}
