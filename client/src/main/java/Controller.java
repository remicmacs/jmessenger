import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Controller {

    public Controller() {
        WebSocketController c = null; // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        try {
            c = new WebSocketController(new URI( "ws://192.168.1.23:8888" ));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (c != null) {
            c.connect();
        }
    }
}
