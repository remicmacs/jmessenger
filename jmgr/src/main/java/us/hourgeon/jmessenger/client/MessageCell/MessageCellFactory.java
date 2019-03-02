package us.hourgeon.jmessenger.client.MessageCell;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import us.hourgeon.jmessenger.client.MessageCell.MessageCellView;
import us.hourgeon.jmessenger.client.WSMessageTest;

public class MessageCellFactory implements Callback<ListView<WSMessageTest>, ListCell<WSMessageTest>> {

    @Override
    public ListCell<WSMessageTest> call(ListView<WSMessageTest> listview)  {
        return new MessageCellView();
    }
}
