import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Controller {

    @FXML
    private TextField serverAddress;

    @FXML
    private Button connectButton;

    private WebSocketController controller;

    public void initialize() {
        connectButton.setOnMouseClicked(mouseEvent -> connect());
    }

    private void connect() {
        try {
            controller = new WebSocketController(new URI(serverAddress.getText()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (controller != null) {
            controller.connect();
        }
    }

    public void close() {
        controller.closeConnection(1000, "Quitting application");
    }
}
