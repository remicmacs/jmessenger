package sample;

import javafx.event.ActionEvent;

import javafx.scene.control.Button;

public class SampleController {
    private boolean hasSaidHello = false;
    public void sayHello(ActionEvent actionEvent) {
        final Button btn = (Button) actionEvent.getTarget();
        if (!this.hasSaidHello) {
            this.hasSaidHello = true;
            btn.setText("Hello, human");
        } else {
            btn.setText("Begone !!");
        }
    }
}
