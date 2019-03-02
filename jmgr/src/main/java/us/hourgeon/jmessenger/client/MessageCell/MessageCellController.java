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

    public MessageCellController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("messagecell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setInfo(String expeditor, String message) {
        label1.setText(expeditor);
        label2.setText(message);
    }

    public VBox getBox() {
        return vBox;
    }
}
