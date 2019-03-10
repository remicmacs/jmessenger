package us.hourgeon.jmessenger.client.ContactCell;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import us.hourgeon.jmessenger.Model.User;

public class ContactCellFactory implements Callback<ListView<User>, ListCell<User>> {

    @Override
    public ListCell<User> call(ListView<User> listview)  {
        return new ContactCellView();
    }
}