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
        super(uuid, initialSubscribers, history);
    }

    public PublicChannel(UUID uuid) {
        this(uuid, Collections.emptyList(), Collections.emptySortedSet());
    }
}
