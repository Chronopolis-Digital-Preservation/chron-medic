package org.chronopolis.medic.runners;

import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.client.StageManager;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.Repair;
import retrofit2.Call;

import java.util.Optional;

/**
 * Simple Runnable which stages content through the StageManager
 * and updates the repair api with access information
 *
 * Created by shake on 2/24/17.
 */
public class FulfillmentStager implements Runnable {

    private final Repairs repairs;
    private final StageManager manager;
    private final Fulfillment fulfillment;

    public FulfillmentStager(Repairs repairs, StageManager manager, Fulfillment fulfillment) {
        this.repairs = repairs;
        this.manager = manager;
        this.fulfillment = fulfillment;
    }

    @Override
    public void run() {
        // Get the repair
        OptionalCallback<Repair> cb = new OptionalCallback<>();
        Call<Repair> call = repairs.getRepair(fulfillment.getRepair());
        call.enqueue(cb);
        Optional<Repair> opRepair = cb.get();
        opRepair.ifPresent(manager::stage);
    }
}
