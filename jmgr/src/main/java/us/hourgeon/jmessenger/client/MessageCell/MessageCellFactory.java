package us.hourgeon.jmessenger.client.MessageCell;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import us.hourgeon.jmessenger.Model.Message;

public class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {

    @Override
    public ListCell<Message> call(ListView<Message> listview)  {
        return new MessageCellView();
    }
}
