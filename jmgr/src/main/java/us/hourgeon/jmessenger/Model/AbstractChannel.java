package us.hourgeon.jmessenger.Model;

import java.util.Collection;
import java.util.Collections;
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
     * Complete constructor
     *  @param uuid               {@link PublicRoom#uuid}
     * @param initialSubscribers {@link PublicRoom#subscribers}
     * @param history            {@link PublicRoom#history}
     */
    private AbstractChannel(
            UUID uuid,
            Collection<User> initialSubscribers,
            SortedSet<Message> history
    ) {
        this.uuid = uuid;
        this.subscribers = new CopyOnWriteArraySet<User>(initialSubscribers);
        this.history = new ConcurrentSkipListSet<Message>(history);
    }

    /**
     * Default Constructor
     *
     * For lazy people.
     */
    public AbstractChannel() {
        this(UUID.randomUUID(),
            Collections.emptyList(),
            Collections.emptySortedSet());
    }

    /**
     * Constructor with only UUID and subscribers
     *
     * Because the constructor with the history was mostly destined for a
     * version of this server with a database (hydration).
     * @param uuid {@link PublicRoom#uuid}
     * @param initialSubscribers {@link PublicRoom#subscribers}
     */
    AbstractChannel(UUID uuid, Collection<User> initialSubscribers) {
        this(uuid, initialSubscribers, Collections.emptySortedSet());
    }

    /**
     * Constuctor with only the UUID
     * @param uuid {@link PublicRoom#uuid}
     */
    public AbstractChannel(UUID uuid) {
        this(uuid,
            Collections.emptyList(),
            Collections.emptySortedSet());
    }

    public AbstractChannel(Collection<User> initialSubscribers) {
        this(UUID.randomUUID(),
            initialSubscribers,
            Collections.emptySortedSet());
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
        // Defensive copy of subscribers
        return new CopyOnWriteArraySet<>(this.subscribers);
    }

    public boolean subscribeUser(User newSubscriber) {
        return this.subscribers.add(newSubscriber);
    }

    public boolean appendMessage(Message incomingMessage) {
        return this.history.add(incomingMessage);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("\tUUID: \"").append(this.uuid)
            .append("\"\n\tType: \"").append(this.getClass())
            .append("\"\n\tSubscribers: ");

        StringBuilder subscribersList = new StringBuilder("[");
        this.subscribers.forEach(user -> subscribersList.append("\n\t\t")
            .append(user));
        subscribersList.append("\n\t]\n");
        sb.append(subscribersList.toString());

        if (this instanceof AbstractRoom) {
            sb.append("\tAlias: \"").append(((AbstractRoom) this).getAlias())
                .append("\"\n\tAdmins: ");
            StringBuilder admins = new StringBuilder("[");
            ((AbstractRoom) this).getAdministrators().forEach(admin ->
                admins.append("\n\t\t")
                    .append(admin));
            admins.append("\n\t]\n");
            sb.append(admins);

            if (this instanceof PrivateRoom) {
                sb.append("\tAuthorized Users: [\n");
                ((PrivateRoom) this).getAuthorizedUsers().forEach(user ->
                    sb.append("\n\t\t")
                        .append(user)
                );
                sb.append("\n\t]\n");
            }
        }
        return sb.append("}\n").toString();
    }
}
