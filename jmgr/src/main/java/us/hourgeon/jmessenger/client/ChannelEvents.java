package us.hourgeon.jmessenger.client;

import us.hourgeon.jmessenger.Model.User;

import java.util.ArrayList;
import java.util.UUID;

public interface ChannelEvents {
    void onQuitRequest(UUID uuid);
    void onDeleteRequest(UUID uuid);
    void onCreateRequest(String name, ArrayList<User> invites, boolean isDirect, boolean isPrivate);
    void onJoinRequest(UUID uuid);
}
