package us.hourgeon.jmessenger.client.MessageCell;

import javafx.scene.control.ListCell;
import us.hourgeon.jmessenger.Model.Message;

public class MessageCellView extends ListCell<Message> {

    MessageCellController controller;


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

        String expeditor = null;
        String payload = null;

        if (item != null) {
            expeditor = item.getAuthorUUID() + ":";
            payload = item.getPayload();
        }

        if (!empty) {
            controller.setInfo(expeditor, payload);
            setGraphic(controller.getBox());
        }
        else {
            setGraphic(null);
        }
    }
}
