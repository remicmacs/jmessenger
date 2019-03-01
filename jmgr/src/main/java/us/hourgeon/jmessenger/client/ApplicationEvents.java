package us.hourgeon.jmessenger.client;


public interface ApplicationEvents {
    void onConnect(String address);
    void onDisconnect();
}
