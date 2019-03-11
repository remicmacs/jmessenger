package us.hourgeon.jmessenger.server;

import us.hourgeon.jmessenger.Model.PublicChannel;

import java.util.UUID;

/**
 * Command Administration runnable
 *
 * For command messages passed between client and server, we need a special
 * type of Runnable able to create new Channel, update existing ones, etc.
 */
public class AdminCommandRunnable implements Runnable {
    private final ChatServer serverInstance;
    public AdminCommandRunnable(String payload, ChatServer serverInstance) {
        this.serverInstance = serverInstance;
    }

    @Override
    public void run() {
        PublicChannel newPublicChannel = new PublicChannel(UUID.randomUUID());
        this.serverInstance.addChannel(newPublicChannel);
        System.err.println("List of open channels : ");
        System.err.println(this.serverInstance.getOpenChannels());
    }
}
