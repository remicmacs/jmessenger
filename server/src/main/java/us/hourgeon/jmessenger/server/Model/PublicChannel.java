package us.hourgeon.jmessenger.server.Model;

import javafx.beans.InvalidationListener;

import java.util.UUID;
import java.util.Vector;

class PublicChannel implements Channel {
    @Override
    public UUID getChannelId() {
        return null;
    }

    @Override
    public Channel getCopy() {
        return null;
    }

    @Override
    public ChannelHistory getHistory() {
        return null;
    }

    @Override
    public Vector<User> getRegisteredUsers() {
        return null;
    }

    @Override
    public Vector<User> getAllUsers() {
        return null;
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {

    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {

    }
}
