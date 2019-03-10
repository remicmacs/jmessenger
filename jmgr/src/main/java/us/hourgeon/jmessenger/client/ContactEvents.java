package us.hourgeon.jmessenger.client;

import java.util.UUID;

public interface ContactEvents {
    void onKickRequest(UUID uuid);
    void onPromoteRequest(UUID uuid);
}
