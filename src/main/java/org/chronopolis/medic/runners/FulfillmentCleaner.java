package org.chronopolis.medic.runners;

import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.client.StageManager;
import org.chronopolis.rest.models.repair.Repair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

/**
 * Runnable which cleans the staging area for a complete fulfillment
 * and updates the repair api
 *
 * Created by shake on 2/24/17.
 */
public class FulfillmentCleaner implements Runnable {
    private final Logger log = LoggerFactory.getLogger(FulfillmentCleaner.class);

    private final Repair repair;
    private final Repairs repairs;
    private final StageManager manager;

    public FulfillmentCleaner(Repairs repairs, StageManager manager, Repair repair) {
        this.repairs = repairs;
        this.manager = manager;
        this.repair = repair;
    }

    @Override
    public void run() {
        log.warn("Cleaning for fulfillments currently is not reflected in the API");

        boolean clean = manager.clean(repair);
        if (clean) {
            Call<Repair> call = repairs.repairCleaned(repair.getId());
        }
    }

}
