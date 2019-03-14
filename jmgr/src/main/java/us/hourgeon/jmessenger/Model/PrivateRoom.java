package us.hourgeon.jmessenger.Model;

import java.util.Collection;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class PrivateRoom extends AbstractRoom {
    /**
     * Users authorized to subscribe to this Channel
     */
    private final CopyOnWriteArraySet<User> authorizedUsers;

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
                new CopyOnWriteArraySet<>(initialAuthorizedUsers);
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
    public CopyOnWriteArraySet<User> getAuthorizedUsers() {
        // Defensive copy
        return new CopyOnWriteArraySet<>(this.authorizedUsers);
    }

    public boolean isAuthorized(User aUser) {
        return this.authorizedUsers.contains(aUser);
    }
}
