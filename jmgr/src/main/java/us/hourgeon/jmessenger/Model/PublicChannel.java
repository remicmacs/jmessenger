package us.hourgeon.jmessenger.Model;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.UUID;

public class PublicChannel extends AbstractChannel {
    /**
     * Constructor
     *  @param uuid               {@link PublicChannel#uuid}
     * @param initialSubscribers {@link PublicChannel#subscribers}
     * @param history            {@link PublicChannel#history}
     */
    public PublicChannel(
            UUID uuid, Collection<User> initialSubscribers,
            SortedSet<Message> history
    ) {
        this(uuid, initialSubscribers, history, "PublicChannel#"+ uuid);
    }

    /**
     * Constructor with an alias
     *  @param uuid               {@link PublicChannel#uuid}
     * @param initialSubscribers {@link PublicChannel#subscribers}
     * @param history            {@link PublicChannel#history}
     * @param alias             {@link AbstractChannel#alias}
     */
    public PublicChannel(
        UUID uuid, Collection<User> initialSubscribers,
        SortedSet<Message> history, String alias
    ) {
        super(uuid, initialSubscribers, history, alias);
    }


    /**
     * Constuctor with only the UUID and the alias
     * @param uuid {@link PublicChannel#uuid}
     * @param alias {@link AbstractChannel#alias}
     */
    public PublicChannel(UUID uuid, String alias) {
        this(uuid,
            Collections.emptyList(),
            Collections.emptySortedSet(),
            alias);
    }

    /**
     * Constructor with only the alias
     * @param alias {@link AbstractChannel#alias}
     */
    public PublicChannel(String alias) {
        this(UUID.randomUUID(),
            Collections.emptyList(),
            Collections.emptySortedSet(),
            alias);
    }
}
