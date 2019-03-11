package us.hourgeon.jmessenger.client;

import us.hourgeon.jmessenger.Model.AbstractChannel;
import us.hourgeon.jmessenger.Model.User;

import java.util.ArrayList;
import java.util.UUID;

public interface ContactEvents {
    void onInviteRequest(UUID user, UUID channel);
    void onInvitesRequest(ArrayList<User> users, AbstractChannel channel);
    void onKickRequest(UUID user);
    void onBanRequest(UUID user);
    void onPromoteRequest(UUID user);
}
