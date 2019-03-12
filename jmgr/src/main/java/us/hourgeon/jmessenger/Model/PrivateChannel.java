package us.hourgeon.jmessenger.Model;

import java.util.Collection;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class PrivateChannel extends AbstractChannel {
    /**
     * Users authorized to subscribe to this Channel
     */
    private final CopyOnWriteArraySet<User> authorizedUsers;

    /**
     * Administrators of this Channel
     */
    private final CopyOnWriteArraySet<User> administrators;

    /**
     * Constructor
     *
     * @param uuid {@link PrivateChannel#uuid}
     * @param initialSubscribers {@link PrivateChannel#subscribers}
     * @param initialAuthorizedUsers {@link PrivateChannel#authorizedUsers}
     * @param initialAdministrators {@link PrivateChannel#administrators}
     * @param history {@link PrivateChannel#history}
     */
    public PrivateChannel(
            UUID uuid,
            Collection<User> initialSubscribers,
            Collection<User> initialAuthorizedUsers,
            Collection<User> initialAdministrators,
            SortedSet<Message> history
    ) {
        super(uuid, initialSubscribers, history, "PrivateChannel#"+uuid);
        this.authorizedUsers =
                new CopyOnWriteArraySet<>(initialAuthorizedUsers);
        this.administrators = new CopyOnWriteArraySet<>(initialAdministrators);
    }

    public CopyOnWriteArraySet<User> getAuthorizedUsers() {
        return this.authorizedUsers;
    }

    public CopyOnWriteArraySet<User> getAdministrators() {
        return this.administrators;
    }
}
