package us.hourgeon.jmessenger.Model;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.UUID;

/**
 * "Virtual" channel class
 *
 * This channel is not destined to be opened for a conversation between
 * users.
 *
 * It is used only by clients to pass commands to the server :
 *
 * Subscribe to a channel, create a channel, a conversation, add a user to a
 * channel, change nickname, etc.
 *
 * TODO: implement all features described above.
 */
public class AdminChannel extends AbstractChannel {

    /**
     * Default constructor for AdminChannel.
     *
     * UUID of the admin channel is 0x00 on 16 bytes.
     */
    public AdminChannel() {
        this(new UUID(0,0), Collections.emptyList(),
                Collections.emptySortedSet());
    }

    /**
     * Constructor
     *
     * @param uuid               {@link AbstractChannel#uuid}
     * @param initialSubscribers {@link AbstractChannel#subscribers}
     * @param history            {@link AbstractChannel#history}
     */
    AdminChannel(
            UUID uuid,
            Collection<User> initialSubscribers,
            SortedSet<Message> history
    ) {
        super(uuid, initialSubscribers, history, "Admin");
    }
}
