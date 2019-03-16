package us.hourgeon.jmessenger.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import us.hourgeon.jmessenger.Model.AbstractChannel;
import us.hourgeon.jmessenger.Model.Channel;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.client.ChannelCell.ChannelCellFactory;
import us.hourgeon.jmessenger.client.ContactCell.ContactCellFactory;

import java.util.ArrayList;
import java.util.UUID;

public class JoinDialogController {
    @FXML
    VBox vBox;
    @FXML
    ListView channelsList;
    @FXML
    Button joinButton;

    private static ObservableList<AbstractChannel> channels = FXCollections.observableArrayList();

    ChannelEvents events;

    public void initialize() {
        channelsList.setCellFactory(new ChannelCellFactory(events, false));
        channelsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        channelsList.setPlaceholder(new Label("No rooms to join..."));

        joinButton.setOnAction(value -> {
            Channel channel = (Channel)channelsList.getSelectionModel().getSelectedItem();
            ((Stage) joinButton.getScene().getWindow()).close();
            events.onJoinRequest(channel.getChannelId());
        });
    }

    void setEvents(ChannelEvents events) {
        this.events = events;
    }

    void setChannelsList(ObservableList<AbstractChannel> channels) {
        this.channels = channels;
        channelsList.setItems(channels);
    }
}
