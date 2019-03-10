package us.hourgeon.jmessenger.client.ChannelCell;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import us.hourgeon.jmessenger.Model.AbstractChannel;
import us.hourgeon.jmessenger.client.ChannelEvents;

public class ChannelCellFactory implements Callback<ListView<AbstractChannel>, ListCell<AbstractChannel>> {

    private ChannelEvents events;

    public ChannelCellFactory(ChannelEvents events) {
        this.events = events;
    }

    @Override
    public ListCell<AbstractChannel> call(ListView<AbstractChannel> listview)  {
        return new ChannelCellView(events);
    }
}