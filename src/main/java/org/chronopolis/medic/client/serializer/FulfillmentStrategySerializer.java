package org.chronopolis.medic.client.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.chronopolis.rest.models.repair.ACEStrategy;
import org.chronopolis.rest.models.repair.FulfillmentStrategy;
import org.chronopolis.rest.models.repair.RsyncStrategy;

import java.lang.reflect.Type;

/**
 * Serializer for a fulfillment strategy because we've been having issues with the default
 * at runtime
 *
 * Created by shake on 4/12/17.
 */
public class FulfillmentStrategySerializer implements JsonSerializer<FulfillmentStrategy> {
    @Override
    public JsonElement serialize(FulfillmentStrategy fulfillmentStrategy, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonElement element;
        switch (fulfillmentStrategy.getType()) {
            case ACE:
                ACEStrategy strategy = (ACEStrategy) fulfillmentStrategy;
                element = jsonSerializationContext.serialize(strategy);
                break;
            case NODE_TO_NODE:
                RsyncStrategy rStrategy = (RsyncStrategy) fulfillmentStrategy;
                element = jsonSerializationContext.serialize(rStrategy);
                break;
            default:
                throw new JsonIOException("Unsupported type" + fulfillmentStrategy.getType());
        }

        return element;
    }
}
