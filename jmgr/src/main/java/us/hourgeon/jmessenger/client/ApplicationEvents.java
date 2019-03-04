package us.hourgeon.jmessenger.client;


import java.net.URISyntaxException;

public interface ApplicationEvents {
    void onConnect(String address) throws URISyntaxException;
    void onDisconnect();
}
