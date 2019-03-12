package us.hourgeon.jmessenger.server;

import com.google.gson.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import us.hourgeon.jmessenger.AdminCommand;
import us.hourgeon.jmessenger.Model.*;

import java.net.InetSocketAddress;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
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

    private final PublicChannel generalChannel;

    // Need a GsonBuilder ad hoc since Message uses ZonedDateTime
    // and it is not properly (de)serialized natively.
    private final Gson gson = new GsonBuilder().registerTypeAdapter(
            ZonedDateTime.class, new ZDTSerializerDeserializer()).create();



    /**
     * Constructor
     *
     * Default values are used in this default constructor.
     *
     * Default port is set to 38887.
     *
     * Executor service instantiated is a CachedThreadPool
     */
    public ChatServer() {
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
    public ChatServer( InetSocketAddress address ) {
        super( address );
        this.serverPortNumber = address.getPort();
        this.executor = Executors.newCachedThreadPool();
        UUID generalChannelUUID= UUID.randomUUID();
        PublicChannel generalChannel = new PublicChannel(generalChannelUUID);
        this.openChannels.add(generalChannel);
        this.generalChannel = generalChannel;
    }

    public ChatServer( int port, ExecutorService executor) {
        // TODO: check port value (not negative, not in well-known ports, etc)
        super( new InetSocketAddress( port ) );
        this.serverPortNumber = port;
        this.executor = executor;
        UUID generalChannelUUID= UUID.randomUUID();
        PublicChannel generalChannel = new PublicChannel(generalChannelUUID);
        this.openChannels.add(generalChannel);
        this.generalChannel = generalChannel;
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
        this.executor.execute(
            new AdminCommandRunnable(
                newUserMessage,
                this,
                conn.getAttachment()
            )
        );

        System.out.println(
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
            this.executor.execute(
                new AdminCommandRunnable(
                    inMessage,
                    this,
                    conn.getAttachment()
                )
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
            this.executor.execute(new PublishMessageRunnable(udpdatedInMessage, this));
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

    PublicChannel getGeneralChannel() {
        return this.generalChannel;
    }
}
