package us.hourgeon.jmessenger.client;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;

public class ConnectWindowController {

    @FXML
    private TextField serverAddress;

    @FXML
    private ToggleButton connectButton;

    @FXML
    private TextField username;

    private ApplicationEvents applicationEvents;


    /**
     * Mandatory function for JavaFX controllers
     */
    public void initialize() {
        connectButton.setOnAction(event -> {
            ToggleButton connectButton = (ToggleButton)event.getSource();
            if (connectButton.isSelected()) {
                String address = serverAddress.getText().isEmpty() ? serverAddress.getPromptText() : serverAddress.getText();
                try {
                    applicationEvents.onConnect(address);
                } catch (URISyntaxException | IllegalArgumentException e) {
                    connectButton.setSelected(false);

                    // Alert dialog to inform of bad URI format
                    Alert alertDialog = new Alert(Alert.AlertType.ERROR);
                    alertDialog.setTitle("Connection Error");
                    String header = String.format(
                            "Malformed URI : \"%s\"",
                            address
                    );
                    alertDialog.setHeaderText(header);
                    alertDialog.setResizable(true);

                    String content =
                            e.getMessage() +
                            "\n" +
                            Arrays.toString(e.getStackTrace());

                    alertDialog.setContentText(content);
                    alertDialog.showAndWait();
                }
            } else {
                applicationEvents.onDisconnect();
            }
        });
    }


    /**
     * A simple function do untoggle the connection button.
     * Mainly done for the calling application to untoggle the button
     * in case of an error during the connection
     */
    void deselectConnectButton() {
        connectButton.setSelected(false);
    }


    /**
     * Get the nickname from the TextField
     * If the TextField is empty, the nickname will be User# + a random UUID
     * The nickname may be stripped from forbidden characters (trailing and ending spaces)
     * @return The nickname
     */
    String getNickname() {
        // If we need to strip forbidden characters from the username,
        // we should do it here

        // We remove the trailing and ending whitespaces
        String nickname = username.getText().trim();

        // Return a random username if the field is empty
        if (username.getText().isEmpty()) {
            return "User#" + UUID.randomUUID().toString();
        } else {
            return username.getText();
        }
    }


    /**
     * Set the application events listener
     * @param applicationEvents A class implementing the ApplicationEvents interface
     */
    void registerApplicationEvents(ApplicationEvents applicationEvents) {
        this.applicationEvents = applicationEvents;
    }

}
