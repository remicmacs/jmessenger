package us.hourgeon.jmessenger.Model;

import java.util.Collection;
import java.util.UUID;

/**
 * A public Channel with no access control
 */
public class PublicRoom extends AbstractRoom {

    public PublicRoom(
            UUID uuid,
            Collection<User> initialSubscribers,
            Collection<User> initialAdmins,
            String alias
    ) {
        super(uuid, initialSubscribers, initialAdmins, alias);
    }

    PublicRoom(UUID uuid, Collection<User> initialAdmins) {
        this(uuid, initialAdmins, initialAdmins, "PublicRoom#" + uuid);
    }

    public PublicRoom(Collection<User> initialAdmins, String alias) {
        this(UUID.randomUUID(),initialAdmins, initialAdmins, alias);
    }

    public PublicRoom(Collection<User> initialAdmins) {
        this(UUID.randomUUID(),initialAdmins);
    }

    @Override
    AbstractRoom changeAlias(String alias) {
        return new PublicRoom(
            this.getChannelId(),
            this.getSubscribers(),
            this.getAdministrators(),
            alias
        );
    }
}
