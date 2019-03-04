package us.hourgeon.jmessenger.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Main {
    static class ChatServer extends WebSocketServer {
        ExecutorService executor;

        public ChatServer( int port, ExecutorService executor ) {
            super( new InetSocketAddress( port ) );
            this.executor = executor;
        }

        public ChatServer( InetSocketAddress address, ExecutorService executor ) {
            super( address );
            this.executor = executor;
        }

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

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            broadcast( conn + " has left the room!" );
            System.out.println( conn + " has left the room!" );

        }

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

        @Override
        public void onStart() {
            System.out.println("Server started!");
            setConnectionLostTimeout(0);
            setConnectionLostTimeout(100);
        }
    };
    public static void main(String[] args) throws IOException {

        System.out.println("Server booting...");

        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println("Thread pool created");

        int port = 38887;
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }
        ChatServer s = new ChatServer( port, executor );
        s.start();
        System.out.println( "ChatServer started on port: " + s.getPort() );

        // Shutdown hook to do some last-minute cleaning if interrupt signal
        // is received.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutdown signal received\n" +
                    "Saving system state...");
            s.broadcast("Server shutting down...");

            try {
                s.stop(1000);
            } catch (InterruptedException e) {
                System.err.println("Wow server shutdown got interrupted, bad " +
                        "luck.");
                e.printStackTrace();
            }
        }));

        BufferedReader sysin = new BufferedReader(
                new InputStreamReader( System.in )
        );
        while ( true ) {
            try {
                String in = sysin.readLine();
                if( in.equals( "exit" ) ) {
                    System.exit(0);
                }
                s.broadcast( in );
            } catch (NullPointerException nullPEx) {
                System.err.println("System side of stream closed");
                /* Call to System.exit() will invoke shutdown hook registered
                    above. */
                System.exit(1);
            }
        }
    }
}
