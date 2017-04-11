package org.chronopolis.medic.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.chronopolis.medic.client.serializer.FulfillmentStrategyDeserializer;
import org.chronopolis.rest.models.repair.FulfillmentStrategy;
import org.chronopolis.rest.models.repair.RsyncStrategy;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by shake on 4/11/17.
 */
public class SerializerTest {
    private final Logger log = LoggerFactory.getLogger(SerializerTest.class);

    private final String json = "{\"link\":\"test-rsync-link\",\"type\":\"NODE_TO_NODE\"}";
    private final FulfillmentStrategy strategy = new RsyncStrategy().setLink("test-rsync-link");

    @Test
    public void testFromFS() {
        Gson gson = new GsonBuilder().create();
        Assert.assertEquals(json, gson.toJson(strategy));
    }

    @Test
    public void testFromJson() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(FulfillmentStrategy.class, new FulfillmentStrategyDeserializer())
                .create();

        Assert.assertEquals(strategy, gson.fromJson(json, FulfillmentStrategy.class));
    }


}
