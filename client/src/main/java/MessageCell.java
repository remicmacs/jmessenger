import javafx.scene.control.ListCell;

public class MessageCell extends ListCell<WSMessageTest> {

    @Override
    public void updateItem(WSMessageTest item, boolean empty) {
        super.updateItem(item, empty);

        //int index = this.getIndex();
        String name = null;

        // Format name
        if (item == null || empty)  {
        }
        else  {
            name = item.getExpeditor() + ": " +
                    item.getPayload();
        }

        this.setText(name);
        setGraphic(null);
    }
}
