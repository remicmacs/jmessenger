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

    void registerApplicationEvents(ApplicationEvents applicationEvents) {
        this.applicationEvents = applicationEvents;
    }
}
