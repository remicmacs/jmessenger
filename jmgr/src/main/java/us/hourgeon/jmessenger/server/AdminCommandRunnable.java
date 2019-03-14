package us.hourgeon.jmessenger.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.java_websocket.WebSocket;
import us.hourgeon.jmessenger.AdminCommand;
import us.hourgeon.jmessenger.Model.*;

import java.lang.reflect.Type;
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

    private final WebSocket connection;

    /**
     * Gson serializer / deserializer
     */
    static private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(ZonedDateTime.class, new ZDTAdapter())
        .registerTypeAdapter(Channel.class, new ChannelAdapter()).create();

    /**
     * Response to send by the server
     */
    private Message response = null;

    private AdminCommand adminCommand = null;

    /**
     * Constructor
     * @param adminMessage {@link AdminCommandRunnable#adminMessage}
     * @param serverInstance {@link AdminCommandRunnable#serverInstance}
     * @param connection
     */
    AdminCommandRunnable(
            Message adminMessage,
            ChatServer serverInstance,
            WebSocket connection) {
        this.serverInstance = serverInstance;
        this.adminMessage = adminMessage;
        this.sender = connection.getAttachment();
        this.connection = connection;
    }

    @Override
    public void run() {
        System.err.println("AdminCommand Start " + this.adminMessage.getPayload());
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
                type = "CHANNELLIST";
                Type channelListToken =
                    new TypeToken<ArrayList<Channel>>() {}.getType();
                payload =
                    gson.toJson(
                        new ArrayList<Channel>(
                            this.getAccessibleChannels()), channelListToken
                    );
                break;
            case CREATECHANNEL:
                Channel newChannel = this.createChannel();
                this.serverInstance.addChannel(newChannel);
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

        // TODO: Always map invites to actual users
        // Create the channel accordingly
        Channel serverChannel;
        if (!ccr.isPrivate() && !ccr.isDirect()) {

            // Create PublicRoom with User from request as sole admin
            ArrayList<User> initUser = new ArrayList<>();
            initUser.add(this.sender);
            serverChannel = new PublicRoom(
                initUser,
                ccr.getAlias()
            );

            // Send invites to initial list of Users
            Collection<User> invites = ccr.getInvites();
            this.sendInvites(invites);

        } else if (ccr.isPrivate()) {
            ArrayList<User> initUser = new ArrayList<>();
            initUser.add(this.sender);

            // Send invites to initial list of Users
            Collection<User> invites = ccr.getInvites();
            invites.add(this.sender);

            // Create PrivateRoom with the requester as admin and list of
            // authorized users.
            serverChannel = new PrivateRoom(
                invites,
                initUser,
                ccr.getAlias()
            );
            // Requester and admin are subscribed to the channel
            ((PrivateRoom) serverChannel).subscribeUser(this.sender);

            this.sendInvites(invites);

        } else { // newChannel instanceof DirectMessageConversation
            // Filter channels to find if DM Channel already exists
            CopyOnWriteArraySet<Channel> directMessageUsersGroups =
                this.serverInstance.getOpenChannels().stream()
                    .filter(channel -> channel instanceof DirectMessageConversation)
                    .collect(Collectors.toSet()).stream()
                    .filter(channel ->
                        (channel.getSubscribers().size() == ccr.getInvites().size())
                    )
                    .filter(
                        channel -> channel.getSubscribers()
                            .equals(new CopyOnWriteArraySet<>(ccr.getInvites()))
                    ).collect(Collectors.toCollection(CopyOnWriteArraySet::new)
                );

            // If a DM Channel was found with a subscriber set exactly the
            // same as the requested DM, return the current DM Channel
            if (directMessageUsersGroups.size() > 0) {
                serverChannel=
                    (DirectMessageConversation) directMessageUsersGroups.toArray()[0];
            } else {
                serverChannel = new DirectMessageConversation(
                    UUID.randomUUID(),
                    ccr.getInvites() // TODO: should be mapped to server users
                );

                this.sendInvites(ccr.getInvites());
            }
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
    private List<Channel> getAccessibleChannels() {

        // Recovering list of channel
        CopyOnWriteArraySet<Channel> channelList =
            this.serverInstance.getOpenChannels();

        return channelList.stream()
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
                    ((PrivateRoom) aChannel).getAdministrators(),
                    ((PrivateRoom) aChannel).getAlias()
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
