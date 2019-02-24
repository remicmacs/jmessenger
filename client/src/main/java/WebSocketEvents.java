import org.java_websocket.handshake.ServerHandshake;

public interface WebSocketEvents {
    void onOpen(ServerHandshake handshakedata);
    void onClose(int code, String reason, boolean remote);
}
