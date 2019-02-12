package us.hourgeon.jmessenger.server;
import java.io.IOException;
import java.net.InetSocketAddress;
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

public class Main {
    public static void main(String[] args) throws SQLException, IOException {
        System.out.println("Hello, world");
        /* JDBC Connection try
        try (Connection conn = DriverManager.getConnection("jdbc:mariadb" +
                "://localhost/", "root", "root")) {
            // create a Statement
            try (Statement stmt = conn.createStatement()) {
                //execute query
                try (ResultSet rs = stmt.executeQuery("SELECT 'Hello World!'")) {
                    //position result to first
                    rs.first();
                    System.out.println(rs.getString(1)); //result is "Hello World!"
                }
            }
        }
        */

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
        }
    }
}
