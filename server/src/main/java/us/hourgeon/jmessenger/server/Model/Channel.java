package us.hourgeon.jmessenger.server.Model;

import javafx.beans.Observable;

import java.util.UUID;
import java.util.Vector;

public interface Channel extends Observable {

    /**
     * Get the channel ID
     *
     * @return UUID of the channel (primary key in DB)
     */
    UUID getChannelId();

    /**
     * Get a copy of the Channel object
     *
     * @return A (defensive) copy of the Channel.
     */
    Channel getCopy();

    /**
     * Get message history of Channel
     * @return The message history until now
     */
    ChannelHistory getHistory();

    Vector<User> getRegisteredUsers();

    Vector<User> getAllUsers();
}
