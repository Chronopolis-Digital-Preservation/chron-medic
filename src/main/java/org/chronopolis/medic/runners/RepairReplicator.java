package org.chronopolis.medic.runners;

import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.FulfillmentStatus;
import org.chronopolis.rest.models.repair.Repair;
import retrofit2.Call;

/**
 *
 * Created by shake on 2/28/17.
 */
public class RepairReplicator implements Runnable {

    private final Repair repair;
    private final Repairs repairs;
    private final RepairManager manager;

    public RepairReplicator(Repair repair, Repairs repairs, RepairManager manager) {
        this.repair = repair;
        this.repairs = repairs;
        this.manager = manager;
    }

    @Override
    public void run() {
        Call<Fulfillment> call = repairs.getFulfillment(repair.getFulfillment());
        OptionalCallback<Fulfillment> cb = new OptionalCallback<>();
        call.enqueue(cb);
        cb.get()
          .map(fulfillment -> manager.replicate(fulfillment, repair))
          .ifPresent(this::update);
    }

    /**
     * Update a repair's fulfillment with its transfer information
     *
     * @param success if the transfer succeeded
     */
    private void update(boolean success) {
        if (success) {
            Call<Fulfillment> call = repairs.fulfillmentUpdated(repair.getFulfillment(), FulfillmentStatus.TRANSFERRED);
            call.enqueue(new OptionalCallback<>());
        }
    }
}
