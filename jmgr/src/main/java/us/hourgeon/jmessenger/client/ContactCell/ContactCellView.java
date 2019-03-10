package us.hourgeon.jmessenger.client.ContactCell;

import javafx.scene.control.ListCell;
import us.hourgeon.jmessenger.server.Model.User;

public class ContactCellView extends ListCell<User> {

    private ContactCellController controller;

    /**
     * Mandatory function for ListView's cells. Takes the contact name.
     * @param item The received message
     * @param empty True if the message is empty
     */
    @Override
    public void updateItem(User item, boolean empty) {
        super.updateItem(item, empty);

        if (controller == null) {
            controller = new ContactCellController();
        }

        if (!empty) {
            controller.setNickname(item.getNickName());
        } else {
            setGraphic(null);
        }

        setGraphic(controller.getBox());
    }
}
