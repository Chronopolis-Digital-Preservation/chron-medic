package org.chronopolis.medic.runners;

import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.client.StageManager;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.Repair;
import retrofit2.Call;

import java.util.Optional;

/**
 * Runnable which cleans the staging area for a complete fulfillment
 * and updates the repair api
 *
 * Created by shake on 2/24/17.
 */
public class FulfillmentCleaner implements Runnable {

    private final Repairs repairs;
    private final StageManager manager;
    private final Fulfillment fulfillment;

    public FulfillmentCleaner(Repairs repairs, StageManager manager, Fulfillment fulfillment) {
        this.repairs = repairs;
        this.manager = manager;
        this.fulfillment = fulfillment;
    }

    @Override
    public void run() {
        OptionalCallback<Repair> cb = new OptionalCallback<>();
        Call<Repair> call = repairs.getRepair(fulfillment.getId());
        call.enqueue(cb);
        Optional<Repair> repair = cb.get();

        repair.map(manager::clean)
              .ifPresent(this::update);
    }

    /**
     * Update the repair api if our fulfillment has been successfully cleaned
     *
     * @param cleaned status of our operation
     */
    private void update(Boolean cleaned) {
        if (cleaned) {
            Call<Fulfillment> call = repairs.fulfillmentCleaned(fulfillment.getId());
            call.enqueue(new OptionalCallback<>());
        }
    }
}
