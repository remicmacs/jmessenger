package us.hourgeon.jmessenger.server.Model;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public interface Channel {

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

    /**
     * Get list of Users who subscribe to the Channel
     *
     * An implementation of this method should return a defensive copy because
     * we want this data to be readonly.
     * An alien class must not be able to add Users to the list of subscribers.
     * It should **always** use the provided API to add subscribers to the
     * Channel.
     * @return subscribers of the Channel
     */
    CopyOnWriteArraySet<User> getSubscribers();
}
