package controllers;

import javafx.fxml.Initializable;
import settings.Ignored_objects;
import settings.UserList;

import java.net.URL;
import java.util.ResourceBundle;

import static handlers.CacheCleaner.clearCacheByUser;

public class MainWindowController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void clearCache(){
        clearCacheByUser(new UserList(), new Ignored_objects());
    }
}
