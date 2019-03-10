package us.hourgeon.jmessenger.client.ChannelCell;

import javafx.scene.control.ListCell;
import us.hourgeon.jmessenger.Model.AbstractChannel;
import us.hourgeon.jmessenger.client.ChannelEvents;

public class ChannelCellView extends ListCell<AbstractChannel> {

    private ChannelCellController controller;

    ChannelCellView(ChannelEvents events) {
        controller = new ChannelCellController(events);
    }

    /**
     * Mandatory function for ListView's cells. Takes the contact name.
     * @param item The received message
     * @param empty True if the message is empty
     */
    @Override
    public void updateItem(AbstractChannel item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty) {
            controller.setNickname(item.getChannelId().toString());
            controller.setUUID(item.getChannelId());
        } else {
            setGraphic(null);
        }

        setGraphic(controller.getBox());
    }
}
