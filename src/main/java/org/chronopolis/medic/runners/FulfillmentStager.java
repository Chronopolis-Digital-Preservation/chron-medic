package org.chronopolis.medic.runners;

import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.client.StageManager;
import org.chronopolis.medic.client.StagingResult;
import org.chronopolis.rest.models.repair.Repair;
import retrofit2.Call;

/**
 * Simple Runnable which stages content through the StageManager
 * and updates the repair api with access information
 *
 * Created by shake on 2/24/17.
 */
public class FulfillmentStager implements Runnable {

    private final Repair repair;
    private final Repairs repairs;
    private final StageManager manager;

    public FulfillmentStager(Repairs repairs, StageManager manager, Repair repair) {
        this.repairs = repairs;
        this.manager = manager;
        this.repair = repair;
    }

    @Override
    public void run() {
        StagingResult result = manager.stage(repair);
        update(result);
    }

    /**
     * Update the repairs api with the value of our staging result
     *
     * @param result the result of our staging operation
     */
    private void update(StagingResult result) {
        if (result.isSuccess()) {
            Call<Repair> call = repairs.repairReady(repair.getId(), result.getStrategy());
            call.enqueue(new OptionalCallback<>());
        }
    }
}
