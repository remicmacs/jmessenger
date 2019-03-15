package us.hourgeon.jmessenger.client.ContactCell;

import javafx.scene.control.ListCell;
import us.hourgeon.jmessenger.Model.AbstractChannel;
import us.hourgeon.jmessenger.Model.AbstractRoom;
import us.hourgeon.jmessenger.Model.Channel;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.client.ContactEvents;

public class ContactCellView extends ListCell<User> {

    private ContactCellController controller;
    private AbstractRoom channel;

    ContactCellView(ContactEvents events, boolean hasContextMenu, AbstractRoom channel) {
        controller = new ContactCellController(events, hasContextMenu);
        this.channel = channel;
    }


    /**
     * Mandatory function for ListView's cells. Takes the contact name.
     * @param item The received message
     * @param empty True if the message is empty
     */
    @Override
    public void updateItem(User item, boolean empty) {
        super.updateItem(item, empty);

        prefWidthProperty().bind(getListView().widthProperty().subtract(24));

        if (!empty) {
            controller.setNickname(item.getNickName());
            controller.setUser(item);
            if (channel == null) {
                controller.hideImage();
            } else if (channel.isAdmin(item)) {
                controller.setImageUrl("star.png");
            }
            setGraphic(controller.getBox());
        } else {
            setGraphic(null);
        }
    }
}
