package us.hourgeon.jmessenger.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.WebSocket;
import us.hourgeon.jmessenger.Model.Channel;
import us.hourgeon.jmessenger.Model.Message;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.Model.ZDTSerializerDeserializer;

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

    public PublishMessageRunnable(Message message, ChatServer chatServerInstance) {
        this.message = message;
        this.chatServerInstance = chatServerInstance;
    }

    @Override
    public void run() {
        Channel theChannel = null;

        for(Channel aChannel : this.chatServerInstance.getOpenChannels()) {
            if (aChannel.getChannelId().equals(message.getDestinationUUID())) {
                theChannel = aChannel;
                break;
            }
        }

        if (theChannel != null) {
            for(WebSocket currentConnection :
                    this.chatServerInstance.getConnections()) {
                User attachedUser = currentConnection.getAttachment();
                Gson gson = new GsonBuilder().registerTypeAdapter(
                        ZonedDateTime.class, new ZDTSerializerDeserializer()).create();
                if (theChannel.getSubscribers().contains(attachedUser)) {
                    currentConnection.send(gson.toJson(message, Message.class));
                }
            }
        }
    }
}
