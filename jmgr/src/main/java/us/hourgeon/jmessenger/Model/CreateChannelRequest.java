package us.hourgeon.jmessenger.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represent a channel creation request
 */
public class CreateChannelRequest {
    /**
     * List of invites to Users
     */
    private ArrayList<User> invites;

    /**
     * Channel alias
     */
    private String alias;

    private boolean isPrivate;
    private boolean isDirect;

    /**
     * Constructor
     * @param alias the alias of the new Channel
     * @param isPrivate
     * @param isDirect
     */
    public CreateChannelRequest(Collection<User> invites,
                                String alias, boolean isPrivate,
                                boolean isDirect) {
        this.invites = new ArrayList<>(invites);
        this.alias = alias;
        this.isPrivate = isPrivate;
        this.isDirect = isDirect;
    }

    public List<User> getInvites() {
        return invites;
    }

    public String getAlias() {
        return alias;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isDirect() {
        return isDirect;
    }
}
