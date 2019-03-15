package us.hourgeon.jmessenger.Model;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class PrivateRoom extends AbstractRoom {
    /**
     * Users authorized to subscribe to this Channel
     */
    private final ConcurrentSkipListSet<User> authorizedUsers;

    /**
     * Complete constructor
     */
    public PrivateRoom(
        UUID uuid,
        Collection<User> initialSubscribers,
        Collection<User> initialAuthorizedUsers,
        Collection<User> initialAdministrators,
        String alias
    ) {
        super(
            uuid,
            initialSubscribers,
            initialAdministrators,
            alias
        );
        this.authorizedUsers =
            new ConcurrentSkipListSet<>(initialAuthorizedUsers);
    }

    /**
     * Constructor with all access list of admins, authorized users and
     * subscribers
     */
    public PrivateRoom(
        UUID uuid,
        Collection<User> initialSubscribers,
        Collection<User> initialAuthorizedUsers,
        Collection<User> initialAdministrators
    ) {
        this(
            uuid,
            initialSubscribers,
            initialAuthorizedUsers,
            initialAdministrators,
            "PrivateRoom#"+uuid
        );
    }

    private PrivateRoom(
        UUID uuid,
        Collection<User> initialAuthorizedUsers,
        Collection<User> initialAdministrators
    ) {
        this(uuid, initialAdministrators, initialAuthorizedUsers,
                initialAdministrators);
    }

    public PrivateRoom(
        Collection<User> initialAuthorizedUsers,
        Collection<User> initialAdministrators,
        String alias
    ) {
        this(
            UUID.randomUUID(),
            initialAdministrators,
            initialAuthorizedUsers,
            initialAdministrators,
            alias
        );
    }

    public PrivateRoom(
        Collection<User> initialAuthorizedUsers,
        Collection<User> initialAdministrators
    ) {
        this(UUID.randomUUID(), initialAuthorizedUsers, initialAdministrators);
    }

    /**
     * Getter for the list of authorized Users
     * @return {@link PrivateRoom#authorizedUsers}
     */
    public ConcurrentSkipListSet<User> getAuthorizedUsers() {
        // Defensive copy
        return new ConcurrentSkipListSet<>(this.authorizedUsers);
    }

    // TODO: comment that
    public boolean isAuthorized(User aUser) {
        return this.authorizedUsers.contains(aUser);
    }

    public boolean banUser(User toBan) {
        return this.authorizedUsers.remove(toBan);
    }

    public boolean authorizeUser(User toAuthorize) {
        return this.authorizedUsers.add(toAuthorize);
    }
}
