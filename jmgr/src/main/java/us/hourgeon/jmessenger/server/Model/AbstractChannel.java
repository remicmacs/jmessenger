package us.hourgeon.jmessenger.server.Model;

import java.util.Collection;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractChannel implements Channel {
    /**
     * Users currently listening to the Channel.
     */
    private final CopyOnWriteArraySet<User> subscribers;

    /**
     * Messages history ordered by creation time
     */
    private final ConcurrentSkipListSet<Message> history;

    /**
     * UUID for Channel object
     *
     * Database primary key.
     */
    private final UUID uuid;

    /**
     * Constructor
     * @param uuid {@link AbstractChannel#uuid}
     * @param initialSubscribers {@link AbstractChannel#subscribers}
     * @param history {@link AbstractChannel#history}
     */
    AbstractChannel(
            UUID uuid, Collection<User> initialSubscribers,
            SortedSet<Message> history
    ) {
        subscribers = new CopyOnWriteArraySet<>(initialSubscribers);
        this.uuid = uuid;
        this.history = new ConcurrentSkipListSet<>(history);
    }

    @Override
    public UUID getChannelId() {
        return this.uuid;
    }

    @Override
    public ChannelHistory getHistory() {
        return new ChannelHistory(
                this.uuid,
                this.history
        );
    }

    @Override
    public CopyOnWriteArraySet<User> getSubscribers() {
        return new CopyOnWriteArraySet<User>(this.subscribers);
    }
}
