package us.hourgeon.jmessenger.client;


import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import us.hourgeon.jmessenger.client.MessageCell.MessageCellFactory;
import us.hourgeon.jmessenger.server.Model.WSMessageTest;

import java.util.ArrayList;
import java.util.List;

public class ChatWindowController implements MessageEvents {
    @FXML
    ListView roomsList;

    @FXML
    ListView conversationsList;

    @FXML
    ListView contactsList;

    @FXML
    ListView messagesList;

    @FXML
    TextField chatEntryField;

    @FXML
    Button chatEntrySendButton;
    
    private static final ObservableList<String> rooms = FXCollections.observableArrayList();
    private static final ObservableList<String> conversations = FXCollections.observableArrayList();
    private static final ObservableList<String> participants = FXCollections.observableArrayList();
    private static final ObservableList<WSMessageTest> messages = FXCollections.observableArrayList();

    private WebSocketController webSocketController;

    public void initialize() {

        // We set the content first before setting the look
        roomsList.setItems(rooms);
        conversationsList.setItems(conversations);
        contactsList.setItems(participants);
        messagesList.setItems(messages);

        messagesList.setCellFactory(new MessageCellFactory());

        // We set the height of the roomsList as the number of rooms times the height of a row
        roomsList.prefHeightProperty().bind(Bindings.size(rooms).multiply(24));

        // We set the height of the conversationsList as the number of conversations times the height of a row
        conversationsList.prefHeightProperty().bind(Bindings.size(conversations).multiply(24));

        GroupSelectionModel<String> first = new GroupSelectionModel<>(conversations);
        GroupSelectionModel<String> second = new GroupSelectionModel<>(rooms);
        first.setSoulmate(second);
        second.setSoulmate(first);
        conversationsList.setSelectionModel(first);
        roomsList.setSelectionModel(second);

        // Sharing the same selection model so only one selection is possible between the lists
        //roomsList.setSelectionModel(conversationsList.getSelectionModel());

        roomsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old, String neww) {
                System.out.println(old);
                System.out.println(neww);
            }
        });

        // Fill the lists with fake data
        for (int i = 0; i < 7; i++) {
            rooms.add("Room " + i);
            conversations.add("Conversation " + i);
            participants.add("Contact " + i);
        }

        chatEntryField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                send();
            }
        });
        chatEntrySendButton.setOnAction(value -> send());
    }

    void setWebSocketController(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;
        this.webSocketController.registerMessageEvents(this);
    }

    private void send() {
        String message = chatEntryField.getText();
        chatEntryField.clear();
        if (webSocketController != null && !message.isEmpty()) {
            webSocketController.send(message);
        }
        WSMessageTest wsMessage = new WSMessageTest("me", "main", message);
        messages.add(wsMessage);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
        WSMessageTest wsMessage = new WSMessageTest("server", "main", message);
        messages.add(wsMessage);
        messagesList.scrollTo(messages.size());
    }
}
