package us.hourgeon.jmessenger.Model;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class AbstractChannel implements Channel {
    /**
     * Users currently listening to the Channel.
     */
    private final ConcurrentSkipListSet<User> subscribers;

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
        this.subscribers = new ConcurrentSkipListSet<>(initialSubscribers);
        this.history = new ConcurrentSkipListSet<>(history);
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
    public ConcurrentSkipListSet<User> getSubscribers() {
        // Defensive copy of subscribers
        return new ConcurrentSkipListSet<>(this.subscribers);
    }

    public boolean subscribeUser(User newSubscriber) {
        return this.subscribers.add(newSubscriber);
    }

    public boolean unsubscribeUser(User subscriber) {
        return this.subscribers.remove(subscriber);
    }

    public boolean isSubscribed(User user) {
        return this.subscribers.contains(user);
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

    @Override
    public int compareTo(Channel channel) {
        return this.getChannelId().compareTo(channel.getChannelId());
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof AbstractChannel) &&
            (((AbstractChannel) o).compareTo(this) == 0);
    }

    /**
     * Update a user's name across channels
     *
     * @param updatedUser
     * @return
     */
    public boolean renameUser(User updatedUser) {
        Optional<User> optionalUser =
            this.subscribers.stream().filter(user -> user.equals(updatedUser))
            .findFirst();

        optionalUser.ifPresent(user -> {
            this.unsubscribeUser(user);
            this.subscribeUser(updatedUser);

            if (this instanceof AbstractRoom ) {
                if (((AbstractRoom) this).isAdmin(user)) {
                    ((AbstractRoom) this).demoteAdmin(user);
                    ((AbstractRoom) this).promoteUser(updatedUser);
                }

                if (this instanceof PrivateRoom) {
                    if (((PrivateRoom) this).isAuthorized(user)) {
                        ((PrivateRoom) this).banUser(user);
                        ((PrivateRoom) this).authorizeUser(updatedUser);
                    }
                }
            }
        });

        return true;
    }
}
