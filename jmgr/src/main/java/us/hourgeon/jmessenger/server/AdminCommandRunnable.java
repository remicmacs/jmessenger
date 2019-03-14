package us.hourgeon.jmessenger.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.WebSocket;
import us.hourgeon.jmessenger.AdminCommand;
import us.hourgeon.jmessenger.Model.*;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Command Administration runnable
 *
 * For command messages passed between client and server, we need a special
 * type of Runnable able to create new Channel, update existing ones, etc.
 */
public class AdminCommandRunnable implements Runnable {

    /**
     * Current server running
     *
     * Needed to send responses
     */
    private final ChatServer serverInstance;

    /**
     * Incoming admin message
     */
    private final Message adminMessage;

    /**
     * User requesting the admin command
     */
    private final User sender;

    /**
     * Gson serializer / deserializer
     */
    static private final Gson gson = new GsonBuilder().registerTypeAdapter(
            ZonedDateTime.class, new ZDTSerializerDeserializer()).create();

    /**
     * Connection on which server will send the response
     */
    private WebSocket connection = null;

    /**
     * Response to send by the server
     */
    private Message response = null;

    private AdminCommand adminCommand = null;

    /**
     * Constructor
     * @param adminMessage {@link AdminCommandRunnable#adminMessage}
     * @param serverInstance {@link AdminCommandRunnable#serverInstance}
     * @param sender {@link AdminCommandRunnable#sender}
     */
    AdminCommandRunnable(
        Message adminMessage,
        ChatServer serverInstance,
        User sender
    ) {
        this.serverInstance = serverInstance;
        this.adminMessage = adminMessage;
        this.sender = sender;
    }

    @Override
    public void run() {
        System.err.println("Admin task: start");

        System.err.println("Admin task: searching user Websocket");
        this.findUserWebsocket();

        System.err.println("Admin task: decoding command");
        this.decodeAdminMessage();

        if (this.connection != null) {
            this.connection.send(gson.toJson(this.response, Message.class));
            System.err.println("Admin task: Sent admin response");
        } else {
            System.err.println(
                    "Connection to user was closed between request and response"
            )   ;
        }
    }

    /**
     * Find the Websocket connection to the caller
     */
    // TODO: see if it's possible to do this with a Callable and a Future thingy
    private void findUserWebsocket() {
        // Finding websocket connection to sender
        for (WebSocket aConnection : this.serverInstance.getConnections()) {
            User attachedUser = aConnection.getAttachment();
            if (this.sender.getUuid().equals(attachedUser.getUuid())) {
                this.connection = aConnection;
            }
        }
    }

    /**
     * Takes an admin command an perform requested actions
     *
     * Find out which type of command has been received and collect data for
     * response.
     *
     * For now does not perform any external action because no side-effects
     * commands are implemented yet
     *
     * TODO: implement side-effects actions without breaking concurrency
     */
    private void decodeAdminMessage() {
        // Deserialize AdminCommand
        this.adminCommand = gson.fromJson(
            this.adminMessage.getPayload(),
            AdminCommand.class
        );

        // Command management
        String payload = "";
        String type;
        switch (this.adminCommand.getType()) {
            case USERLIST:
                payload = gson.toJson(this.getConnectedUsers());
                type ="USERLIST";
                break;
            case CONNECT:
                type = "CONNECT";
                break;
            case CHANNELLIST:
                payload = gson.toJson(this.getOpenChannels());
                type = "CHANNELLIST";
                break;
            case CREATECHANNEL:
                Channel newChannel = this.createChannel();
                payload = gson.toJson(newChannel, Channel.class);
                type = "CREATECHANNEL";
                break;
            case INVITEUSERS:
            case BANUSERS:
            case CHANGENICKNAME:
            case REQUESTHISTORY:
            default:
                type = "ERROR";
                payload = "NOT IMPLEMENTED";
                break;
        }

        createResponseMessage(type, payload);

    }

