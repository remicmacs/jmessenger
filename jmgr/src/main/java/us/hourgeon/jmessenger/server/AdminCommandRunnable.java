package us.hourgeon.jmessenger.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import org.java_websocket.WebSocket;
import us.hourgeon.jmessenger.Model.AdminCommand;
import us.hourgeon.jmessenger.Model.*;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.*;
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

    /**
     * Command decoded by the runnable
     */
    private AdminCommand adminCommand = null;

    /**
     * Constructor
     * @param adminMessage {@link AdminCommandRunnable#adminMessage}
     * @param serverInstance {@link AdminCommandRunnable#serverInstance}
     * @param connection the Websocket connected to requesting User
     */
    AdminCommandRunnable(
        Message adminMessage,
        ChatServer serverInstance,
        WebSocket connection
    ) {

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
        String type = null;
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
                        new ArrayList<>(
                            this.getAccessibleChannels()), channelListToken
                    );
                break;
            case CREATECHANNEL:
                Channel newChannel = this.createChannel();
                this.serverInstance.addChannel(newChannel);
                payload = gson.toJson(newChannel, Channel.class);
                type = "CREATECHANNEL";
                this.serverInstance.broadcastChannelList();
                break;
            case CHANGENICKNAME:
                type = "CHANGENICKNAME";
                String newNick =
                    gson.fromJson(this.adminCommand.getCommandPayload(),
                        String.class);
                User updatedUser = new User(
                    newNick,
                    this.sender.getUuid()
                );
                boolean res = this.serverInstance.updateUser(updatedUser);
                payload = gson.toJson(res ? newNick : this.sender.getNickName());
                if (res) {
                    this.connection.setAttachment(updatedUser);
                    this.serverInstance.broadcastChannelList();
                }
                break;
            case HISTORY:
                type = "HISTORY";
                UUID historyChannelId =
                    gson.fromJson(this.adminCommand.getCommandPayload(),
                        UUID.class);
                ChannelHistory requestedHistory =
                    this.getChannelHistory(historyChannelId);

                payload = gson.toJson(requestedHistory, ChannelHistory.class);
                break;
            case JOIN:
                UUID joinChannelId =
                    gson.fromJson(this.adminCommand.getCommandPayload(),
                        UUID.class);

                AbstractChannel theChannel =
                    (AbstractChannel) this.serverInstance.getOpenChannels()
                        .get(joinChannelId);
                if (theChannel.subscribeUser(this.sender))
                    this.serverInstance.broadcastChannelList();

                break;
            case QUIT:
                UUID quitChannelId =
                    gson.fromJson(this.adminCommand.getCommandPayload(),
                        UUID.class);
                theChannel =
                    (AbstractChannel) this.serverInstance.getOpenChannels()
                        .get(quitChannelId);
                boolean quitSuccess = theChannel.unsubscribeUser(this.sender);
                if (quitSuccess) {
                    type = "QUIT";
                    payload = gson.toJson(theChannel, Channel.class);
                    this.serverInstance.broadcastChannelList();

                }
                break;
            case INVITEUSERS:
                Type payloadType =
                    new TypeToken<Pair<Channel, ArrayList<User>>>() {}.getType();
                Pair<AbstractChannel, ArrayList<User>> cmdPayload =
                    gson.fromJson(
                    this.adminCommand.getCommandPayload(),
                    payloadType
                );

                AbstractChannel invitingChannel =
                    (AbstractChannel) this.serverInstance.getOpenChannels()
                        .get(cmdPayload.getKey().getChannelId());
                ArrayList<User> usersToInvite = cmdPayload.getValue();

                usersToInvite.forEach(user -> {
                    if (invitingChannel instanceof PrivateRoom &&
                        ((PrivateRoom) invitingChannel).isAdmin(this.sender)
                    ) {
                        boolean success =
                            ((PrivateRoom) invitingChannel).authorizeUser(user);
                        if (success) {
                            System.err.println("Successfully added user " + user);
                        } else {
                            System.err.println("Unsuccessfully added user " + user);
                        }
                    }
                });

                List<WebSocket> connections =
                    new ArrayList<>(this.serverInstance.getConnections());

                connections.stream()
                    .filter(webSocket -> usersToInvite
                        .contains(webSocket.getAttachment())
                    )
                    .forEach(webSocket -> {
                        AdminCommand invite = new AdminCommand("INVITEUSERS",
                            gson.toJson(invitingChannel,
                                AbstractChannel.class));
                        Message inviteMessage =
                            new Message(
                                this.sender.getUuid(),
                                    new UUID(0, 0),
                                    gson.toJson(invite, AdminCommand.class),
                                    ZonedDateTime.now());
                        webSocket.send(gson.toJson(inviteMessage, Message.class));
                    });

                this.serverInstance.broadcastChannelList();

                break;
            case BANUSERS:
            default:
                type = "ERROR";
                payload = "NOT IMPLEMENTED";
                break;
        }

        if (type != null) createResponseMessage(type, payload);
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
            serverChannel.subscribeUser(this.sender);

            this.sendInvites(invites);

        } else { // newChannel instanceof DirectMessageConversation
            // Filter channels to find if DM Channel already exists
            Set<Channel> directMessageUsersGroups =
                this.serverInstance.getOpenChannels().values().stream()
                    .filter(channel -> {
                        System.err.println(channel);
                        return (channel instanceof DirectMessageConversation);
                    })
                    .filter(channel -> {
                        System.err.println("DM : " + channel);
                        return (channel.getSubscribers().size() == ccr.getInvites().size()+1);
                    })
                    .filter(
                        channel -> {
                            System.err.println("Candidates DM: " + channel);
                            TreeSet<User> subscribers =
                                new TreeSet<>(ccr.getInvites());
                            subscribers.add(this.sender);
                            boolean isActualDmMessage = channel.getSubscribers()
                                    .equals(subscribers);
                            return isActualDmMessage;
                        }
                    ).collect(Collectors.toCollection(TreeSet::new)
                );

            // If a DM Channel was found with a subscriber set exactly the
            // same as the requested DM, return the current DM Channel
            if (directMessageUsersGroups.size() > 0) {
                serverChannel=
                    (DirectMessageConversation) directMessageUsersGroups.toArray()[0];
            } else {
                // TODO: should be mapped to
                // server
                // users
                ArrayList<User> dmusers = (ArrayList<User>) ccr.getInvites();
                dmusers.add(this.sender);
                serverChannel = new DirectMessageConversation(
                    UUID.randomUUID(),
                    dmusers
                );

                this.sendInvites(dmusers);
            }
        }

        return serverChannel;
    }

    // TODO: Implement invite logic
    private void sendInvites(Collection<User> invites) {
        invites.forEach((User aUser)
            -> System.err.println("Sending an invite to " + aUser)
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
        Set<Channel> channelList =
            new TreeSet<>(
                this.serverInstance.getOpenChannels().values()
            );

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

    private ChannelHistory getChannelHistory(UUID channelId) {
        return this.serverInstance
            .getOpenChannels().get(channelId).getHistory();
    }
}
