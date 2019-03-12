package us.hourgeon.jmessenger.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class Main extends Application
        implements WebSocketEvents, ApplicationEvents, MessageEvents {

    private Stage stage;
    private WebSocketController webSocketController;
    private FXMLLoader loader;


    /**
     * Mandatory starting function for JavaFX
     * @param passedStage The stage passed by the application
     * @throws Exception An Exception thrown when something goes wrong
     */
    @Override
    public void start(Stage passedStage) throws Exception {
        stage = passedStage;

        loader = new FXMLLoader(getClass().getResource("connectwindow.fxml"));
        Parent root = loader.load();
        ConnectWindowController controller = loader.getController();

        stage.setTitle("JMessenger Client");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();

        stage.setOnCloseRequest(event -> close());

        controller.registerApplicationEvents(this);
    }


    /**
     * Application starting point
     * @param args All arguments coming from the command line
     */
    public static void main(String[] args) {
        launch(args);
    }


    /**
     * Called when the connection is opened. Is responsible for setting the content
     * of the window for the chat window.
     * @param handshakedata The handshake data received from the websocket
     * @param webSocketController The websocket controller itself
     */
    @Override
    public void onOpen(ServerHandshake handshakedata, WebSocketController webSocketController) {
        loader = new FXMLLoader(getClass().getResource("chatwindow.fxml"));
        Parent root = null;
        try {
            root = loader.load();
            ChatWindowController controller = loader.getController();
            controller.setWebSocketController(webSocketController);

            stage.setTitle("JMessenger Client");
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Called when the connection is closed
     * @param code The closing code
     * @param reason The closing reason
     * @param remote True if the connection was closed by remote
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {
        ((ConnectWindowController)loader.getController()).deselectConnectButton();

        // Alert dialog to inform of bad URI format
        Alert alertDialog = new Alert(Alert.AlertType.ERROR);
        alertDialog.setTitle("Connection Error");
        String header = "Error when connecting to the server";
        alertDialog.setHeaderText(header);
        alertDialog.setResizable(true);

        String content =
                ex.getMessage() +
                        "\n" +
                        Arrays.toString(ex.getStackTrace());

        alertDialog.setContentText(content);
        alertDialog.showAndWait();
    }


    /**
     * Called by the connect button when the user wants to connect. Creates
     * the websocket and connect it.
     * @param address The address of the remote server
     */
    @Override
    public void onConnect(String address) throws URISyntaxException, IllegalArgumentException {
        URI serverURI = new URI(address);
        String scheme = serverURI.getScheme();
        if (!"wss".equals( scheme ) && !"ws".equals( scheme )) {
            throw new IllegalArgumentException( "Unsupported scheme: " + scheme );
        }
        webSocketController = new WebSocketController(serverURI);

        webSocketController.registerWebSocketEvents(this);
        webSocketController.registerMessageEvents(this);
        webSocketController.connect();
        System.err.println("Connected to server via WebSocketClient");
    }


    /**
     * Called when the user wants to disconnect
     */
    @Override
    public void onDisconnect() {
        if (webSocketController != null) {
            webSocketController.close(1000, "User closed the connection");
        }
    }


    /**
     * Close the websocket controller
     */
    private void close() {
        if (webSocketController != null) {
            webSocketController.close(1000, "Quitting application");
        }
    }

    @Override
    public void onMessage(String message) {
        // This should help catch the first messages, but we have to make more tests
        // it could be because of the runLater() invoked in the WebSocketController
        // and the check for a null MessageEvents. If not null, the call will be made
        // on the next iteration of the JavaFX thread. Since then, the chatwindow will
        // be loaded and the MessageEvents of WebSocketController will be set to
        // the ChatWindow.
    }
}
