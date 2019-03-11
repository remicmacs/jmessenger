package us.hourgeon.jmessenger.client.ContactCell;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.client.ContactEvents;

import java.io.IOException;

class ContactCellController {
    @FXML
    private Label nickNameLabel;

    @FXML
    private VBox vBox;

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

        MenuItem kickItem = new MenuItem("Kick");
        MenuItem banItem = new MenuItem("Ban");
        MenuItem promoteItem = new MenuItem("Promote");
        contextMenu.getItems().addAll(kickItem, banItem, promoteItem);

        // Invoking the context menu on right-click
        vBox.setOnContextMenuRequested(event -> {
            if (hasContextMenu) {
                contextMenu.show(vBox, event.getScreenX(), event.getScreenY());
            }
        });

        // Reacting to the choice on a context menu item
        kickItem.setOnAction(actionEvent -> events.onKickRequest(user.getUuid()));
        banItem.setOnAction(actionEvent -> events.onBanRequest(user.getUuid()));
        promoteItem.setOnAction(actionEvent -> events.onPromoteRequest(user.getUuid()));
    }

    void setNickname(String nickname) {
        nickNameLabel.setText(nickname);
    }

    void setUser(User user) {
        this.user = user;
    }

    VBox getBox() {
        return vBox;
    }
}
