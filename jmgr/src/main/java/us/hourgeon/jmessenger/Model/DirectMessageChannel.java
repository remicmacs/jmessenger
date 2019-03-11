package us.hourgeon.jmessenger.Model;


import java.util.Collection;
import java.util.SortedSet;
import java.util.UUID;

public class DirectMessageChannel extends AbstractChannel {

    /**
     * Constructor
     * @param uuid               {@link DirectMessageChannel#uuid}
     * @param initialSubscribers {@link DirectMessageChannel#subscribers}
     * @param history            {@link DirectMessageChannel#history}
     */
    public DirectMessageChannel(
            UUID uuid,
            Collection<User> initialSubscribers,
            SortedSet<Message> history
    ) {
        super(uuid, initialSubscribers, history);
    }
}