    /**
     * Create a new channel
     *
     * @return the newly created Channel
     */
    private Channel createChannel() {

        // Unpack CreateChannelRequest
        String payload = this.adminCommand.getCommandPayload();
        CreateChannelRequest ccr = gson.fromJson(
                payload,
                CreateChannelRequest.class
        );

        // Create the channel accordingly
        Channel serverChannel;
        if (!ccr.isPrivate() && !ccr.isDirect()) {
            ArrayList<User> initUser = new ArrayList<>();
            initUser.add(this.sender);

            serverChannel = new PublicRoom(
                initUser,
                ccr.getAlias()
            );

            Collection<User> invites = ccr.getInitSubscribers();
            this.sendInvites(invites);
        } else if (ccr.isPrivate()) {
            // TODO: change placeholder code
            ArrayList<User> initUser = new ArrayList<>();
            initUser.add(this.sender);
            serverChannel = new PublicRoom(
                    initUser,
                    ccr.getAlias()
            );
        } else { // newChannel instanceof DirectMessageConversation
            // TODO: check if DM channel does not exist yet
            ArrayList<User> initUser = new ArrayList<>();
            initUser.add(this.sender);
            serverChannel = new PublicRoom(
                    initUser,
                    ccr.getAlias()
            );
        }
        return serverChannel;
    }

    // TODO: Implement invite logic
    private void sendInvites(Collection<User> invites) {
        invites.forEach((User aUser)
            -> System.err.println("Sending an invite to " + aUser.getNickName())
        );
    }

    /**
     * Response message builder
     *
     * Because it's almost always the same thing anyway
     * @param commandType {@link AdminCommand.CommandType}
     * @param commandPayload see AdminCommand.commandPayload
     */
    private void createResponseMessage(String commandType,
                                       String commandPayload) {
        AdminCommand adminCommandResponse = new AdminCommand(
            commandType,
            commandPayload
        );

        this.response = new Message(
            this.adminMessage.getAuthorUUID(),
            new UUID(0,0),
            gson.toJson(adminCommandResponse, AdminCommand.class),
            ZonedDateTime.now()
        );
    }

    // Maybe use a Callable to avoid keeping a reference to the server in
    // other thread ?
    private List <User> getConnectedUsers() {
        return new ArrayList<>(this.serverInstance.getConnectedUsers());
    }

    // Construct a list of Channels accessible by the user who requested it
    private List<Channel> getOpenChannels() {

        // Recovering list of channel
        CopyOnWriteArraySet<Channel> channelList =
            this.serverInstance.getOpenChannels();

        return channelList.stream()
            // Make it a Channel stream
            .map(obj -> (Channel) obj)
            // Filter out Channel forbidden to user
            .filter(aChannel -> (aChannel instanceof PublicRoom) ||
                (aChannel instanceof PrivateRoom &&
                    ((PrivateRoom) aChannel)
                        .isAuthorized(this.sender)
                ) || (aChannel instanceof DirectMessageConversation &&
                    aChannel.getSubscribers().contains(this.sender)
                )
            )
            // Recreate Channel list without history to minimize network load
            .map(aChannel -> {
            Channel newCurrentChannel;
            if (aChannel instanceof PublicRoom) {
                newCurrentChannel = new PublicRoom(
                    aChannel.getChannelId(),
                    aChannel.getSubscribers(),
                    ((PublicRoom) aChannel).getAdministrators(),
                    ((PublicRoom) aChannel).getAlias()
                );
            } else if (aChannel instanceof PrivateRoom) {
                newCurrentChannel = new PrivateRoom(
                    aChannel.getChannelId(),
                    aChannel.getSubscribers(),
                    ((PrivateRoom) aChannel).getAuthorizedUsers(),
                    ((PrivateRoom) aChannel).getAdministrators()
                );
            } else { // aChannel instanceof DirectMessageConversation
                newCurrentChannel = new DirectMessageConversation(
                    aChannel.getChannelId(),
                    aChannel.getSubscribers()
                );
            }
            return newCurrentChannel;
        }).collect(Collectors.toList());
    }

}
