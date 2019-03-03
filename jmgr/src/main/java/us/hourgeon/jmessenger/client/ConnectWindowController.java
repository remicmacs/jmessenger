package us.hourgeon.jmessenger.client;


import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

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
            boolean isSelected = ((ToggleButton)event.getSource()).isSelected();
            if (isSelected) {
                String address = serverAddress.getText().isEmpty() ? serverAddress.getPromptText() : serverAddress.getText();
                applicationEvents.onConnect(address);
            } else {
                applicationEvents.onDisconnect();
            }
        });
    }


    /**
     * Set the application events listener
     * @param applicationEvents A class implementing the ApplicationEvents interface
     */
    void registerApplicationEvents(ApplicationEvents applicationEvents) {
        this.applicationEvents = applicationEvents;
    }
}
