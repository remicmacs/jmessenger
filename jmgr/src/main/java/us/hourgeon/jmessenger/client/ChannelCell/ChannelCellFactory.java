package us.hourgeon.jmessenger.client.ChannelCell;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import us.hourgeon.jmessenger.server.Model.AbstractChannel;

public class ChannelCellFactory implements Callback<ListView<AbstractChannel>, ListCell<AbstractChannel>> {

    @Override
    public ListCell<AbstractChannel> call(ListView<AbstractChannel> listview)  {
        return new ChannelCellView();
    }
}