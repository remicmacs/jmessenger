package us.hourgeon.jmessenger.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.UUID;

public class AddChannelDialogController {
    @FXML
    CheckBox privateCheckbox;
    @FXML
    TextField nameField;
    @FXML
    TextArea invitesField;
    @FXML
    Button confirmButton;
    @FXML
    HBox privateBox;
    @FXML
    Label nameInstruction;

    private ChannelEvents events;
    private boolean isDirect;

    public void initialize() {
        isDirect = false;
        confirmButton.setOnAction(value -> {
            ((Stage)confirmButton.getScene().getWindow()).close();
            events.onCreateRequest(nameField.getText(), invitesField.getText(), isDirect, privateCheckbox.isSelected());
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
}
