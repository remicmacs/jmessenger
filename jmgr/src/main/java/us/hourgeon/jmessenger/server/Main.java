package us.hourgeon.jmessenger.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Server booting...");

        // TODO: better argument parsing with a library maybe ?
        // If a port is provided on CLI, use it
        int port = (args.length == 0) ? 38887 : Integer.parseInt( args[ 0 ] );

        ChatServer s = new ChatServer();
        s.start();

        // Shutdown hook to do some last-minute cleaning if interrupt signal
        // is received.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutdown signal received\n" +
                    "Saving system state...");
            s.broadcast("Server shutting down...");

            try {
                s.stop(1000);
            } catch (InterruptedException e) {
                System.err.println(
                        "Wow server shutdown got interrupted, bad luck.");
                e.printStackTrace();
            }
        }));

        // Server-side loop for exiting server manually
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
            // Exception raised mostly if input stream has been closed (^D)
            } catch (NullPointerException nullPEx) {
                System.err.println("System side of stream closed");
                /* Call to System.exit() will invoke shutdown hook registered
                    above. */
                System.exit(1);
            }
        }
    }
}
