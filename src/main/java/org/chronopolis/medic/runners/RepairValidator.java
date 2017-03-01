package org.chronopolis.medic.runners;

import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.rest.models.repair.AuditStatus;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.FulfillmentStatus;
import org.chronopolis.rest.models.repair.Repair;
import retrofit2.Call;

/**
 *
 * Created by shake on 2/28/17.
 */
public class RepairValidator implements Runnable {

    private final Repair repair;
    private final Repairs repairs;
    private final RepairManager manager;

    public RepairValidator(Repair repair, Repairs repairs, RepairManager manager) {
        this.repair = repair;
        this.repairs = repairs;
        this.manager = manager;
    }

    @Override
    public void run() {
        Call<Fulfillment> call = repairs.getFulfillment(repair.getFulfillment());
        OptionalCallback<Fulfillment> cb = new OptionalCallback<>();
        call.enqueue(cb);
        cb.get().filter(fulfillment -> fulfillment.getStatus() == FulfillmentStatus.TRANSFERRED)
                // we don't need the fulfillment anymore so just ignore it
                .map(ignored -> manager.validateFiles(repair))
                .ifPresent(this::update);
    }

    /**
     * Update a repair with the given AuditStatus
     *
     * @param status the result of the validateFiles operation
     */
    private void update(AuditStatus status) {
        Call<Repair> call = repairs.repairAudited(repair.getId(), status);
        call.enqueue(new OptionalCallback<>());
    }
}
