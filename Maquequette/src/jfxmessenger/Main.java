package jfxmessenger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(this.getClass().getResource("MainView.fxml"));
        primaryStage.setTitle("JFXMessenger");
        primaryStage.setScene(new Scene(root));
        System.out.println(primaryStage.getProperties());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
