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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class ChatWindowController implements MessageEvents, ChannelEvents, ContactEvents {

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
    @FXML
    Button inviteButton;

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

    private static final UUID adminChannelUUID = new UUID(0,0);


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
        contactsList.setCellFactory(new ContactCellFactory(this));
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

        addRoomButton.setOnAction(value -> openAddChannelDialog(false));
        addConvoButton.setOnAction(value -> openAddChannelDialog(true));
        inviteButton.setOnAction(value -> openInviteDialog());
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
        if (!message.isEmpty()) {
            AbstractChannel room = (AbstractChannel)currentRoom.getValue();

            Message wsMessage = new Message(me, room, message, ZonedDateTime.now());

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


    private void openAddChannelDialog(boolean isDirect) {
        String title = isDirect ? "Add a conversation" : "Add a room";

        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("addroomdialog.fxml"));
        try {
            Parent root = loader.load();

            dialog.setTitle(title);
            dialog.setScene(new Scene(root, 400, 250));
            dialog.show();

            if (isDirect) {
                ((AddChannelDialogController)loader.getController()).setDirect();
            }
            ((AddChannelDialogController)loader.getController()).setChannelEvents(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void openInviteDialog() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("invitedialog.fxml"));
        try {
            Parent root = loader.load();

            dialog.setTitle("Invite users");
            dialog.setScene(new Scene(root, 400, 250));
            dialog.show();

            ((InviteDialogController)loader.getController()).setChannel((AbstractChannel)currentRoom.getValue());
            ((InviteDialogController)loader.getController()).setEvents(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onQuitRequest(UUID uuid) {
        System.out.println("Request quitting channel " + uuid.toString());
    }

    @Override
    public void onDeleteRequest(UUID uuid) {
        System.out.println("Request removing channel " + uuid.toString());
    }

    @Override
    public void onCreateRequest(String name, String invites, boolean isDirect, boolean isPrivate) {
        System.out.println("Create channel !");
        System.out.println("Name : " + name);
        System.out.println("Invites : " + invites);
        System.out.println("Is direct : " + isDirect);
        System.out.println("Is private : " + isPrivate);

        Message message = new Message(
                this.me.getUuid(),
                adminChannelUUID,
                "Create Channel " + name,
                ZonedDateTime.now()
        );

        String toSend = gson.toJson(message, Message.class);
        this.webSocketController.send(toSend);
    }

    @Override
    public void onInviteRequest(UUID user, UUID channel) {
        System.out.println("Request kicking " + user.toString() + " from " + channel.toString());
    }

    @Override
    public void onInvitesRequest(ArrayList<User> users, AbstractChannel channel) {
        System.out.println("Invites request: ");
        for (User user:users) {
            System.out.println(user.getUuid());
        }
    }

    @Override
    public void onKickRequest(UUID user) {
        AbstractChannel channel = (AbstractChannel)currentRoom.getValue();
        System.out.println("Request kicking " + user.toString() + " from " + channel.getChannelId().toString());
    }

    @Override
    public void onBanRequest(UUID user) {
        AbstractChannel channel = (AbstractChannel)currentRoom.getValue();
        System.out.println("Request banning " + user.toString() + " from " + channel.getChannelId().toString());
    }

    @Override
    public void onPromoteRequest(UUID user) {
        AbstractChannel channel = (AbstractChannel)currentRoom.getValue();
        System.out.println("Request promoting " + user.toString() + " from " + channel.getChannelId().toString());
    }
}
