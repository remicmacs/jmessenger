package us.hourgeon.jmessenger.client.ContactCell;

import javafx.scene.control.ListCell;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.client.ContactEvents;

public class ContactCellView extends ListCell<User> {

    private ContactCellController controller;


    ContactCellView(ContactEvents events) {
        controller = new ContactCellController(events);
    }

    ContactCellView(ContactEvents events, boolean hasContextMenu) {
        controller = new ContactCellController(events, hasContextMenu);
    }


    /**
     * Mandatory function for ListView's cells. Takes the contact name.
     * @param item The received message
     * @param empty True if the message is empty
     */
    @Override
    public void updateItem(User item, boolean empty) {
        super.updateItem(item, empty);

        prefWidthProperty().bind(getListView().widthProperty().subtract(2));

        if (!empty) {
            controller.setNickname(item.getNickName());
            controller.setUser(item);
        } else {
            setGraphic(null);
        }

        setGraphic(controller.getBox());
    }
}
