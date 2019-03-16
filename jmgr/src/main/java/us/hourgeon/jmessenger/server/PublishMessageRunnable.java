package us.hourgeon.jmessenger.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.WebSocket;
import us.hourgeon.jmessenger.Model.Channel;
import us.hourgeon.jmessenger.Model.Message;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.Model.ZDTAdapter;

import java.time.ZonedDateTime;

/**
 * Runnable to publish a simple message to a channel
 *
 * No privileges checked because message is just forwarded to connections
 * being part of the subscribers of the channel. All subscribers have a
 * read/write privilege on channel so no need to check upon message publishing.
 */
public class PublishMessageRunnable implements Runnable {
    private final Message message;
    private final ChatServer chatServerInstance;
    final private Gson gson = new GsonBuilder().registerTypeAdapter(
        ZonedDateTime.class, new ZDTAdapter()).create();

    PublishMessageRunnable(Message message, ChatServer chatServerInstance) {
        this.message = message;
        this.chatServerInstance = chatServerInstance;
    }

    @Override
    public void run() {
        System.err.println("Launch publish standard message");

        if (this.message.getDestinationUUID()
            .equals(this.chatServerInstance.getGeneralChannel().getChannelId())
        ) {
            this.broadcastMessageToGeneral();
        } else {
            this.publishMessage();
        }

    }

    private void publishMessage() {
        Channel theChannel =
            this.chatServerInstance.getOpenChannels()
                .get(this.message.getDestinationUUID());


        if (theChannel != null) {
            theChannel.appendMessage(this.message);

            for(WebSocket currentConnection :
                this.chatServerInstance.getConnections()
            ) {
                User attachedUser = currentConnection.getAttachment();
                if (theChannel.getChannelId() !=
                    this.chatServerInstance.getGeneralChannel().getChannelId() &&
                    theChannel.getSubscribers().contains(attachedUser)) {
                    currentConnection.send(this.gson.toJson(
                        this.message,
                        Message.class)
                    );

                    System.err.println("Standard message sent");
                }
            }
        }
    }

    private void broadcastMessageToGeneral() {
        this.chatServerInstance.broadcast(this.gson.toJson(message,
            Message.class));
        this.chatServerInstance.getGeneralChannel().appendMessage(message);
        System.err.println("Message posted in shoutbox for all to see");
    }
}
