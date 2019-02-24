import org.java_websocket.handshake.ServerHandshake;

public interface WebSocketEvents {
    void onOpen(ServerHandshake handshakedata);
}
