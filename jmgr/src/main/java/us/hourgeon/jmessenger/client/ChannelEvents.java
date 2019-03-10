package us.hourgeon.jmessenger.client;

import java.util.UUID;

public interface ChannelEvents {
    void onQuitRequest(UUID uuid);
    void onDeleteRequest(UUID uuid);
    void onCreateRequest();
}
