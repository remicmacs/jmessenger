import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;

public class ChatWindow implements MessageEvents {
    @FXML
    ListView chatroomsList;

    @FXML
    ListView contactsList;

    @FXML
    ListView messagesList;

    @FXML
    TextField chatEntryField;

    @FXML
    Button chatEntrySendButton;

    private List<String> cells = new ArrayList<>();
    private List<String> contacts = new ArrayList<>();
    private List<String> messages = new ArrayList<>();

    private ListProperty<String> listProperty = new SimpleListProperty<>();
    private ListProperty<String> contactsListProperty = new SimpleListProperty<>();
    private ListProperty<String> messagesListProperty = new SimpleListProperty<>();

    private WebSocketController webSocketController;

    public void initialize() {
        // Fill the rooms list with fake data
        for (int i = 0; i < 20; i++) {
            cells.add("Room " + i);
        }

        // Fill the contacts list with fake data
        for (int i = 0; i < 20; i++) {
            contacts.add("Contact " + i);
        }

        chatroomsList.itemsProperty().bind(listProperty);
        contactsList.itemsProperty().bind(contactsListProperty);
        messagesList.itemsProperty().bind(messagesListProperty);

        listProperty.set(FXCollections.observableArrayList(cells));
        contactsListProperty.set(FXCollections.observableArrayList(contacts));
        messagesListProperty.set(FXCollections.observableArrayList(messages));

        chatEntryField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                send();
            }
        });
        chatEntrySendButton.setOnAction(value -> send());
    }

    void setWebSocketController(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;
        this.webSocketController.registerMessageEvents(this);
    }

    private void send() {
        String message = chatEntryField.getText();
        chatEntryField.clear();
        if (webSocketController != null && !message.isEmpty()) {
            webSocketController.send(message);
        }
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
        messages.add(message);
        messagesListProperty.set(FXCollections.observableArrayList(messages));
        messagesList.scrollTo(messages.size());
    }
}
