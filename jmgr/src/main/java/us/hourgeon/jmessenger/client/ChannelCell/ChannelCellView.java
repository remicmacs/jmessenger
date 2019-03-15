package us.hourgeon.jmessenger.client.ChannelCell;

import javafx.scene.control.ListCell;
import us.hourgeon.jmessenger.Model.AbstractChannel;
import us.hourgeon.jmessenger.Model.AbstractRoom;
import us.hourgeon.jmessenger.Model.PublicRoom;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.client.ChannelEvents;

public class ChannelCellView extends ListCell<AbstractChannel> {

    private ChannelCellController controller;
    private User me;

    ChannelCellView(ChannelEvents events) {
        controller = new ChannelCellController(events);
    }

    ChannelCellView(ChannelEvents events, boolean hasContextMenu) {
        controller = new ChannelCellController(events, hasContextMenu);
    }

    ChannelCellView(ChannelEvents events, boolean hasContextMenu, User me) {
        controller = new ChannelCellController(events, hasContextMenu);
        this.me = me;
    }

    /**
     * Mandatory function for ListView's cells. Takes the contact name.
     * @param item The received message
     * @param empty True if the message is empty
     */
    @Override
    public void updateItem(AbstractChannel item, boolean empty) {
        super.updateItem(item, empty);

        prefWidthProperty().bind(getListView().widthProperty().subtract(24));

        if (!empty) {
            controller.setNickname(item.getChannelId().toString());
            controller.setUUID(item.getChannelId());
            controller.setHasContextMenu(true);

            if (item instanceof AbstractRoom) {
                controller.setNickname(((AbstractRoom) item).getAlias());
                if (item instanceof PublicRoom) {
                    controller.setImageUrl("empty.png");
                } else {
                    controller.setImageUrl("lock.png");
                }
            } else {
                String title = ", ";
                for (User user:item.getSubscribers()) {
                    if (!user.equals(me)) {
                        title += user.getNickName() + ", ";
                    }
                }
                title = title.substring(0, title.length() - 2);
                controller.setNickname(title);
            }
            setGraphic(controller.getBox());
        } else {
            controller.setHasContextMenu(false);
            setGraphic(null);
        }
    }
}
