package jfxmessenger;

import javafx.scene.control.ListCell;

public class ContactCell extends ListCell<String> {
    private ContactData contactData;

    public ContactCell() {
        this.contactData = new ContactData();
        this.setGraphic(this.contactData.getBox());
    }

    @Override
    public void updateItem(String string, boolean empty) {
        super.updateItem(string,empty);
        if(string != null) {
            this.contactData.setUsername(string);
        }
    }
}
