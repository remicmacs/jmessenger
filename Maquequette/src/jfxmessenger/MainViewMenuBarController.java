package jfxmessenger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainViewMenuBarController {
    @FXML public MenuItem mainMenuItemFileMenuClose;
    @FXML public Menu mainMenuFileMenu;
    @FXML public Menu mainMenuEditMenu;
    @FXML public Menu mainMenuHelpMenu;
    @FXML public MenuBar root;

    /* Code inutile ?
    private ResourceBundle bundle;

    public void initialize(URL location, ResourceBundle resources) {
        bundle = resources;
        mainMenuItemFileMenuClose.setText(bundle.getString("FileMenuItemClose"));
    }
    */

    public void onClick(ActionEvent actionEvent) {
        Stage stage = (Stage) this.root.getParent().getScene().getWindow();
        stage.close();
    }
}
