package us.hourgeon.jmessenger.client;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import us.hourgeon.jmessenger.client.MessageCell.MessageCellFactory;
import us.hourgeon.jmessenger.server.Model.WSMessageTest;

public class ChatWindowController implements MessageEvents {
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

    private static final ObservableList<String> rooms = FXCollections.observableArrayList();
    private static final ObservableList<String> conversations = FXCollections.observableArrayList();
    private static final ObservableList<String> participants = FXCollections.observableArrayList();
    private static final ObservableList<WSMessageTest> messages = FXCollections.observableArrayList();

    private WebSocketController webSocketController;

    /**
     * Mandatory function for JavaFX controllers
     */
    public void initialize() {

        // We set the content first before setting the look
        roomsList.setItems(rooms);
        conversationsList.setItems(conversations);
        contactsList.setItems(participants);
        messagesList.setItems(messages);

        // Set the cell factory of the messages list to a fancy custom cell
        messagesList.setCellFactory(new MessageCellFactory());

        // We set the height of the roomsList as the number of rooms times the height of a row
        roomsList.prefHeightProperty().bind(Bindings.size(rooms).multiply(24));

        // We set the height of the conversationsList as the number of conversations times the height of a row
        conversationsList.prefHeightProperty().bind(Bindings.size(conversations).multiply(24));

        // Create the selection models
        GroupSelectionModel<String> first = new GroupSelectionModel<>(conversations);
        GroupSelectionModel<String> second = new GroupSelectionModel<>(rooms);

        // Set their soulmate to each others (ooooh so cute)
        first.setSoulmate(second);
        second.setSoulmate(first);

        // Set the selection models for the lists
        conversationsList.setSelectionModel(first);
        roomsList.setSelectionModel(second);

        // Add a listener to a selection change (just for testing now)
        roomsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old, String neww) {
                System.out.println(old);
                System.out.println(neww);
            }
        });

        // Fill the lists with fake data
        for (int i = 0; i < 7; i++) {
            rooms.add("Room " + i);
            conversations.add("Conversation " + i);
            participants.add("Contact " + i);
        }

        // Configure the chat entry field to send the messages
        chatEntryField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                send();
            }
        });
        chatEntrySendButton.setOnAction(value -> send());
    }


    /**
     * Set the websocket controller and register the appropriate events
     * @param webSocketController The websocket controller
     */
    void setWebSocketController(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;
        this.webSocketController.registerMessageEvents(this);
    }


    /**
     * Send the text contained in the chat entry field
     */
    private void send() {

        // Get the text and clear the text field for after
        String message = chatEntryField.getText();
        chatEntryField.clear();

        // Check if the message is empty before sending
        if (webSocketController != null && !message.isEmpty()) {
            webSocketController.send(message);
        }

        // TODO : Here we can implement a list of messages awaiting confirmation before displaying the message
        WSMessageTest wsMessage = new WSMessageTest("me", "main", message);
        messages.add(wsMessage);
    }


    /**
     * Executed when a message is received, this is were we implement adding the message
     * to the chat window
     * @param message The message received
     */
    @Override
    public void onMessage(String message) {

        // We add the message and set the scrolling to the bottom
        WSMessageTest wsMessage = new WSMessageTest("server", "main", message);
        messages.add(wsMessage);
        messagesList.scrollTo(messages.size());
    }
}