package us.hourgeon.jmessenger.Model;

import java.util.Collection;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractChannel implements Channel {
    /**
     * Users currently listening to the Channel.
     */
    final CopyOnWriteArraySet<User> subscribers;

    /**
     * Messages history ordered by creation time
     */
    final ConcurrentSkipListSet<Message> history;

    /**
     * UUID for Channel object
     *
     * Database primary key.
     */
    final UUID uuid;

    /**
     * Alias name for the Channel
     *
     * Allows better memorization of channel reference for human weaklings
     */
    private final String alias;

    /**
     * Constructor
     * @param uuid {@link AbstractChannel#uuid}
     * @param initialSubscribers {@link AbstractChannel#subscribers}
     * @param history {@link AbstractChannel#history}
     * @param alias {@link AbstractChannel#alias}
     */
    AbstractChannel(
            UUID uuid, Collection<User> initialSubscribers,
            SortedSet<Message> history,
            String alias) {
        subscribers = new CopyOnWriteArraySet<>(initialSubscribers);
        this.uuid = uuid;
        this.history = new ConcurrentSkipListSet<>(history);
        this.alias = alias;
    }

    AbstractChannel(
            UUID uuid, Collection<User> initialSubscribers,
            SortedSet<Message> history
    ) {
        subscribers = new CopyOnWriteArraySet<>(initialSubscribers);
        this.uuid = uuid;
        this.history = new ConcurrentSkipListSet<>(history);
        this.alias = "Channel#"+this.uuid.toString();
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
        return new CopyOnWriteArraySet<>(this.subscribers);
    }

    public void subscribeUser(User newSubscriber) {
        this.subscribers.add(newSubscriber);
    }

    public void appendMessage(Message incomingMessage) {
        this.history.add(incomingMessage);
    }

    public String getAlias() {
        return this.alias;
    }
}
