package us.hourgeon.jmessenger.client;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
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

import javafx.util.Pair;
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


/**
 * This class implements all of the logic for the main interfaces, including
 * the management of the websocket and the messages
 */
public class ChatWindowController implements MessageEvents, ChannelEvents, ContactEvents {

    @FXML
    ListView<AbstractChannel> roomsList;
    @FXML
    ListView<AbstractChannel> conversationsList;
    @FXML
    ListView<User> contactsList;
    @FXML
    ListView<Message> messagesList;
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
    @FXML
    Button quitButton;

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
    private final Clipboard clipboard = Clipboard.getSystemClipboard();
    private final ClipboardContent content = new ClipboardContent();

    private ApplicationEvents applicationEvents;

    private boolean filterUsers = false;
    private Channel newlyAddedChannel;


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

        // Add a listener to a selection change that will update the view
        first.selectedItemProperty().addListener((observableValue, old, neww) -> updateView());

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

        // Bind the buttons
        chatEntrySendButton.setOnAction(value -> send());
        addRoomButton.setOnAction(value -> openAddChannelDialog(false));
        addConvoButton.setOnAction(value -> openAddChannelDialog(true));
        joinRoomButton.setOnAction(value -> openJoinChannelDialog());
        inviteButton.setOnAction(value -> openInviteDialog());
        exportXMLButton.setOnAction(value -> exportToXML());
        quitButton.setOnAction(value -> applicationEvents.onDisconnect());

