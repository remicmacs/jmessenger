package us.hourgeon.jmessenger.Model;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Adapter for ZonedDateTime class
 *
 * Gson does not implement a default serialization of all java.* types for
 * some reason so we have to provide one.
 *
 * In JSON, ZDT is expressed as ISO timestamp, because why using something
 * that is not a standard ?
 */
public class ZDTAdapter
    implements JsonDeserializer<ZonedDateTime>, JsonSerializer<ZonedDateTime> {

    @Override
    public ZonedDateTime deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
        return  DateTimeFormatter.ISO_DATE_TIME.parse(
            json.getAsJsonPrimitive().getAsString(),
            ZonedDateTime::from
        );
    }

    @Override
    public JsonElement serialize(
            ZonedDateTime src,
            Type typeOfSrc,
            JsonSerializationContext context
    ) {
        return new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME));
    }
}