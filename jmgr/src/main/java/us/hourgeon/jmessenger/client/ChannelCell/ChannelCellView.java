package us.hourgeon.jmessenger.client.ChannelCell;

import javafx.scene.control.ListCell;
import us.hourgeon.jmessenger.server.Model.AbstractChannel;

public class ChannelCellView extends ListCell<AbstractChannel> {

    private ChannelCellController controller;

    /**
     * Mandatory function for ListView's cells. Takes the contact name.
     * @param item The received message
     * @param empty True if the message is empty
     */
    @Override
    public void updateItem(AbstractChannel item, boolean empty) {
        super.updateItem(item, empty);

        if (controller == null) {
            controller = new ChannelCellController();
        }

        if (!empty) {
            controller.setNickname(item.getChannelId().toString());
        } else {
            setGraphic(null);
        }

        setGraphic(controller.getBox());
    }
}
