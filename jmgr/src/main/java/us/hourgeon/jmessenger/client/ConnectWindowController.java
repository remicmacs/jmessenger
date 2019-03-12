package us.hourgeon.jmessenger.client;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

import java.net.URISyntaxException;
import java.util.Arrays;

public class ConnectWindowController {

    @FXML
    private TextField serverAddress;

    @FXML
    private ToggleButton connectButton;

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

    public void deselectConnectButton() {
        connectButton.setSelected(false);
    }

    /**
     * Set the application events listener
     * @param applicationEvents A class implementing the ApplicationEvents interface
     */
    void registerApplicationEvents(ApplicationEvents applicationEvents) {
        this.applicationEvents = applicationEvents;
    }
}
