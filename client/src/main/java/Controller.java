import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
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

    @FXML
    private TextField messageField;

    @FXML
    private HBox messageBar;

    @FXML
    private Button sendButton;

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

        messageBar.setVisible(false);
        messageField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                send();
            }
        });
        sendButton.setOnAction(value -> {
            send();
        });
    }

    private void connect() {
        String address = serverAddress.getText().isEmpty() ? serverAddress.getPromptText() : serverAddress.getText();
        try {
            controller = new WebSocketController(new URI(address));
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
        System.out.println("Connected");
        messageBar.setVisible(true);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected");
        messageBar.setVisible(false);
    }

    public void send() {
        String message = messageField.getText();
        messageField.clear();
        if (controller != null && !message.isEmpty()) {
            controller.send(message);
        }
    }
}
