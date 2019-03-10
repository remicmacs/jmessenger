package us.hourgeon.jmessenger.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService executor;

    private int serverPortNumber = 38887;

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
     * @param executor {@link ChatServer#executor}
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
    }

    public ChatServer( int port, ExecutorService executor) {
        // TODO: check port value (not negative, not in well-known ports, etc)
        super( new InetSocketAddress( port ) );
        this.serverPortNumber = port;
        this.executor = executor;
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
        //This method sends a message to the new client only
        conn.send("Welcome to the server!");
        //This method sends a message to all clients connected
        broadcast( "new connection: " + handshake.getResourceDescriptor() );
        System.out.println(
                conn.getRemoteSocketAddress()
                        .getAddress()
                        .getHostAddress()
                        + " entered the room!"
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
        broadcast( conn + " has left the room!" );
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
        // Include thread instantiation for message handling here
        this.executor.execute(() -> {
                    this.broadcast(message);
                    System.out.println( conn + ": " + message );
                }
        );

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
}
