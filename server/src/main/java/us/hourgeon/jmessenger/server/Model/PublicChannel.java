package us.hourgeon.jmessenger.server.Model;

import java.util.Collection;
import java.util.SortedSet;
import java.util.UUID;

class PublicChannel extends AbstractChannel {
    /**
     * Constructor
     *  @param uuid               {@link PublicChannel#uuid}
     * @param initialSubscribers {@link PublicChannel#subscribers}
     * @param history            {@link PublicChannel#history}
     */
    PublicChannel(
            UUID uuid, Collection<User> initialSubscribers,
            SortedSet<Message> history
    ) {
        super(uuid, initialSubscribers, history);
    }

    @Override
    public Channel getCopy() {
        // No need to send copy of COW set because it is instantiated in
        // constructor anyway.
        return new PublicChannel(
                this.getChannelId(),
                this.getSubscribers(),
                this.getHistory().getMessages());
    }
}
