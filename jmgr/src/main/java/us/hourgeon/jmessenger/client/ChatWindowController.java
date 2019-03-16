package us.hourgeon.jmessenger.client;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;

import us.hourgeon.jmessenger.Model.AdminCommand;
import us.hourgeon.jmessenger.client.ChannelCell.ChannelCellFactory;
import us.hourgeon.jmessenger.client.ContactCell.ContactCellFactory;
import us.hourgeon.jmessenger.client.MessageCell.MessageCellFactory;
import us.hourgeon.jmessenger.Model.*;

import java.io.IOException;
import java.lang.reflect.Type;
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
import java.util.stream.Collectors;

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
    @FXML
    Button joinRoomButton;

    private static final ObservableList<AbstractChannel> rooms = FXCollections.observableArrayList();
    private static final ObservableList<AbstractChannel> conversations = FXCollections.observableArrayList();
    private static final ObservableList<User> participants = FXCollections.observableArrayList();
    private static final ObservableList<Message> messages = FXCollections.observableArrayList();

    private ReadOnlyObjectProperty currentRoom;

    private WebSocketController webSocketController;

    private User me = new User("me", new UUID(0,0));

    private static final ObservableList<User> users = FXCollections.observableArrayList();
    private static final ObservableList<AbstractChannel> channels = FXCollections.observableArrayList();

    private static final Gson gson =
        new GsonBuilder().registerTypeAdapter(
            ZonedDateTime.class, new ZDTAdapter())
            .registerTypeAdapter(Channel.class, new ChannelAdapter())
            .create();

    private static final UUID adminChannelUUID = new UUID(0,0);

    private String nickname;
    final Clipboard clipboard = Clipboard.getSystemClipboard();
    final ClipboardContent content = new ClipboardContent();


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
        messagesList.setCellFactory(new MessageCellFactory(users));
        contactsList.setCellFactory(new ContactCellFactory(this));
        roomsList.setCellFactory(new ChannelCellFactory(this));
        conversationsList.setCellFactory(new ChannelCellFactory(this));

        // We set the height of the roomsList as the number of rooms times the height of a row
        roomsList.prefHeightProperty().bind(Bindings.size(rooms).multiply(32).add(0));

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

        ContextMenu nicknameMenu = new ContextMenu();
        MenuItem copyNickname = new MenuItem("Copy UUID in clipboard");
        nicknameMenu.getItems().setAll(copyNickname);

        // Add a listener to a selection change (just for testing now)
        first.selectedItemProperty().addListener((observableValue, old, neww) -> {
            // Here must be implemented any events following the selection of a room
            // Ideally, everything should be bound but if it is not, this is were we manually
            // set the current room and chat window content and stuff like that
            AbstractChannel room = (AbstractChannel)currentRoom.getValue();
            updateChannelLabel();
            updateXMLExportVisibility();

            messages.setAll(room.getHistory().getMessages());
            participants.setAll(room.getSubscribers());

            // Reset the cell factories to adapt their views to the new data
            contactsList.setCellFactory(new ContactCellFactory(this, isAdmin(), (AbstractRoom)room));
            messagesList.setCellFactory(new MessageCellFactory(participants));
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
        joinRoomButton.setOnAction(value -> openJoinChannelDialog());
        inviteButton.setOnAction(value -> openInviteDialog());
        testButton.setOnAction(value -> sendTestMessage());
        exportXMLButton.setOnAction(value -> exportToXML());

        copyNickname.setOnAction(value -> {
            content.putString(me.getUuid().toString());
            clipboard.setContent(content);
        });
        nicknameLabel.setOnContextMenuRequested(event -> nicknameMenu.show(nicknameLabel, event.getScreenX(), event.getScreenY()));
    }


    private void initializeLists() {
        // Fill the lists with fake data
        for (int i = 0; i < 7; i++) {
            //conversations.add(new DirectMessageConversation(UUID.randomUUID(), Collections.emptyList()));
        }

        showLoaded();
    }


    /**
     * Check if the user is the current room's admin. In case of a direct messages
     * conversation, this will return false.
     * @return True if the user is the room's admin, False otherwise
     */
    private boolean isAdmin() {
        Channel channel = (Channel)currentRoom.getValue();
        if (channel instanceof AbstractRoom) {
            return ((AbstractRoom) channel).isAdmin(me);
        } else {
            return false;
        }
    }


    /**
     * Update the channel label. If it is a room, the channel's alias is taken.
     * Otherwise, it will build a string made from the name of the participants
     */
    private void updateChannelLabel() {
        Channel channel = (Channel)currentRoom.getValue();
        if (channel instanceof AbstractRoom) {
            roomLabel.setText(((AbstractRoom) channel).getAlias());
        } else {
            String title = channel.getSubscribers().stream()
                    .filter(user -> !user.equals(me))
                    .map(User::getNickName)
                    .collect(Collectors.joining(", "));

            roomLabel.setText(title);
        }
    }


    /**
     * Update the XML export button's visibility.
     * If the user is the admin of the current channel or if the current
     * channel is a direct message conversation, it will show the export
     * button
     */
    private void updateXMLExportVisibility() {
        Channel channel = (Channel)currentRoom.getValue();
        if (channel instanceof AbstractRoom) {
            exportXMLButton.setVisible(((AbstractRoom) channel).isAdmin(me));
        } else {
            exportXMLButton.setVisible(true);
        }
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
        request("HISTORY",
            ((AbstractChannel) this.currentRoom.get()).getChannelId().toString()
        );
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
            System.err.println("=== Sending: " + toSend + "\n");
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
        // TODO: Remove this annoying dump whenever possible ffs
        System.err.println("=== Received: " + message + "\n");

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
                System.err.println("User new UUID : " + receivedMessage.getAuthorUUID());
                me = new User(nickname, receivedMessage.getAuthorUUID());
                nicknameLabel.setText("User#" + receivedMessage.getAuthorUUID());
                roomsList.setCellFactory(new ChannelCellFactory(this, true, me));
                conversationsList.setCellFactory(new ChannelCellFactory(this, true, me));
                request("CHANNELLIST", "");
                request("USERLIST", "");
                request("CHANGENICKNAME", nickname);
                initializeLists();
            } else if (
                payload.getType().equals(AdminCommand.CommandType.CHANNELLIST)
            ) {
                // For the CHANNELLIST response, prolly the best place to fill
                // the channels lists
                String cmdPayload = payload.getCommandPayload();
                Type channelListToken =
                    new TypeToken<ArrayList<Channel>>() {}.getType();
                ArrayList<Channel> channels = gson.fromJson(cmdPayload,
                    channelListToken);

                rooms.clear();

                for (Channel channel:channels) {
                    for (User user:channel.getSubscribers()) {
                        if (user.equals(me)) {
                            rooms.add((AbstractChannel)channel);
                        }
                    }
                }

                ArrayList<AbstractChannel> abstractChannels = new ArrayList<>(
                    channels.stream()
                        .map(channel -> ((AbstractChannel)channel))
                        .collect(Collectors.toList())
                );

                channels.forEach(System.err::println);

                this.channels.setAll(abstractChannels);

                if (currentRoom.getValue() == null) {
                    roomsList.getSelectionModel().select(0);
                }
            } else if (payload.getType().equals(AdminCommand.CommandType.CHANGENICKNAME)) {
                // For the CHANGENICKNAME response, prolly the best place to set the nickname
                String newNickname =
                    gson.fromJson(payload.getCommandPayload(), String.class);
                System.out.println(newNickname);
                nicknameLabel.setText(newNickname);
                this.me = new User(newNickname, this.me.getUuid());
            } else if (payload.getType().equals(AdminCommand.CommandType.CREATECHANNEL)) {
                String cmdPayload = payload.getCommandPayload();
                Channel newlyAddedChannel =
                    gson.fromJson(cmdPayload, Channel.class);

                System.err.println("Newly added channel:: " + newlyAddedChannel);
                rooms.add((AbstractChannel)newlyAddedChannel);
            } else if (payload.getType().equals(AdminCommand.CommandType.USERLIST)) {
                String cmdPayload = payload.getCommandPayload();
                Type channelListToken =
                        new TypeToken<ArrayList<User>>() {}.getType();
                ArrayList<User> users = gson.fromJson(cmdPayload,
                        channelListToken);
                this.users.setAll(users);
            }

        } else {
            // TODO: store correct UUIDs
            // For now does not work because list of channel client-side does
            // not have correct UUIDs
            if (rooms.stream().map(AbstractChannel::getChannelId)
                .collect(Collectors.toList())
                .contains(receivedMessage.getDestinationUUID())) {
                System.err.println("Channel found in rooms !");
            } else if (conversations.stream().map(AbstractChannel::getChannelId)
                .collect(Collectors.toList())
                .contains(receivedMessage.getDestinationUUID())) {
                System.err.println("Channel found in rooms !");
            } else {
                System.err.println("Channel not found, gasp !");
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


    /*************************************************************************
     * OPENING DIALOGS HERE
     ************************************************************************/


    private void openInviteDialog() {
        request("USERLIST", "");
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
            ((InviteDialogController)loader.getController()).setUsers(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void openJoinChannelDialog() {
        request("CHANNELLIST", "");
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("joindialog.fxml"));
        try {
            Parent root = loader.load();

            dialog.setTitle("Join a room");
            dialog.setScene(new Scene(root, 400, 250));
            dialog.show();

            ((JoinDialogController)loader.getController()).setEvents(this);
            ((JoinDialogController)loader.getController()).setChannelsList(channels);
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

        // Initialize the list of users
        ArrayList<User> initialUsers = new ArrayList<>();
        initialUsers.add(this.me);

        // Create a request object
        CreateChannelRequest ccr = new CreateChannelRequest(initialUsers,
                name, isPrivate, isDirect);

        // Serialize it
        String adminCommandPayload = gson.toJson(ccr, CreateChannelRequest.class);

        // Request creation
        request("CREATECHANNEL", adminCommandPayload);
    }

    @Override
    public void onJoinRequest(UUID uuid) {
        System.out.println("Request joining " + uuid.toString());
        request("JOIN", uuid.toString());
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
