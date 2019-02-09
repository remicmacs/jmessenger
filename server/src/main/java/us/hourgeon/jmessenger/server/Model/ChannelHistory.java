package us.hourgeon.jmessenger.server.Model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Vector;

final class ChannelHistory implements Serializable {
    /**
     * The Channel the history is of
     */
    private final Channel channel;

    /**
     * The Message objects ordered list
     *
     * It is the essence of the ChannelHistory object : the sequence of these
     * messages is what an history is about.
     *
     * The Vector class is used for thread safety.
     */
    private final Vector<Message> messages;

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
     * @param channel {@link ChannelHistory#channel}
     * @param messages {@link ChannelHistory#messages}
     */
    ChannelHistory(Channel channel, List<Message> messages) {
        this.channel = channel.getCopy();
        this.messages = new Vector<>(messages);
        this.timestamp = ZonedDateTime.now();
    }

    /**
     * Get messages included in the history
     * @return {@link ChannelHistory#messages}
     */
    Vector<Message> getMessages() {
        return this.messages;
    }

    /**
     * Get a copy of the Channel object
     *
     * TODO: It might not be useful to make a defensive copy of the channel.
     * @return {@link ChannelHistory#channel}
     */
    Channel getChannel() {
        return this.channel.getCopy();
    }

    /**
     * Get the timestamp of the message history.
     * @return {@link ChannelHistory#timestamp}
     */
    ZonedDateTime getTimestamp() {
        return ZonedDateTime.from(this.timestamp);
    }

}
