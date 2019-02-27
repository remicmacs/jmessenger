import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;

public class ChatWindow implements MessageEvents {
    @FXML
    ListView roomsList;

    @FXML
    ListView conversationsList;

    @FXML
    ListView contactsList;

    @FXML
    ListView messagesList;

    @FXML
    TextField chatEntryField;

    @FXML
    Button chatEntrySendButton;

    private List<WSMessageTest> wsMessages = new ArrayList<>();

    private static final ObservableList<String> rooms = FXCollections.observableArrayList();
    private static final ObservableList<String> conversations = FXCollections.observableArrayList();
    private static final ObservableList<String> participants = FXCollections.observableArrayList();
    private static final ObservableList<WSMessageTest> messages = FXCollections.observableArrayList();

    private WebSocketController webSocketController;

    public void initialize() {
        roomsList.setItems(rooms);
        roomsList.prefHeightProperty().bind(Bindings.size(rooms).multiply(24));

        conversationsList.setItems(conversations);
        conversationsList.prefHeightProperty().bind(Bindings.size(conversations).multiply(24));

        contactsList.setItems(participants);

        messagesList.setItems(messages);
        messagesList.setCellFactory(new MessageCellFactory());

        // Fill the lists with fake data
        for (int i = 0; i < 7; i++) {
            rooms.add("Room " + i);
            conversations.add("Conversation " + i);
            participants.add("Contact " + i);
        }

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
        WSMessageTest wsMessage = new WSMessageTest("me", "main", message);
        messages.add(wsMessage);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
        WSMessageTest wsMessage = new WSMessageTest("server", "main", message);
        messages.add(wsMessage);
        messagesList.scrollTo(messages.size());
    }
}
