package us.hourgeon.jmessenger.Model;

import java.util.Collection;
import java.util.UUID;

public class DirectMessageConversation extends AbstractChannel {

    public DirectMessageConversation(UUID channelId, Collection<User> subscribers) {
        super(channelId, subscribers);
    }
}
