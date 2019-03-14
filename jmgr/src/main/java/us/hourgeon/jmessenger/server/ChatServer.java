package us.hourgeon.jmessenger.server;

import com.google.gson.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import us.hourgeon.jmessenger.AdminCommand;
import us.hourgeon.jmessenger.Model.*;

import java.net.InetSocketAddress;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Websocket server implementation for chat use case.
 *
 * This class is the server side entrypoint of all interaction between the
 * client and the server. It is an event-driven server, relying on the
 * Websocket application protocol. Websocket is a full-duplex protocol,
 * ensuring message transmission from end to end.
 */
public class ChatServer extends WebSocketServer {

    /**
     * Service used to execute tasks spawned by the server.
     */
    private final ExecutorService executor;

    private final int serverPortNumber;

    private final CopyOnWriteArraySet<Channel> openChannels =
        new CopyOnWriteArraySet<>();

    private final PublicRoom generalChannel;

    // Need a GsonBuilder ad hoc since Message uses ZonedDateTime
    // and it is not properly (de)serialized natively.
    // And Channel is a generic type so a custom deserializer is needed
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(ZonedDateTime.class, new ZDTAdapter())
        .registerTypeAdapter(Channel.class, new ChannelAdapter())
        .create();

    /**
     * Constructor
     *
     * Default values are used in this default constructor.
     *
     * Default port is set to 38887.
     *
     * Executor service instantiated is a CachedThreadPool
     */
    ChatServer() {
        this(38887, Executors.newCachedThreadPool());
    }

    /**
     * Constructor taking a port number
     *
     * The server will be available on the port number passed as parameter.
     *
     * @param port
     */
    public ChatServer( int port ) {
        this(port, Executors.newCachedThreadPool());
    }

    /**
     * Constructor taking a fully qualified address (with a port number)
     *
     * @param address
     */
    private ChatServer(InetSocketAddress address, ExecutorService executor) {
        super( address );
        this.serverPortNumber = address.getPort();
        this.executor = executor;

        // "General" is the only Room with no admin
        PublicRoom generalChannel = new PublicRoom(
            Collections.emptyList(),
            "General"
        );
        this.openChannels.add(generalChannel);
        this.generalChannel = generalChannel;
    }

    /**
     * Constructor taking a port and an instance of ExecutorService
     * @param port
     * @param executor
     */
    private ChatServer( int port, ExecutorService executor) {
        this(new InetSocketAddress(port), executor);
    }

    /**
     * Event handler for new connection
     *
     * Associates a client to a websocket.
     * @param conn
     * @param handshake
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        // Creating new User and
        // Attaching its information to websocket connection
        UUID newUUID = UUID.randomUUID();
        String newTemporaryNickname = "User#" + newUUID;
        User newUser = new User(newTemporaryNickname, newUUID);
        conn.setAttachment(newUser);

        // All users are subscribed to general and admin channel.
        // General channel is the shoutbox
        // Admin channel is a hidden channel for all commands messages
        this.generalChannel.subscribeUser(newUser);

        // TODO: remove temporary block when done with tests
        // Temporary : create test channel attached to new user
        ArrayList<User> users = new ArrayList<>();
        users.add(newUser);

        this.addChannel(new PrivateRoom(UUID.randomUUID(),users, users,
            users, "PrivateRoom accessible by new user"));

        /*this.addChannel(new DirectMessageConversation(UUID.randomUUID(),
                users));*/
        // End of temporary block

        // On connect, user does not send a "proper" message, so we build one
        AdminCommand connectAdminCommand = new AdminCommand(
            "CONNECT",
            ""
        );
        String adminMessagePayload = this.gson.toJson(
            connectAdminCommand,
            AdminCommand.class
        );
        Message newUserMessage = new Message(
            newUser.getUuid(),
            new UUID(0, 0),
            adminMessagePayload,
            ZonedDateTime.now()
        );

        // Executing admin command "CONNECT" == sending the new user its UUID
        this.submitTask(
            new AdminCommandRunnable(newUserMessage,this,conn)
        );

        // TODO: remove debug logging code
        System.err.println(
            "onOpen: "
                + newUser.getNickName()
                + " just arrived on the server"
        );
    }

    /**
     * Event handler for client disconnection
     *
     * Client is already gone when this event handler is called.
     * @param conn
     * @param code
     * @param reason
     * @param remote
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        User quittingUser = conn.getAttachment();
        Message closeMessage = new Message(
            quittingUser.getUuid(),
            new UUID(0,0),
            conn + " has left the room!",
            ZonedDateTime.now()
        );
        broadcast( this.gson.toJson(closeMessage, Message.class));

        this.getConnectedUsers().remove(quittingUser);
        System.out.println( conn + " has left the room!" );

    }

    /**
     * Event handler for incoming message
     *
     * When a message is received, this handler is called.
     * The client sending is identified thanks to the websocket.
     * @param conn
     * @param message
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        // Receive a Message
        Message inMessage = this.gson.fromJson(message, Message.class);

        // All messages with 0x0 destination UUID are admin messages
        if (inMessage.getDestinationUUID().equals(new UUID(0,0))) {
            this.submitTask(
                new AdminCommandRunnable(inMessage, this, conn)
            );
        } else {
            // For now messages are modified on arrival because client is not
            // yet aware of UUIDs attributed to channels
            Message udpdatedInMessage = new Message(
                inMessage.getAuthorUUID(),
                this.generalChannel.getChannelId(),
                inMessage.getPayload(),
                inMessage.getTimestamp()
            );
            this.submitTask(
                new PublishMessageRunnable(udpdatedInMessage, this)
            );
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }


    /**
     * Event handler for server instantiation.
     *
     * Some things are better done only in this method.
     */
    @Override
    public void onStart() {
        System.out.println(
            "Server started on port " + this.serverPortNumber + "\n"
        );
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);

        // TODO: remove temporary test private and dm channels
        // Add temporary private Channel and DMChannel with nobody in it
        this.addChannel(new PrivateRoom(
                Collections.emptyList(),
                Collections.emptyList(),
                "EmptyPrivateChannel"
            )
        );

        this.addChannel(new DirectMessageConversation(UUID.randomUUID(),
            Collections.emptySet()));

        // End of temporary block
    }

    void addChannel(Channel newChannel) {
        this.openChannels.add(newChannel);
    }

    CopyOnWriteArraySet<Channel> getOpenChannels() {
        return this.openChannels;
    }

    Set<User> getConnectedUsers() {
        return this.getConnections().stream()
            .map(obj -> (User) obj.getAttachment())
            .collect(Collectors.toSet());
    }

    PublicRoom getGeneralChannel() {
        return this.generalChannel;
    }

    // Destined to be public : other tasks will submit new runnables to
    // executor queue.
    private void submitTask(Runnable task) {
        this.executor.execute(task);
    }
}
