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

import us.hourgeon.jmessenger.AdminCommand;
import us.hourgeon.jmessenger.client.ChannelCell.ChannelCellFactory;
import us.hourgeon.jmessenger.client.ContactCell.ContactCellFactory;
import us.hourgeon.jmessenger.client.MessageCell.MessageCellFactory;
import us.hourgeon.jmessenger.Model.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

// For XML export
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;

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
    @FXML
    Button exportXMLButton;
    @FXML
    Label nicknameLabel;

    private static final ObservableList<AbstractChannel> rooms = FXCollections.observableArrayList();
    private static final ObservableList<AbstractChannel> conversations = FXCollections.observableArrayList();
    private static final ObservableList<User> participants = FXCollections.observableArrayList();
    private static final ObservableList<Message> messages = FXCollections.observableArrayList();

    private ReadOnlyObjectProperty currentRoom;

    private WebSocketController webSocketController;

    private User me = new User("me", new UUID(0,0));

    private static final ObservableList<User> users = FXCollections.observableArrayList();

    private static final Gson gson =
            new GsonBuilder().registerTypeAdapter(
                    ZonedDateTime.class, new ZDTSerializerDeserializer())
                    .create();

    private static final UUID adminChannelUUID = new UUID(0,0);

    private String nickname;


    /**
     * Mandatory function for JavaFX controllers
     */
    public void initialize() {

        // Set placeholder before initializing the lists
        showLoading();
        lockAll();

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
            participants.setAll(room.getSubscribers());
        });

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
        exportXMLButton.setOnAction(value -> exportToXML());
    }


    private void initializeLists() {
        // Fill the lists with fake data
        for (int i = 0; i < 7; i++) {
            participants.add(new User("Contact " + i, UUID.randomUUID()));

            conversations.add(new DirectMessageChannel(UUID.randomUUID(), Collections.emptyList(), Collections.emptySortedSet()));
            rooms.add(new PublicChannel(UUID.randomUUID(), new ArrayList<>(Arrays.asList(me)), Collections.emptySortedSet()));
            rooms.add(new PrivateChannel(UUID.randomUUID(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptySortedSet()));
        }

        // Default setting on start
        roomsList.getSelectionModel().select(0);
        roomsList.setPlaceholder(new Label(""));

        showLoaded();
    }


    private void lockMessages() {
        chatEntryField.setDisable(true);
        chatEntrySendButton.setDisable(true);
        //messagesList.setDisable(true);
    }


    private void unlockMessages() {
        chatEntryField.setDisable(false);
        chatEntrySendButton.setDisable(false);
        //messagesList.setDisable(false);
    }


    private void lockChannels() {
        addConvoButton.setDisable(true);
        addRoomButton.setDisable(true);
        //roomsList.setDisable(true);
        //conversationsList.setDisable(true);
    }


    private void unlockChannels() {
        addConvoButton.setDisable(false);
        addRoomButton.setDisable(false);
        //roomsList.setDisable(false);
        //conversationsList.setDisable(false);
    }


    private void lockAll() {
        inviteButton.setDisable(true);
        lockChannels();
        lockMessages();
    }


    private void unlockAll() {
        inviteButton.setDisable(false);
        unlockChannels();
        unlockMessages();
    }


    private void showLoading() {
        roomsList.setPlaceholder(new Label("Loading..."));
        conversationsList.setPlaceholder(new Label("Loading..."));
        contactsList.setPlaceholder(new Label("Loading..."));
        messagesList.setPlaceholder(new Label("Loading..."));

        lockAll();
    }


    private void showLoaded() {
        roomsList.setPlaceholder(new Label("No channel added yet"));
        conversationsList.setPlaceholder(new Label("No channel added yet"));
        contactsList.setPlaceholder(new Label("No contacts in this channel"));
        messagesList.setPlaceholder(new Label("Welcome to this channel. Start chatting to see the messages displayed here"));

        unlockAll();
    }


    private void sendTestMessage() {
        request("CHANNELLIST", "");
        request("CHANGENICKNAME", nickname);

        nicknameLabel.setText(nickname);
    }


    /**
     * Set the websocket controller and register the appropriate events
     * @param webSocketController The websocket controller
     */
    void setWebSocketController(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;
        this.webSocketController.registerMessageEvents(this);
    }



    void setNickname(String nickname) {
        this.nickname = nickname;
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
        System.err.println("In ChatWindowController: " + message);

        AbstractChannel room = (AbstractChannel)currentRoom.getValue();

        // Deserializing message
        Message receivedMessage = gson.fromJson(message, Message.class);

        // Check for admin message, else we'll guess it's a message
        if (receivedMessage.getDestinationUUID().equals(adminChannelUUID)) {

            // Admin Message recieved, inflating payload into AdminCommand
            AdminCommand payload = gson.fromJson(
                receivedMessage.getPayload(),
                AdminCommand.class
            );

            // If we receive the new connection message, the author UUID is the
            // client UUID given by the server. We can then initialize the user
            // and proceed with the rest of the session initialization
            // It is also a good place to put the requests to get all the available
            // data like the list of channels and informations about the user
            if (payload.getType().equals(AdminCommand.CommandType.CONNECT)) {
                me = new User("me", receivedMessage.getAuthorUUID());
                request("CHANNELLIST", "");
                request("CHANGENICKNAME", nickname);
                initializeLists();
            }

            // For the CHANNELLIST response, prolly the best place to fill
            // the channels lists
            if (payload.getType().equals(AdminCommand.CommandType.CHANNELLIST)) {
                System.out.println(payload.getCommandPayload());
            }

            // For the CHANGENICKNAME response, prolly the best place to set the nickname
            if (payload.getType().equals(AdminCommand.CommandType.CHANGENICKNAME)) {
                System.out.println(payload.getCommandPayload());
                nicknameLabel.setText("Here goes my new nickname stripped from the JSON");
            }

        } else {
            for (AbstractChannel channel:rooms) {
                System.out.println(channel.getChannelId());
                if (channel.getChannelId().equals(receivedMessage.getAuthorUUID())) {
                    System.out.println("Channel found in rooms !");
                }
            }
            for (AbstractChannel channel:conversations) {
                System.out.println(channel.getChannelId());
                if (channel.getChannelId().equals(receivedMessage.getAuthorUUID())) {
                    System.out.println("Channel found in conversations !");
                }
            }

            // Add the message and set the scrolling to the bottom
            messages.add(receivedMessage);
            room.appendMessage(receivedMessage);
            messagesList.scrollTo(messages.size());
        }
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


    /*************************************************************************
     * ONREQUEST EVENTS SHOULD BE HERE
     ************************************************************************/

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

  
    /*************************************************************************
     * THIS WHERE WE SHOULD MAKE THE REQUESTS TO THE SERVER
     ************************************************************************/


    /**
     * Make an request through the admin channel
     * @param request The type of the request, see AdminCommand.CommandType
     * @param argument The argument to pass to the request
     */
    private void request(String request, String argument) {

        // We build an admin command dumper with gson
        String adminCommand = gson.toJson(
                new AdminCommand(request, argument),
                AdminCommand.class
        );

        // We build a classic message with the admin command as payload
        Message adminMessageTest = new Message(
                this.me.getUuid(),
                adminChannelUUID,
                adminCommand,
                ZonedDateTime.now()
        );

        // And we send it
        String toSend = gson.toJson(adminMessageTest, Message.class);
        this.webSocketController.send(toSend);
    }
  
  
    public void exportToXML() {
        System.out.println("Exporting to XML");
          
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // root element
            Element rootElement = doc.createElement("messages");
            Attr channelUUID = doc.createAttribute("channelUUID");
            channelUUID.setValue(((AbstractChannel)currentRoom.getValue()).getChannelId().toString());
            rootElement.setAttributeNode(channelUUID);
            doc.appendChild(rootElement);

            for (Message message:messages) {
                Element messageEl = doc.createElement("message");
              
                Attr authorUUID = doc.createAttribute("authorUUID");
                Attr timestamp = doc.createAttribute("timestamp");
                authorUUID.setValue(message.getAuthorUUID().toString());
                timestamp.setValue(message.getTimestamp().toString());

                messageEl.setAttributeNode(authorUUID);
                messageEl.setAttributeNode(timestamp);
              
                messageEl.appendChild(doc.createTextNode(message.getPayload()));
                rootElement.appendChild(messageEl);
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File("./export.xml"));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

            // Output to console for testing
            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(source, consoleResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
