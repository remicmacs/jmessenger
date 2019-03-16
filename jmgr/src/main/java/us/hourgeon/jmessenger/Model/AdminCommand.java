package us.hourgeon.jmessenger.Model;

/**
 * Represents a server management command
 *
 * This command can only be a request from the client. The server will answer
 * with the same command type, including the needed data serialized in JSON
 * into the AdminCommand payload.
 *
 * @see AdminCommand.CommandType
 */
public class AdminCommand {

    /**
     * Describes the implemented command that a user can request to the server
     */
    public enum CommandType {

        CONNECT("CONNECT"),
        CHANNELLIST("CHANNELLIST"),
        USERLIST("USERLIST"),
        CREATECHANNEL("CREATECHANNEL"),
        CHANGENICKNAME("CHANGENICKNAME"),
        INVITEUSERS("INVITEUSERS"),
        BANUSERS("BANUSERS"),
        HISTORY("HISTORY"),
        JOIN("JOIN"),
        QUIT("QUIT"),
        ERROR("ERROR");

        private final String typeName;

        CommandType(String s) {
            typeName = s;
        }

        public String toString() {
            return this.typeName;
        }
    };


    /**
     * Action to be executed by the server
     */
    private final CommandType type;

    /**
     * Options transmitted by the user during request, or data payload
     * attached to response by the server
     *
     * JSON object to deserialize accordingly
     */
    private final String commandPayload;

    /**
     * Constructor taking a CommandType
     *
     * @param type {@link AdminCommand#type}
     * @param commandPayload {@link AdminCommand#commandPayload}
     */
    public AdminCommand(CommandType type, String commandPayload) {
        this.type = type;
        this.commandPayload = commandPayload;
    }

    /**
     * Constructor taking a string as command type
     *
     * @param type {@link AdminCommand#type}
     * @param commandPayload {@link AdminCommand#commandPayload}
     */
    public AdminCommand(String type, String commandPayload) {
        // Verifying command type validity
        String theType = type.toUpperCase();
        CommandType[] types = CommandType.values();
        for(CommandType aType : types) {
            if (aType.name().equals(theType)) {
                this.type = aType;
                this.commandPayload = commandPayload;
                return;
            }
        }

        // If type doesn't exist, throw exception
        throw new IllegalArgumentException(
                "Command " + type + " not implemented yet"
        );
    }

    /**
     * Get the type of this admin command
     * @return {@link AdminCommand#type}
     */
    public CommandType getType() {
        return this.type;
    }

    /**
     * Get stringified JSON payload of this admin command
     * @return {@link AdminCommand#commandPayload}
     */
    public String getCommandPayload() {
        return this.commandPayload;
    }
}
