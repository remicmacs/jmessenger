package us.hourgeon.jmessenger.client;

import org.java_websocket.handshake.ServerHandshake;

public interface WebSocketEvents {
    void onOpen(ServerHandshake handshakedata, WebSocketController controller);
    void onClose(int code, String reason, boolean remote);
}
