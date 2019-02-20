import java.io.Serializable;

public class WSMessageTest implements Serializable {
    private final String expeditor;
    private final String channel;
    private final String payload;


    public WSMessageTest(String expeditor, String channel, String payload) {
        this.expeditor = expeditor;
        this.channel = channel;
        this.payload = payload;
    }

    public String getExpeditor() {
        return expeditor;
    }

    public String getChannel() {
        return channel;
    }

    public String getPayload() {
        return payload;
    }
}
