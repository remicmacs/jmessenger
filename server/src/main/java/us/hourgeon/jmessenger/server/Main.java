package us.hourgeon.jmessenger.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.sql.*;
import java.util.Iterator;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Main {
    static class ChatServer extends WebSocketServer {

        public ChatServer( int port ) throws UnknownHostException {
            super( new InetSocketAddress( port ) );
        }

        public ChatServer( InetSocketAddress address ) {
            super( address );
        }


        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            conn.send("Welcome to the server!"); //This method sends a message to the new client
            broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This method sends a message to all clients connected
            System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );

        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {

            broadcast( conn + " has left the room!" );
            System.out.println( conn + " has left the room!" );

        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            broadcast( message );
            System.out.println( conn + ": " + message );

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
    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        System.out.println("Server booting...");

        /* Server with Sockets and Selector of channels
        // Create the server socket channel
        ServerSocketChannel server = ServerSocketChannel.open();
        // nonblocking I/O
        server.configureBlocking(false);

        server.socket().bind(new InetSocketAddress(8000));
        System.out.println("Server is active at port 8000");

        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (selector.select() > 0) {

            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> i = keys.iterator();

            System.out.println("[ " + keys.size() + " ]");

            while (i.hasNext()) {
                SelectionKey key = i.next();
                // Remove the current key
                i.remove();

                // a client required a connection
                if (key.isAcceptable()) {
                    // get client socket channel
                    SocketChannel client = server.accept();
                    // Non Blocking I/O
                    client.configureBlocking(false);
                    // recording to the selector (reading)
                    client.register(selector, SelectionKey.OP_READ);
                    continue;
                }

                // the server is ready to read
                if (key.isReadable()) {

                    SocketChannel client = (SocketChannel) key.channel();

                    // Read byte coming from the client
                    int BUFFER_SIZE = 1024;
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);


                    if (client.read(buffer) != -1 ) {

                        // Show bytes on the console
                        buffer.flip();
                        Charset charset = Charset.forName("UTF-8");
                        CharsetDecoder decoder = charset.newDecoder();
                        CharBuffer charBuffer = decoder.decode(buffer);
                        System.out.println("[" + charBuffer.toString() + "]");
                    } else {
                        // IF EOF, close the channel
                        client.close();
                    }
                }
            }
        }*/

        int port = 8887; // 843 flash policy port
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }
        ChatServer s = new ChatServer( port );
        s.start();
        System.out.println( "ChatServer started on port: " + s.getPort() );

        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String in = sysin.readLine();
            s.broadcast( in );
            if( in.equals( "exit" ) ) {
                s.stop(1000);
                break;
            }
        }


        System.out.println("Server stopping...");


    }
}
