package us.hourgeon.jmessenger.Model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

final class ChannelHistory implements Serializable {
    /**
     * The UUID of the Channel which activity is recorded in this history
     */
    private final UUID channelUUID;

    /**
     * The Message objects ordered list
     *
     * It is the essence of the ChannelHistory object : the sequence of these
     * messages is what an history is about.
     *
     * The Vector class is used for thread safety.
     */
    private final ConcurrentSkipListSet<Message> messages;

    /**
     * Timestamp of the instant the history was generated.
     *
     * The History object is immutable, and it was a **snapshot** of the
     * Channel's state at **one moment in time**.
     */
    private final ZonedDateTime timestamp;

    /**
     * Constructor
     *
     * Take a Channel object and its associated messages, and date the time
     * when the history was instantiated.
     *
     * No reference on original mutable object is saved. Messages are
     * immutable so they are just saved in a Vector.
     *
     * @param channelUUID {@link ChannelHistory#channelUUID}
     * @param messages {@link ChannelHistory#messages}
     */
    ChannelHistory(UUID channelUUID, SortedSet<Message> messages) {
        this.channelUUID = channelUUID;
        this.messages = new ConcurrentSkipListSet<>(messages);
        this.timestamp = ZonedDateTime.now();
    }

    /**
     * Get messages included in the history
     * @return {@link ChannelHistory#messages}
     */
    SortedSet<Message> getMessages() {
        return this.messages;
    }

    /**
     * Get the timestamp of the message history.
     * @return {@link ChannelHistory#timestamp}
     */
    ZonedDateTime getTimestamp() {
        return ZonedDateTime.from(this.timestamp);
    }

}
