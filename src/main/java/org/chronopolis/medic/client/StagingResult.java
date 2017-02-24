package org.chronopolis.medic.client;

import org.chronopolis.rest.models.repair.FulfillmentStrategy;

/**
 * Class to encapsulate the result of a staging operation
 *
 * Created by shake on 2/24/17.
 */
public class StagingResult {

    private boolean success = true;
    private FulfillmentStrategy strategy;

    public boolean isSuccess() {
        return success;
    }

    public StagingResult setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public FulfillmentStrategy getStrategy() {
        return strategy;
    }

    public StagingResult setStrategy(FulfillmentStrategy strategy) {
        this.strategy = strategy;
        return this;
    }
}
