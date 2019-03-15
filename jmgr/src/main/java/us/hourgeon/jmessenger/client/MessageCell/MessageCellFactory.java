package us.hourgeon.jmessenger.client.MessageCell;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import us.hourgeon.jmessenger.Model.Message;
import us.hourgeon.jmessenger.Model.User;

public class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {

    private ObservableList<User> users;

    public MessageCellFactory(ObservableList<User> users) {
        this.users = users;
    }

    @Override
    public ListCell<Message> call(ListView<Message> listview)  {
        return new MessageCellView(users);
    }
}
