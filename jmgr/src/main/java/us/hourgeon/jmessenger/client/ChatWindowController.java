package us.hourgeon.jmessenger.client;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;

import us.hourgeon.jmessenger.client.ContactCell.ContactCellFactory;
import us.hourgeon.jmessenger.client.MessageCell.MessageCellFactory;
import us.hourgeon.jmessenger.server.Model.User;
import us.hourgeon.jmessenger.server.Model.WSMessageTest;

import java.io.IOException;
import java.util.UUID;

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
    TextArea chatEntryField;
    @FXML
    Button chatEntrySendButton;
    @FXML
    Label roomLabel;
    @FXML
    Button addConvoButton;
    @FXML
    Button addRoomButton;

    private static final ObservableList<String> rooms = FXCollections.observableArrayList();
    private static final ObservableList<String> conversations = FXCollections.observableArrayList();
    private static final ObservableList<User> participants = FXCollections.observableArrayList();
    private static final ObservableList<WSMessageTest> messages = FXCollections.observableArrayList();

    private ReadOnlyObjectProperty currentRoom;

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
        contactsList.setCellFactory(new ContactCellFactory());

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

        // The currentRoom will hold the selection from the conversations/rooms list
        // We'll bind the room label to the selected room property so that it will update
        // automatically
        currentRoom = first.selectedItemProperty();
        roomLabel.textProperty().bind(currentRoom);

        // Add a listener to a selection change (just for testing now)
        first.selectedItemProperty().addListener((observableValue, old, neww) -> {
            // Here must be implemented any events following the selection of a room
            // Ideally, everything should be bound but if it is not, this is were we manually
            // set the current room and chat window content and stuff like that
        });

        // Fill the lists with fake data
        for (int i = 0; i < 7; i++) {
            rooms.add("Room " + i);
            conversations.add("Conversation " + i);
            participants.add(new User("Contact " + i, UUID.randomUUID()));
        }

        // Default setting on start
        roomsList.getSelectionModel().select(0);

        // Configure the chat entry field to send the messages
        chatEntryField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                send();
                event.consume();
            }
            if (event.getCode() == KeyCode.ENTER && event.isShiftDown()) {
                chatEntryField.appendText("\n");
            }
        });
        chatEntrySendButton.setOnAction(value -> send());

        addRoomButton.setOnAction(value -> addRoom());
        addConvoButton.setOnAction(value -> addConversation());
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

            // TODO : Here we can implement a list of messages awaiting confirmation before displaying the message
            // Or maybe we should let the websocket handle this
            WSMessageTest wsMessage = new WSMessageTest("me", "main", message);
            messages.add(wsMessage);
        }
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


    private void addRoom() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("addroomdialog.fxml"));
        Parent root = null;
        try {
            root = loader.load();
            dialog.setTitle("Add a room");
            dialog.setScene(new Scene(root, 400, 250));
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addConversation() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("addconversationdialog.fxml"));
        Parent root = null;
        try {
            root = loader.load();
            dialog.setTitle("Add a conversation");
            dialog.setScene(new Scene(root, 400, 250));
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
