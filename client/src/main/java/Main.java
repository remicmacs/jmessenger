import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        stage.setTitle("JMessenger Client");
        stage.setScene(new Scene(root, 300, 275));
        stage.show();

        stage.setOnCloseRequest((WindowEvent event1) -> {
            controller.close();
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
