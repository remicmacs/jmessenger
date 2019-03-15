package us.hourgeon.jmessenger.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.client.ContactCell.ContactCellFactory;

import java.util.ArrayList;
import java.util.UUID;

public class AddChannelDialogController {
    @FXML
    CheckBox privateCheckbox;
    @FXML
    TextField nameField;
    @FXML
    ListView usersList;
    @FXML
    Button confirmButton;
    @FXML
    HBox privateBox;
    @FXML
    Label nameInstruction;

    private ChannelEvents events;
    private boolean isDirect;
    private static ObservableList<User> users = FXCollections.observableArrayList();

    public void initialize() {
        isDirect = false;
        usersList.setCellFactory(new ContactCellFactory(null, false));
        usersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        usersList.setPlaceholder(new Label("Loading..."));

        confirmButton.setOnAction(value -> {
            ((Stage)confirmButton.getScene().getWindow()).close();
            ArrayList<User> selected = new ArrayList<>(usersList.getSelectionModel().getSelectedItems());
            events.onCreateRequest(nameField.getText(), selected, isDirect, privateCheckbox.isSelected());
        });
    }

    void setChannelEvents(ChannelEvents events) {
        this.events = events;
    }

    void setDirect() {
        isDirect = true;
        privateBox.setVisible(false);
        privateBox.setManaged(false);
        nameInstruction.setVisible(false);
        nameInstruction.setManaged(false);
        nameField.setVisible(false);
        nameField.setManaged(false);
    }

    void setUsers(ObservableList<User> users) {
        this.users = users;
        usersList.setItems(users);
    }
}
