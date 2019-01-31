package jfxmessenger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;

import javafx.scene.image.ImageView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ContactData {
    @FXML
    private HBox hBox;

    @FXML
    private ImageView contactAvatar;

    @FXML
    private Label contactUsername;

    public ContactData() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ContactCellItem.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileInputStream input = null;
        try {
            input = new FileInputStream("resources/dumb_avatar.png");
            Image image = new Image(input);
            this.contactAvatar.setImage(image);
            this.contactAvatar.setFitHeight(80);
            this.contactAvatar.setFitWidth(80);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    public void setUsername(String username) {
        this.contactUsername.setText(username);
    }


    public HBox getBox() {
        return this.hBox;
    }

}
