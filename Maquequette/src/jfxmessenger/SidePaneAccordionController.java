package jfxmessenger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class SidePaneAccordionController {
    @FXML
    private ListView contactListView;

    public void initialize(){
        ObservableList<String> contactArrayList =
                FXCollections.observableArrayList(
                        "Julia", "Ian", "Sue", "Matthew", "Hannah", "Stephan", "Denise"
                );
        this.contactListView.setItems(contactArrayList);
        this.contactListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>()
        {
            @Override
            public ListCell<String> call(ListView<String> listView)
            {
                return new ContactCell();
            }
        });

    }
}
