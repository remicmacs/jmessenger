package us.hourgeon.jmessenger.Model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

public final class Message implements Serializable, Comparable<Message> {
    /**
     * User sending the message
     */
    private UUID authorUUID;

    /**
     * Channel receiving the message
     */
    private UUID destinationUUID;

    /**
     * Content of the message
     */
    private String payload;

    /**
     * Instant when the message was emitted
     */
    private ZonedDateTime timestamp;

    /**
     * Constructor
     *
     * All properties are final when the message is emitted
     * @param author {@link Message#authorUUID}
     * @param destination {@link Message#destinationUUID}
     * @param payload {@link Message#payload}
     * @param timestamp {@link Message#timestamp}
     */
    public Message(
            User author,
            Channel destination,
            String payload,
            ZonedDateTime timestamp
    ) {
        this.authorUUID = author.getUuid();
        this.destinationUUID = destination.getChannelId();
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public Message() {

    }

    /**
     * Get author
     *
     * {@link Message#authorUUID}
     * The object returned is a **defensive copy** of the User.
     *
     * @return User object representing the author of the message
     */
    public UUID getAuthorUUID() {
        return this.authorUUID;
    }

    /**
     * Get destination of message
     *
     * {@link Message#destination}
     * The object returned is a **defensive copy** of the Channel.
     *
     * @return Channel object, copy of the original Channel
     */
    public UUID getDestinationUUID() {
        return this.destinationUUID;
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
        return this.timestamp.compareTo(message.timestamp);
    }
}
