package us.hourgeon.jmessenger.client.ContactCell;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.client.ContactEvents;

import java.io.IOException;

class ContactCellController {
    @FXML
    private Label nickNameLabel;
    @FXML
    private HBox vBox;
    @FXML
    private ImageView imageView;

    private ContextMenu contextMenu = new ContextMenu();
    private ContactEvents events;
    private User user;
    private boolean hasContextMenu = true;

    ContactCellController(ContactEvents events) {
        this();
        this.events = events;
    }

    ContactCellController(ContactEvents events, boolean hasContextMenu) {
        this();
        this.events = events;
        this.hasContextMenu = hasContextMenu;
    }

    /**
     * Constructor
     */
    ContactCellController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("contactcell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        MenuItem banItem = new MenuItem("Ban");
        MenuItem promoteItem = new MenuItem("Promote");
        contextMenu.getItems().addAll(banItem, promoteItem);

        // Invoking the context menu on right-click
        vBox.setOnContextMenuRequested(event -> {
            if (hasContextMenu) {
                contextMenu.show(vBox, event.getScreenX(), event.getScreenY());
            }
        });

        // Reacting to the choice on a context menu item
        banItem.setOnAction(actionEvent -> events.onBanRequest(user));
        promoteItem.setOnAction(actionEvent -> events.onPromoteRequest(user));
    }

    void setNickname(String nickname) {
        nickNameLabel.setText(nickname);
    }

    void setUser(User user) {
        this.user = user;
    }

    void setImageUrl(String url) {
        Image newImage = new Image(getClass().getResource(url).toExternalForm());
        imageView.setFitHeight(16);
        imageView.setPreserveRatio(true);
        imageView.setImage(newImage);
    }

    void hideImage() {
        imageView.setVisible(false);
        imageView.setManaged(false);
    }

    HBox getBox() {
        return vBox;
    }
}
