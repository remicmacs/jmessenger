package us.hourgeon.jmessenger.client.ChannelCell;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;

class ChannelCellController {
    @FXML
    private Label nickNameLabel;

    @FXML
    private VBox vBox;

    private ContextMenu contextMenu = new ContextMenu();

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

        MenuItem kickItem = new MenuItem("Remove");
        MenuItem promoteItem = new MenuItem("Quit");
        contextMenu.getItems().addAll(kickItem, promoteItem);

        // When user right-click on Circle
        vBox.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show(vBox, event.getScreenX(), event.getScreenY());
            }
        });
    }

    void setNickname(String nickname) {
        nickNameLabel.setText(nickname);
    }

    VBox getBox() {
        return vBox;
    }
}
