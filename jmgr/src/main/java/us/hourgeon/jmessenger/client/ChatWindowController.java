package us.hourgeon.jmessenger.client;

import com.google.gson.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
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

import us.hourgeon.jmessenger.client.ChannelCell.ChannelCellFactory;
import us.hourgeon.jmessenger.client.ContactCell.ContactCellFactory;
import us.hourgeon.jmessenger.client.MessageCell.MessageCellFactory;
import us.hourgeon.jmessenger.Model.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;

public class ChatWindowController implements MessageEvents, ChannelEvents {

    @FXML
    Button testButton;
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

    private static final ObservableList<AbstractChannel> rooms = FXCollections.observableArrayList();
    private static final ObservableList<AbstractChannel> conversations = FXCollections.observableArrayList();
    private static final ObservableList<User> participants = FXCollections.observableArrayList();
    private static final ObservableList<Message> messages = FXCollections.observableArrayList();

    private ReadOnlyObjectProperty currentRoom;

    private WebSocketController webSocketController;

    private User me;

    private static final Gson gson =
            new GsonBuilder().registerTypeAdapter(
                    ZonedDateTime.class, new ZDTSerializerDeserializer())
                    .create();


    /**
     * Mandatory function for JavaFX controllers
     */
    public void initialize() {

        // TODO: USER HERE ! TO REPLACE !
        me = new User("me", UUID.randomUUID());

        // We set the content first before setting the look
        roomsList.setItems(rooms);
        conversationsList.setItems(conversations);
        contactsList.setItems(participants);
        messagesList.setItems(messages);

        // Set the cell factory of the messages list to a fancy custom cell
        messagesList.setCellFactory(new MessageCellFactory());
        contactsList.setCellFactory(new ContactCellFactory());
        roomsList.setCellFactory(new ChannelCellFactory(this));
        conversationsList.setCellFactory(new ChannelCellFactory(this));

        // We set the height of the roomsList as the number of rooms times the height of a row
        roomsList.prefHeightProperty().bind(Bindings.size(rooms).multiply(24));

        // We set the height of the conversationsList as the number of conversations times the height of a row
        conversationsList.prefHeightProperty().bind(Bindings.size(conversations).multiply(24));

        // Create the selection models
        GroupSelectionModel<AbstractChannel> first = new GroupSelectionModel<>(conversations);
        GroupSelectionModel<AbstractChannel> second = new GroupSelectionModel<>(rooms);

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

        // Add a listener to a selection change (just for testing now)
        first.selectedItemProperty().addListener((observableValue, old, neww) -> {
            // Here must be implemented any events following the selection of a room
            // Ideally, everything should be bound but if it is not, this is were we manually
            // set the current room and chat window content and stuff like that
            AbstractChannel room = (AbstractChannel)currentRoom.getValue();
            roomLabel.setText(room.getChannelId().toString());
            messages.setAll(room.getHistory().getMessages());
        });

        // Fill the lists with fake data
        for (int i = 0; i < 7; i++) {
            participants.add(new User("Contact " + i, UUID.randomUUID()));

            conversations.add(new DirectMessageChannel(UUID.randomUUID(), Collections.emptyList(), Collections.emptySortedSet()));
            rooms.add(new PublicChannel(UUID.randomUUID(), Collections.emptyList(), Collections.emptySortedSet()));
            rooms.add(new PrivateChannel(UUID.randomUUID(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptySortedSet()));
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
        testButton.setOnAction(value -> sendTestMessage());
    }

    private void sendTestMessage() {
        UUID adminChannelUUID = new UUID(0,0);
        String adminCommand = "Create Channel Toto";

        Message adminMessageTest = new Message(
                this.me.getUuid(),
                adminChannelUUID,
                adminCommand,
                ZonedDateTime.now()
        );
        Gson gson =
                new GsonBuilder().registerTypeAdapter(
                        ZonedDateTime.class, new ZDTSerializerDeserializer())
                        .create();
        String toSend = gson.toJson(adminMessageTest, Message.class);
        this.webSocketController.send(toSend);
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
            AbstractChannel room = (AbstractChannel)currentRoom.getValue();

            // TODO : Here we can implement a list of messages awaiting confirmation before displaying the message
            // Or maybe we should let the websocket handle this
            Message wsMessage = new Message(me, room, message, ZonedDateTime.now());

            room.appendMessage(wsMessage);

            String toSend = gson.toJson(wsMessage, Message.class);
            this.webSocketController.send(toSend);
        }
    }


    /**
     * Executed when a message is received, this is were we implement adding the message
     * to the chat window
     * @param message The message received
     */
    @Override
    public void onMessage(String message) {
        AbstractChannel room = (AbstractChannel)currentRoom.getValue();

        // We add the message and set the scrolling to the bottom
        Message receivedMessage = gson.fromJson(message, Message.class);
        messages.add(receivedMessage);
        room.appendMessage(receivedMessage);
        messagesList.scrollTo(messages.size());
    }


    private void addRoom() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("addroomdialog.fxml"));
        Parent root;
        try {
            root = loader.load();
            dialog.setTitle("Add a room");
            dialog.setScene(new Scene(root, 400, 250));
            dialog.show();
            ((AddChannelDialogController)loader.getController()).setChannelEvents(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addConversation() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("addroomdialog.fxml"));
        Parent root = null;
        try {
            root = loader.load();
            dialog.setTitle("Add a conversation");
            dialog.setScene(new Scene(root, 400, 250));
            dialog.show();
            ((AddChannelDialogController)loader.getController()).setDirect();
            ((AddChannelDialogController)loader.getController()).setChannelEvents(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onQuitRequest(UUID uuid) {
        AbstractChannel room = (AbstractChannel)currentRoom.getValue();

        // We add the message and set the scrolling to the bottom
        Message receivedMessage = new Message(me, room, "quit " + uuid.toString(), ZonedDateTime.now());

        messages.add(receivedMessage);
        room.appendMessage(receivedMessage);
    }

    @Override
    public void onDeleteRequest(UUID uuid) {
        AbstractChannel room = (AbstractChannel)currentRoom.getValue();

        // We add the message and set the scrolling to the bottom
        Message receivedMessage = new Message(me, room, "delete " + uuid.toString(), ZonedDateTime.now());

        messages.add(receivedMessage);
        room.appendMessage(receivedMessage);
    }

    @Override
    public void onCreateRequest(String name, String invites, boolean isDirect, boolean isPrivate) {
        System.out.println("Create channel !");
        System.out.println("Name : " + name);
        System.out.println("Invites : " + invites);
        System.out.println("Is direct : " + isDirect);
        System.out.println("Is private : " + isPrivate);
    }
}
