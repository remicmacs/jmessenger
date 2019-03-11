package us.hourgeon.jmessenger.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import us.hourgeon.jmessenger.Model.AbstractChannel;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.client.ContactCell.ContactCellFactory;

import java.util.ArrayList;
import java.util.UUID;

public class InviteDialogController {
    @FXML
    VBox vBox;
    @FXML
    ListView usersList;
    @FXML
    Button inviteButton;

    private final static ObservableList<User> users = FXCollections.observableArrayList();
    private AbstractChannel channel;

    ContactEvents events;

    public void initialize() {
        usersList.setItems(users);
        usersList.setCellFactory(new ContactCellFactory(events, false));
        usersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        for (int i=0; i < 10; i++) {
            User newUser = new User("User " + i, UUID.randomUUID());
            users.add(newUser);
        }
        inviteButton.setOnAction(value -> {
            ArrayList<User> selected = new ArrayList<>(usersList.getSelectionModel().getSelectedItems());
            ((Stage)inviteButton.getScene().getWindow()).close();
            events.onInvitesRequest(selected, channel);
        });
    }

    void setChannel(AbstractChannel channel) {
        this.channel = channel;
    }

    void setEvents(ContactEvents events) {
        this.events = events;
    }
}
