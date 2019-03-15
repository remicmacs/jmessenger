package us.hourgeon.jmessenger.client.MessageCell;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import us.hourgeon.jmessenger.Model.Message;
import us.hourgeon.jmessenger.Model.User;

public class MessageCellView extends ListCell<Message> {

    MessageCellController controller;
    ObservableList<User> users;

    MessageCellView(ObservableList<User> users) {
        this.users = users;
    }

    /**
     * Mandatory function for ListView's cells. Takes the received message and the set
     * the texts and graphic part
     * @param item The received message
     * @param empty True if the message is empty
     */
    @Override
    public void updateItem(Message item, boolean empty) {
        super.updateItem(item, empty);

        if (controller == null) {
            controller = new MessageCellController();
        }

        String payload = null;

        if (item != null) {
            payload = item.getPayload();
        }

        if (!empty) {
            User messageUser = users.stream()
                    .filter(user -> user.getUuid().equals(item.getAuthorUUID()))
                    .findFirst()
                    .get();
            controller.setInfo(messageUser.getNickName() + ":", payload);
            setGraphic(controller.getBox());
        }
        else {
            setGraphic(null);
        }
    }
}
