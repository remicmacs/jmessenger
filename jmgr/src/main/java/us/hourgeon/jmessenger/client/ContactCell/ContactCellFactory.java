package us.hourgeon.jmessenger.client.ContactCell;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import us.hourgeon.jmessenger.Model.AbstractChannel;
import us.hourgeon.jmessenger.Model.AbstractRoom;
import us.hourgeon.jmessenger.Model.Channel;
import us.hourgeon.jmessenger.Model.User;
import us.hourgeon.jmessenger.client.ContactEvents;

public class ContactCellFactory implements Callback<ListView<User>, ListCell<User>> {

    private ContactEvents events;
    private boolean hasContextMenu = true;
    private AbstractChannel channel;

    public ContactCellFactory(ContactEvents events) {
        this.events = events;
    }

    public ContactCellFactory(ContactEvents events, boolean hasContextMenu) {
        this(events);
        this.hasContextMenu = hasContextMenu;
    }

    public ContactCellFactory(ContactEvents events, boolean hasContextMenu,
          AbstractChannel channel
    ) {
        this(events);
        this.hasContextMenu = hasContextMenu;
        this.channel = channel;
    }

    @Override
    public ListCell<User> call(ListView<User> listview)  {
        return new ContactCellView(events, hasContextMenu, channel);
    }
}