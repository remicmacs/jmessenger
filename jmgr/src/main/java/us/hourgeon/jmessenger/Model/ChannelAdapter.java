package us.hourgeon.jmessenger.Model;

import com.google.gson.*;
import java.lang.reflect.Type;

/**
 * Adapter class for Channel types
 *
 * Serialize and deserialize Channels
 *
 * Thanks to [Oleg Varaskin](https://ovaraksin.blogspot.com/2011/05/json-with-gson-and-abstract-classes.html)
 */
public class ChannelAdapter
    implements JsonSerializer<Channel>,  JsonDeserializer<Channel> {

    @Override
    public JsonElement serialize(
        Channel src,
        Type typeOfSrc,
        JsonSerializationContext context
    ) {
        JsonObject result = new JsonObject();
        // Adding meta information on Json object for deserializer
        result.add("type", new JsonPrimitive(src.getClass().getSimpleName()));

        // Adding the serialized Channel itself
        result.add("properties", context.serialize(src, src.getClass()));
        return result;
    }

    @Override
    public Channel deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Recovering metadata and serialized Channel
        String type = jsonObject.get("type").getAsString();
        JsonElement element = jsonObject.get("properties");

        Channel deserializedChannel;
        switch (type) {
            case "PublicRoom":
                deserializedChannel =
                    context.deserialize(element, PublicRoom.class);
                break;
            case "PrivateRoom":
                deserializedChannel =
                    context.deserialize(
                        element,
                        PrivateRoom.class
                    );
                break;
            case "DirectMessageConversation":
                deserializedChannel =
                    context.deserialize(
                        element,
                        DirectMessageConversation.class
                    );
                break;
                default:
                    throw new JsonParseException(
                        "No such Channel class as " + type
                    );
        }
        return deserializedChannel;
    }
}