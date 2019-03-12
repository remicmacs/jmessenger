/*
 * Copyright (c) 2010-2019 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package us.hourgeon.jmessenger.client;


import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketController extends WebSocketClient {

    private WebSocketEvents events;
    private MessageEvents messageEvents;

    WebSocketController(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        if (events != null) {
            Platform.runLater(() -> events.onOpen(handshakedata, this));
        }
    }

    @Override
    public void onMessage(String message) {
        if (messageEvents != null) {
            // Platform.runLater() will ensure that the callback will be called during the JavaFX thread
            // It has to run in the JavaFX thread because it can change elements in UI
            Platform.runLater(() -> messageEvents.onMessage(message));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The codes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
        if (events != null) {
            events.onClose(code, reason, remote);
        }
    }

    @Override
    public void onError(Exception ex) {
        Platform.runLater(() -> events.onError(ex));
    }

    void registerWebSocketEvents(WebSocketEvents events) {
        this.events = events;
    }

    void registerMessageEvents(MessageEvents messageEvents) {
        this.messageEvents = messageEvents;
    }
}