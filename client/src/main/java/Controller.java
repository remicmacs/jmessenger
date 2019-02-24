import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import org.java_websocket.handshake.ServerHandshake;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Controller implements WebSocketEvents {

    @FXML
    private TextField serverAddress;

    @FXML
    private ToggleButton connectButton;

    private WebSocketController controller;

    public void initialize() {
        connectButton.setOnAction(event -> {
            boolean isSelected = ((ToggleButton)event.getSource()).isSelected();
            if (isSelected) {
                connect();
            } else {
                disconnect();
            }
        });
    }

    private void connect() {
        try {
            controller = new WebSocketController(new URI(serverAddress.getText()));
            controller.registerWebSocketEvents(this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (controller != null) {
            controller.connect();
        }
    }

    private void disconnect() {
        if (controller != null) {
            controller.close(1000, "User closed the connection");
        }
    }

    public void close() {
        if (controller != null) {
            controller.close(1000, "Quitting application");
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected !!");
    }
}
