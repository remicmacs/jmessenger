package us.hourgeon.jmessenger.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.WebSocket;
import us.hourgeon.jmessenger.AdminCommand;
import us.hourgeon.jmessenger.Model.Message;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.Model.ZDTSerializerDeserializer;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        switch (receivedAdminCommand.getType()) {
            case USERLIST:
                String payload = gson.toJson(this.getConnectedUsers());
                createResponseMessage("USERLIST", payload);
                break;
            case CONNECT:
                createResponseMessage("CONNECT", "");
                break;
            case BANUSERS:
            case CHANNELLIST:
            case INVITEUSERS:
            case CREATECHANNEL:
            case CHANGENICKNAME:
            default:
                createResponseMessage("ERROR",
                        "How the fuck did you get here ?");
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

}
