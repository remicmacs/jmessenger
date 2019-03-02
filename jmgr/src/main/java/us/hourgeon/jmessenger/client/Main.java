package us.hourgeon.jmessenger.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Main extends Application implements WebSocketEvents, ApplicationEvents {

    private Stage stage;
    private WebSocketController webSocketController;

    @Override
    public void start(Stage passedStage) throws Exception {
        stage = passedStage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("connectwindow.fxml"));
        Parent root = loader.load();
        ConnectWindowController controller = loader.getController();

        stage.setTitle("JMessenger Client");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();

        stage.setOnCloseRequest(event -> close());

        controller.registerApplicationEvents(this);
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata, WebSocketController webSocketController) {
        System.out.println("Connected !");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("chatwindow.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ChatWindowController controller = loader.getController();
        controller.setWebSocketController(webSocketController);

        stage.setTitle("JMessenger Client");
        stage.getScene().setRoot(root);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onConnect(String address) {
        try {
            webSocketController = new WebSocketController(new URI(address));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        webSocketController.registerWebSocketEvents(this);
        if (webSocketController != null) {
            webSocketController.connect();
        }
    }

    @Override
    public void onDisconnect() {
        System.out.println("Wants to disconnect");
        if (webSocketController != null) {
            webSocketController.close(1000, "User closed the connection");
        }
    }

    private void close() {
        if (webSocketController != null) {
            webSocketController.close(1000, "Quitting application");
        }
    }
}
