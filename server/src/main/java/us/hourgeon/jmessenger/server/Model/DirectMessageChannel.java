package us.hourgeon.jmessenger.server.Model;


import java.util.Collection;
import java.util.SortedSet;
import java.util.UUID;

class DirectMessageChannel extends AbstractChannel {

    /**
     * Constructor
     * @param uuid               {@link DirectMessageChannel#uuid}
     * @param initialSubscribers {@link DirectMessageChannel#subscribers}
     * @param history            {@link DirectMessageChannel#history}
     */
    DirectMessageChannel(
            UUID uuid,
            Collection<User> initialSubscribers,
            SortedSet<Message> history
    ) {
        super(uuid, initialSubscribers, history);
    }

    @Override
    public Channel getCopy() {
        return new DirectMessageChannel(
                this.getChannelId(),
                this.getSubscribers(),
                this.getHistory().getMessages()
        );
    }
}
