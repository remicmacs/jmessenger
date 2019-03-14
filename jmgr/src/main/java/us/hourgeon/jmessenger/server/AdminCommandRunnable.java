package us.hourgeon.jmessenger.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.WebSocket;
import us.hourgeon.jmessenger.AdminCommand;
import us.hourgeon.jmessenger.Model.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
        AdminCommand receivedAdminCommand = gson.fromJson(
            this.adminMessage.getPayload(),
            AdminCommand.class
        );

        // Command management
        String payload;
        switch (receivedAdminCommand.getType()) {
            case USERLIST:
                payload = gson.toJson(this.getConnectedUsers());
                createResponseMessage("USERLIST", payload);
                break;
            case CONNECT:
                createResponseMessage("CONNECT", "");
                break;
            case BANUSERS:
            case CHANNELLIST:
                payload = gson.toJson(this.getOpenChannels());
                createResponseMessage("CHANNELLIST", payload);
                break;
            case INVITEUSERS:
            case CREATECHANNEL:
            case CHANGENICKNAME:
            default:
                payload = "How the fuck did you get here ?";
                createResponseMessage("ERROR", payload);
                break;
        }

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
            .filter(aChannel -> (aChannel instanceof PublicChannel) ||
                (aChannel instanceof PrivateChannel &&
                    ((PrivateChannel) aChannel)
                        .getAuthorizedUsers().contains(this.sender)
                ) || (aChannel instanceof DirectMessageChannel &&
                    aChannel.getSubscribers().contains(this.sender)
                )
            )
            // Recreate Channel list without history to minimize network load
            .map(aChannel -> {
            Channel newCurrentChannel;
            if (aChannel instanceof PublicChannel) {
                newCurrentChannel = new PublicChannel(
                    aChannel.getChannelId(),
                    aChannel.getSubscribers(),
                    Collections.emptySortedSet()
                );
            } else if (aChannel instanceof PrivateChannel) {
                newCurrentChannel = new PrivateChannel(
                    aChannel.getChannelId(),
                    aChannel.getSubscribers(),
                    ((PrivateChannel) aChannel).getAuthorizedUsers(),
                    ((PrivateChannel) aChannel).getAdministrators(),
                    Collections.emptySortedSet()
                );
            } else { // aChannel instanceof DirectMessageChannel
                newCurrentChannel = new DirectMessageChannel(
                    aChannel.getChannelId(),
                    aChannel.getSubscribers(),
                    Collections.emptySortedSet()
                );
            }
            return newCurrentChannel;
        }).collect(Collectors.toList());
    }

}
