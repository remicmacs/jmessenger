package us.hourgeon.jmessenger.server.Model;

import java.io.Serializable;
import java.time.ZonedDateTime;

public final class Message implements Serializable, Comparable<Message> {
    /**
     * User sending the message
     */
    private final User author;

    /**
     * Channel receiving the message
     */
    private final Channel destination;

    /**
     * Content of the message
     */
    private final String payload;

    /**
     * Instant when the message was emitted
     */
    private final ZonedDateTime timestamp;

    /**
     * Constructor
     *
     * All properties are final when the message is emitted
     * @param author {@link Message#author}
     * @param destination {@link Message#destination}
     * @param payload {@link Message#payload}
     * @param timestamp {@link Message#timestamp}
     */
    public Message(
            User author,
            Channel destination,
            String payload,
            ZonedDateTime timestamp
    ) {
        this.author = author;
        this.destination = destination;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    /**
     * Get author
     *
     * {@link Message#author}
     * The object returned is a **defensive copy** of the User.
     *
     * @return User object representing the author of the message
     */
    public User getAuthor() {
        return new User(this.author);
    }

    /**
     * Get destination of message
     *
     * {@link Message#destination}
     * The object returned is a **defensive copy** of the Channel.
     *
     * @return Channel object, copy of the original Channel
     */
    public Channel getDestination() {
        return this.destination.getCopy();
    }

    /**
     * Get message payload
     *
     * @return {@link Message#payload}
     */
    public String getPayload() {
        return this.payload;
    }

    /**
     * Get message timestamp
     *
     * @return {@link Message#timestamp}
     */
    public ZonedDateTime getTimestamp() {
        return this.timestamp;
    }

    @Override
    public int compareTo(Message message) {
        return 0;
    }
}
