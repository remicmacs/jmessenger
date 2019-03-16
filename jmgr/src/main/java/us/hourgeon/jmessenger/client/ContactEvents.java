package us.hourgeon.jmessenger.client;

import us.hourgeon.jmessenger.Model.AbstractChannel;
import us.hourgeon.jmessenger.Model.User;

import java.util.ArrayList;
import java.util.UUID;

public interface ContactEvents {
    void onInvitesRequest(ArrayList<User> users, AbstractChannel channel);
    void onKickRequest(User user);
    void onBanRequest(User user);
    void onPromoteRequest(User user);
}
