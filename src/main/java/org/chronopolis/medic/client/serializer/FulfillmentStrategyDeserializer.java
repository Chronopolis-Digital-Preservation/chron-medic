package org.chronopolis.medic.client.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.chronopolis.rest.models.repair.ACEStrategy;
import org.chronopolis.rest.models.repair.FulfillmentStrategy;
import org.chronopolis.rest.models.repair.RsyncStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 *
 * Created by shake on 4/11/17.
 */
public class FulfillmentStrategyDeserializer implements JsonDeserializer<FulfillmentStrategy> {

    private final Logger log = LoggerFactory.getLogger(FulfillmentStrategyDeserializer.class);

    @Override
    public FulfillmentStrategy deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        JsonElement element = object.get("type");
        String elementType = element.getAsString();
        if ("NODE_TO_NODE".equals(elementType)) {
            return jsonDeserializationContext.deserialize(jsonElement, RsyncStrategy.class);
        } else if ("ACE".equalsIgnoreCase(elementType)) {
            return jsonDeserializationContext.deserialize(jsonElement, ACEStrategy.class);
        }

        throw new RuntimeException("Unsupported fulfillment strategy " + elementType);
    }
}
