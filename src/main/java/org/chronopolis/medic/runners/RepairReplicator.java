package org.chronopolis.medic.runners;

import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RepairStatus;
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
        boolean success = manager.replicate(repair);
        update(success);
    }

    /**
     * Update a repair's fulfillment with its transfer information
     *
     * @param success if the transfer succeeded
     */
    private void update(boolean success) {
        if (success) {
            Call<Repair> call = repairs.repairUpdate(repair.getId(), RepairStatus.TRANSFERRED);
            call.enqueue(new OptionalCallback<>());
        }
    }
}
