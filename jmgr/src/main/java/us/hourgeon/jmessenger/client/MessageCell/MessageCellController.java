package us.hourgeon.jmessenger.client.MessageCell;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MessageCellController {
    @FXML
    private VBox vBox;

    @FXML
    private Label label1;

    @FXML
    private Label label2;


    /**
     * Constructor
     */
    MessageCellController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("messagecell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Set the informations in the cell
     * @param expeditor The message's expeditor
     * @param message The content of the message
     */
    void setInfo(String expeditor, String message) {
        label1.setText(expeditor);
        label2.setText(message);
    }


    /**
     * Getter for the container
     * @return The layout containing all the cell content
     */
    VBox getBox() {
        return vBox;
    }
}
