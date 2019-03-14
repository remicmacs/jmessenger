package us.hourgeon.jmessenger.Model;

import java.util.UUID;

public class User implements Comparable<User> {
    /**
     * Nickname currently used by the User
     */
    private final String nickName;

    /**
     * Unique identifier designating the user across different nicknames
     *
     * True primary key of the User object.
     */
    private final UUID uuid;

    /**
     * Constuctor
     *
     * Create a User object based on nickName and UUID stored in database.
     * Creation of UUID is delegated to DAO.
     * @param nickName {@link User#nickName}
     * @param uuid {@link User#uuid}
     */
    public User(String nickName, UUID uuid){
        this.nickName = nickName;
        // UUID creation should be delegated to DB or DAO.
        this.uuid = uuid;
    }

    /**
     * Copy Constructor
     *
     * Copy a User.
     * UUID and String are immutable classes, no need to invoke copy
     * constructors for members of User class.
     *
     * @param anotherUser User to copy
     */
    User(User anotherUser){
        this.nickName = anotherUser.nickName;
        this.uuid = anotherUser.uuid;
    }

    /**
     * Get nickname of User
     * @return {@link User#nickName}
     */
    public String getNickName() {
        return this.nickName;
    }

    /**
     * Get UUID of User
     * @return {@link User#uuid}
     */
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public int compareTo(User user) {
        return this.getUuid().compareTo(user.getUuid());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User &&
            ((User) o).getUuid().equals(this.getUuid());
    }

    @Override
    public String toString() {
        return "User#"+this.uuid + " a.k.a " + this.getNickName();
    }
}
