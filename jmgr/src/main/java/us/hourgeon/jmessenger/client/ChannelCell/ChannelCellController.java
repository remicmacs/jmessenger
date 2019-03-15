package us.hourgeon.jmessenger.client.ChannelCell;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import us.hourgeon.jmessenger.client.ChannelEvents;

import java.io.IOException;
import java.util.UUID;

class ChannelCellController {
    @FXML
    private Label nickNameLabel;

    @FXML
    private VBox vBox;

    private ContextMenu contextMenu = new ContextMenu();
    private ChannelEvents events;

    private UUID uuid;

    private boolean hasContextMenu = true;

    ChannelCellController(ChannelEvents events) {
        this();
        this.events = events;
    }

    ChannelCellController(ChannelEvents events, boolean hasContextMenu) {
        this();
        this.events = events;
        this.hasContextMenu = hasContextMenu;
    }

    /**
     * Constructor
     */
    ChannelCellController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("channelcell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        MenuItem removeItem = new MenuItem("Remove");
        MenuItem quitItem = new MenuItem("Quit");
        contextMenu.getItems().addAll(removeItem, quitItem);

        // When user right-click on Circle
        vBox.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                if (hasContextMenu) {
                    contextMenu.show(vBox, event.getScreenX(), event.getScreenY());
                }
            }
        });

        removeItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                events.onDeleteRequest(uuid);
            }
        });

        quitItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                events.onQuitRequest(uuid);
            }
        });
    }

    void setNickname(String nickname) {
        nickNameLabel.setText(nickname);
    }

    void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    VBox getBox() {
        return vBox;
    }
}
