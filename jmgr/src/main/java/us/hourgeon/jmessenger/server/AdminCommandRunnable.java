package us.hourgeon.jmessenger.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.WebSocket;
import us.hourgeon.jmessenger.Model.Message;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.Model.ZDTSerializerDeserializer;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Command Administration runnable
 *
 * For command messages passed between client and server, we need a special
 * type of Runnable able to create new Channel, update existing ones, etc.
 */
public class AdminCommandRunnable implements Runnable {
    private final ChatServer serverInstance;
    private final Message adminMessage;
    private final User sender;
    private final Gson gson = new GsonBuilder().registerTypeAdapter(
            ZonedDateTime.class, new ZDTSerializerDeserializer()).create();
    public AdminCommandRunnable(Message adminMessage,
                                ChatServer serverInstance, User sender) {
        this.serverInstance = serverInstance;
        this.adminMessage = adminMessage;
        this.sender = sender;
    }

    public List<User> getConnectedUsers() {
        return new ArrayList<>(this.serverInstance.getConnectedUsers());
    }

    @Override
    public void run() {
        System.err.println("Launch admin task");
        WebSocket connection = null;
        for (WebSocket aConnection : this.serverInstance.getConnections()) {
            User attachedUser = aConnection.getAttachment();
            if (this.sender.getUuid().equals(attachedUser.getUuid())) {
                connection = aConnection;
            }
        }

        List<User> userList = this.getConnectedUsers();

        String payload = this.gson.toJson(userList);

        Message responseMessage = new Message(
                this.adminMessage.getAuthorUUID(),
                new UUID(0,0),
                payload,
                ZonedDateTime.now()
        );

        if (connection != null) {
            connection.send(gson.toJson(responseMessage, Message.class));
        }
    }
}