        // Bind the nickname label
        copyNickname.setOnAction(value -> {
            content.putString(me.getUuid().toString());
            clipboard.setContent(content);
        });
        nicknameLabel.setOnContextMenuRequested(event -> nicknameMenu.show(nicknameLabel, event.getScreenX(), event.getScreenY()));
    }


    /*
     * UTILS FUNCTIONS HERE
     ************************************************************************/


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
     * Get a channel based on it's uuid
     * @param uuid The uuid for the channel we're searching
     * @return The AbstractChannel we were searching for
     */
    private AbstractChannel getChannel(UUID uuid) {
        if (rooms.stream().map(AbstractChannel::getChannelId)
                .collect(Collectors.toList())
                .contains(uuid)) {
            return rooms.stream()
                    .filter(abstractChannel -> abstractChannel.getChannelId().equals(uuid))
                    .findFirst()
                    .orElse(null);
        } else if (conversations.stream().map(AbstractChannel::getChannelId)
                .collect(Collectors.toList())
                .contains(uuid)) {
            return conversations.stream()
                    .filter(abstractChannel -> abstractChannel.getChannelId().equals(uuid))
                    .findFirst()
                    .orElse(null);
        } else {
            System.err.println("Channel not found, gasp !");
            return null;
        }
    }


    /*
     * UI FUNCTIONS HERE
     ************************************************************************/


    /**
     * Update the whole view.
     */
    private void updateView() {
        AbstractChannel room = (AbstractChannel)currentRoom.getValue();
        updateChannelLabel();
        updateXMLExportVisibility();
        updateInviteButtonVisibility();

        messages.setAll(room.getHistory().getMessages());
        participants.setAll(room.getSubscribers());

        // Reset the cell factories to adapt their views to the new data
        contactsList.setCellFactory(new ContactCellFactory(
            this, isAdmin(), room)
        );
        messagesList.setCellFactory(new MessageCellFactory(participants));

        request("HISTORY", room.getChannelId().toString());
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


    /**
     * Update the visibility of the invite button.
     * In a private room, if the user is admin, the button should be
     * showing. In any other case, it should be hidden
     */
    private void updateInviteButtonVisibility() {
        Channel channel = (Channel)currentRoom.getValue();
        if (channel instanceof PrivateRoom) {
            inviteButton.setVisible(((AbstractRoom) channel).isAdmin(me));
            inviteButton.setManaged(((AbstractRoom) channel).isAdmin(me));
        } else {
            inviteButton.setVisible(false);
            inviteButton.setManaged(false);
        }
    }


    /**
     * Lock the text entry to send messages
     */
    private void lockMessages() {
        chatEntryField.setDisable(true);
        chatEntrySendButton.setDisable(true);
    }


    /**
     * Unlock the text entry to send messages
     */
    private void unlockMessages() {
        chatEntryField.setDisable(false);
        chatEntrySendButton.setDisable(false);
    }


    /**
     * Lock the channel creation
     */
    private void lockChannels() {
        addConvoButton.setDisable(true);
        addRoomButton.setDisable(true);
    }


    /**
     * Unlock the channel creation
     */
    private void unlockChannels() {
        addConvoButton.setDisable(false);
        addRoomButton.setDisable(false);
    }


    /**
     * Lock all the UI
     */
    private void lockAll() {
        inviteButton.setDisable(true);
        lockChannels();
        lockMessages();
    }


    /**
     * Unlock all the UI
     */
    private void unlockAll() {
        inviteButton.setDisable(false);
        unlockChannels();
        unlockMessages();
    }


    /**
     * Set all the placeholder in the lists while loading data
     */
    private void showLoading() {
        roomsList.setPlaceholder(new Label("Loading..."));
        conversationsList.setPlaceholder(new Label("Loading..."));
        contactsList.setPlaceholder(new Label("Loading..."));
        messagesList.setPlaceholder(new Label("Loading..."));

        lockAll();
    }


    /**
     * Change the placeholders and unlock the UI
     */
    private void showLoaded() {
        roomsList.setPlaceholder(new Label("No channel added yet"));
        conversationsList.setPlaceholder(new Label("No channel added yet"));
        contactsList.setPlaceholder(new Label("No contacts in this channel"));
        messagesList.setPlaceholder(new Label("Welcome to this channel. Start chatting to see the messages displayed here"));

        unlockAll();
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
     * Set the ApplicationEvents (quit, open etc..). Those will be triggered
     * on some button press
     * @param events The object implementing ApplicationEvents
     */
    void setApplicationEvents(ApplicationEvents events) {
        applicationEvents = events;
    }


    /**
     * Set the nickname, mainly used by the calling scene.
     * @param nickname The new nickname
     */
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
            if (wsMessage.getDestinationUUID().equals(new UUID(0, 0)))
                System.err.println("=== Sending: " + toSend + "\n");
            this.webSocketController.send(toSend);
        }
    }


    /**
     * Executed when a message is received, this is were we implement parsing
     * and treating the message.
     * @param message The message received
     */
    @Override
    public void onMessage(String message) {
        AbstractChannel room = (AbstractChannel)currentRoom.getValue();

        System.err.println(message);

        // Deserializing message
        Message receivedMessage = gson.fromJson(message, Message.class);

        if (receivedMessage == null) {
            return;
        }

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
                me = new User(nickname, receivedMessage.getAuthorUUID());
                nicknameLabel.setText("User#" + receivedMessage.getAuthorUUID());
                roomsList.setCellFactory(new ChannelCellFactory(this, true, me));
                conversationsList.setCellFactory(new ChannelCellFactory(this, true, me));
                request("CHANGENICKNAME", nickname);
                request("CHANNELLIST", "");
                request("USERLIST", "");
                showLoaded();

            } else if (payload.getType().equals(AdminCommand.CommandType.QUIT)) {

                roomsList.getSelectionModel().select(0);

            } else if (
                payload.getType().equals(AdminCommand.CommandType.CHANNELLIST)
            ) {

                String cmdPayload = payload.getCommandPayload();
                Type channelListToken =
                    new TypeToken<ArrayList<Channel>>() {}.getType();
                ArrayList<Channel> channels = gson.fromJson(cmdPayload,
                    channelListToken);

                // We clear the current lists of channels
                channels.forEach(System.err::println);
                rooms.clear();
                conversations.clear();

                // We add the received channels depending on some condition
                // We also sets the participant for the current channel
                channels
                    .stream().filter(channel -> channel.isSubscribed(me))
                    .forEach(channel ->  {
                        if (channel instanceof AbstractRoom) {
                            rooms.add((AbstractChannel) channel);
                        } else {
                            conversations.add((AbstractChannel) channel);
                        }

                        if(channel.equals(room)) {
                            participants.setAll(channel.getSubscribers());
                        }
                    });

                // We fill the disponible channels, filtering the one we're
                // already in
                ChatWindowController.channels.setAll(channels.stream()
                        .map(channel -> ((AbstractChannel) channel))
                        .filter(channel -> !channel.isSubscribed(me))
                        .collect(Collectors.toList()));

                // If no channel is selected yet, we select one now
                if (currentRoom.getValue() == null && rooms.size() > 0) {
                    roomsList.getSelectionModel().select(0);
                }

                // If we were waiting for the addition of a new room, we set
                // the current room to this one
                if (newlyAddedChannel != null) {
                    if (newlyAddedChannel instanceof AbstractRoom) {
                        roomsList.getSelectionModel().select((AbstractChannel)newlyAddedChannel);
                    } else {
                        conversationsList.getSelectionModel().select((AbstractChannel)newlyAddedChannel);
                    }
                    newlyAddedChannel = null;
                }
            } else if (payload.getType().equals(AdminCommand.CommandType.CHANGENICKNAME)) { // Handles CHANGENICKNAME

                // For the CHANGENICKNAME response, prolly the best place to set the nickname
                String newNickname =
                    gson.fromJson(payload.getCommandPayload(), String.class);
                System.out.println(newNickname);
                nicknameLabel.setText(newNickname);
                this.me = new User(newNickname, this.me.getUuid());

            } else if (

                payload.getType().equals(AdminCommand.CommandType.CREATECHANNEL)

            ) { // Handles CREATECHANNEL

                String cmdPayload = payload.getCommandPayload();
                newlyAddedChannel = gson.fromJson(cmdPayload, Channel.class);

                System.err.println("Newly added channel:: " + newlyAddedChannel);

                request("CHANNELLIST", "");

            } else if (
                payload.getType().equals(AdminCommand.CommandType.USERLIST)
            ) { // Handles USERLIST

                String cmdPayload = payload.getCommandPayload();
                Type channelListToken =
                        new TypeToken<ArrayList<User>>() {}.getType();
                ArrayList<User> users = gson.fromJson(cmdPayload,
                        channelListToken);

                // If needed, we filter already authorized users
                if (filterUsers) {
                    ChatWindowController.users.setAll(users.stream()
                            .filter(user -> room != null)
                            .filter(user -> !room.isSubscribed(user))
                            .collect(Collectors.toList()));
                } else {
                    ChatWindowController.users.setAll(users.stream()
                            .filter(user -> room != null)
                            .filter(user -> !user.equals(me))
                            .collect(Collectors.toList()));
                }

            } else if (payload.getType().equals(AdminCommand.CommandType.HISTORY)) {        // Handles HISTORY

                String cmdPayload = payload.getCommandPayload();
                ChannelHistory channelHistory =
                    gson.fromJson(cmdPayload, ChannelHistory.class);

                UUID uuid = channelHistory.getChannelUUID();

                // Adding the messages
                AbstractChannel concernedChannel = getChannel(uuid);
                if (concernedChannel != null) {
                    channelHistory.getMessages().forEach(concernedChannel::appendMessage);

                    // Updating the messages list view
                    if (uuid.equals(room.getChannelId())) {
                        messages.setAll(concernedChannel.getHistory().getMessages());
                    }
                }

                messagesList.scrollTo(messages.size());
            } else if (payload.getType().equals(AdminCommand.CommandType.INVITEUSERS)) {
                System.err.println("\n\n\tInvite received :: " + message +
                        "\n\n");
            }

        } else {

            // NOT TESTED
            /*AbstractChannel channel = getChannel(receivedMessage.getDestinationUUID());
            if (channel instanceof DirectMessageConversation) {
                if (!conversations.contains(channel)) {
                    conversations.add(channel);
                }
            }*/

            // If the receiving channel is the currently displayed channel
            if (room.getChannelId().equals(receivedMessage.getDestinationUUID())) {
                // Add the message and set the scrolling to the bottom
                messages.add(receivedMessage);
                room.appendMessage(receivedMessage);
                messagesList.scrollTo(messages.size());
            }
        }
    }


    /*
     * OPENING DIALOGS HERE
     ************************************************************************/


    /**
     * Opens an "Add channel" dialog.
     * @param isDirect True if the channel should be a conversation
     */
    private void openAddChannelDialog(boolean isDirect) {
        request("USERLIST", "");
        filterUsers = false;
        String title = isDirect ? "Add a conversation" : "Add a room";

        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("addroomdialog.fxml"));
        try {
            Parent root = loader.load();
            AddChannelDialogController controller = loader.getController();

            dialog.setTitle(title);
            dialog.setScene(new Scene(root, 400, 250));
            dialog.show();

            if (isDirect) {
                controller.setDirect();
            } else {
                controller.setRoom();
            }
            controller.setChannelEvents(this);
            controller.setUsers(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Opens the dialog to invite people in the current channel
     */
    private void openInviteDialog() {
        request("USERLIST", "");
        filterUsers = true;
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


    /**
     * Opens the dialog to join a channel
     */
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


    /*
     * ONREQUEST EVENTS SHOULD BE HERE
     ************************************************************************/

    /**
     * Triggered when the user requests to quit a channel
     * @param uuid The UUID of the channel
     */
    @Override
    public void onQuitRequest(UUID uuid) {
        System.out.println("Request quitting channel " + uuid.toString());
        request("QUIT", uuid.toString());
    }


    /**
     * Triggered when the user request a channel creation
     * @param name The name of the channel
     * @param invites A list of User to add to the authorized list for the new channel
     * @param isDirect True if the channel is a conversation
     * @param isPrivate True if the channel is private
     */
    @Override
    public void onCreateRequest(String name, ArrayList<User> invites, boolean isDirect, boolean isPrivate) {
        String invitesStr = invites.stream()
                .map(User::getNickName)
                .collect(Collectors.joining("\n"));
        System.out.println("Create channel !");
        System.out.println("Name : " + name);
        System.out.println("Invites : " + invitesStr);
        System.out.println("Is direct : " + isDirect);
        System.out.println("Is private : " + isPrivate);

        // Create a request object
        CreateChannelRequest ccr = new CreateChannelRequest(invites,
                name, isPrivate, isDirect);

        // Serialize it
        String adminCommandPayload = gson.toJson(ccr, CreateChannelRequest.class);

        // Request creation
        request("CREATECHANNEL", adminCommandPayload);
    }


    /**
     * Triggered when the user wants to join a channel
     * @param uuid The UUID of the channel to join
     */
    @Override
    public void onJoinRequest(UUID uuid) {
        System.out.println("Request joining " + uuid.toString());
        request("JOIN", uuid.toString());
    }


    /**
     * Triggers when the user wants to invite users
     * @param users A list of User to invite
     * @param channel The channel where the users should be invited
     */
    @Override
    public void onInvitesRequest(ArrayList<User> users, AbstractChannel channel) {
        System.out.println("Invites request: ");
        for (User user:users) {
            System.out.println(user.getUuid());
        }

        Type channelListToken =
            new TypeToken<Pair<Channel, ArrayList<User>>>() {}.getType();
        Pair<Channel, ArrayList<User>> pair = new Pair<>(channel, users);
        String cmdPayload = gson.toJson(pair, channelListToken);
        request("INVITEUSERS", cmdPayload);
    }


    /**
     * Triggered when the admin wants to ban an user
     * @param user The user to ban
     */
    @Override
    public void onBanRequest(User user) {
        AbstractChannel channel = (AbstractChannel)currentRoom.getValue();
        System.out.println("Request banning " + user.toString() + " from " + channel.getChannelId().toString());
    }


    /**
     * Triggered when the admin wants to promote an user
     * @param user The user to promote
     */
    @Override
    public void onPromoteRequest(User user) {
        AbstractChannel channel = (AbstractChannel)currentRoom.getValue();
        System.out.println("Request promoting " + user.toString() + " from " + channel.getChannelId().toString());
    }

  
    /*
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


    /**
     * Export the current room to XML
     */
    private void exportToXML() {
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
