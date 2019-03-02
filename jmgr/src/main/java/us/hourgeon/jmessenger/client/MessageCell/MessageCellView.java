package us.hourgeon.jmessenger.client.MessageCell;


import javafx.scene.control.ListCell;
import us.hourgeon.jmessenger.client.WSMessageTest;

public class MessageCellView extends ListCell<WSMessageTest> {

    MessageCellController controller;

    @Override
    public void updateItem(WSMessageTest item, boolean empty) {
        super.updateItem(item, empty);

        if (controller == null) {
            controller = new MessageCellController();
        }

        //int index = this.getIndex();
        String expeditor = null;
        String payload = null;

        if (item != null) {
            expeditor = item.getExpeditor() + ":";
            payload = item.getPayload();
        }

        if (controller != null) {
            controller.setInfo(expeditor, payload);
            setGraphic(controller.getBox());
        }
        else {
            setGraphic(null);
        }
    }
}
