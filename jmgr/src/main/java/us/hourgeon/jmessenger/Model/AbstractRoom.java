package us.hourgeon.jmessenger.Model;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class AbstractRoom extends AbstractChannel {
    /**
     * Administrators of the Room
     */
    private final ConcurrentSkipListSet<User> administrators;

    /**
     * Alias name of the Room
     *
     * Allows better memorization of room reference for human weaklings
     */
    private final String alias;

    /**
     * Full constructor
     * @param uuid the UUID of the Room
     * @param initialSubscribers collection of initial subscriber to the Room
     * @param initialAdmins collection of Users administrators of the Room
     * @param alias Room name
     */
    AbstractRoom(
        UUID uuid,
        Collection<User> initialSubscribers,
        Collection<User> initialAdmins,
        String alias
    ) {
        super(uuid, initialSubscribers);
        this.administrators = new ConcurrentSkipListSet<>(initialAdmins);
        this.alias = alias;
    }

    /**
     * Constructor without Room alias
     * @param uuid the UUID of the Room
     * @param initialSubscribers collection of initial subscriber to the Room
     * @param initialAdmins collection of Users administrators of the Room
     */
    private AbstractRoom(
        UUID uuid,
        Collection<User> initialSubscribers,
        Collection<User> initialAdmins
    ) {
        super(uuid, initialSubscribers);
        this.administrators = new ConcurrentSkipListSet<>(initialAdmins);
        this.alias = "Room#" + this.getChannelId();
    }

    /**
     * Constructor with only ID and list of admins
     * @param uuid the UUID of the Room
     * @param initialAdmins collection of initial admins of the Room
     */
    AbstractRoom(UUID uuid, Collection<User> initialAdmins) {
        this(uuid, initialAdmins, initialAdmins);
    }

    /**
     * Constructor with only the list of admins
     * @param initialAdmins collection of initial admins of the Room
     */
    AbstractRoom(Collection<User> initialAdmins) {
        this(UUID.randomUUID(), initialAdmins, initialAdmins);
    }

    /**
     * Getter for alias property
     * @return {@link AbstractRoom#alias}
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * Getter for administrator property
     * @return {@link AbstractRoom#administrators}
     */
    public Collection<User> getAdministrators() {
        // Defensive copy
        return new ConcurrentSkipListSet<>(this.administrators);
    }

    public boolean isAdmin(User aUser) {
        return this.administrators.contains(aUser);
    }
}
