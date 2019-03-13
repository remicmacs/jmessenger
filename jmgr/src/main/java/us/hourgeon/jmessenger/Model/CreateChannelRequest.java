package us.hourgeon.jmessenger.Model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represent a channel creation request
 */
public class CreateChannelRequest {
    /**
     * List of initial subscribers
     *
     * TODO: change to list of initial admins
     */
    private ArrayList<User> initSubscribers;

    /**
     * Channel alias
     */
    private String alias;

    private boolean isPrivate;
    private boolean isDirect;

    /**
     * Constructor
     * @param initSubscribers
     * @param alias
     * @param isPrivate
     * @param isDirect
     */
    public CreateChannelRequest(Collection<User> initSubscribers,
                                String alias, boolean isPrivate,
                                boolean isDirect) {
        this.initSubscribers = new ArrayList<User>(initSubscribers);
        this.alias = alias;
        this.isPrivate = isPrivate;
        this.isDirect = isDirect;
    }

    public ArrayList<User> getInitSubscribers() {
        return initSubscribers;
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
