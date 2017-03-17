package org.chronopolis.medic.runners;

import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.CompareResult;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.Repair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

/**
 * Validate and Copy the files from a fulfillment
 *
 * Created by shake on 3/15/17.
 */
public class RepairValidator implements Runnable {
    private final Logger log = LoggerFactory.getLogger(RepairValidator.class);

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
        CompareResult result = manager.validate(repair);

        if (result == CompareResult.VALID) {
            log.info("{} is valid", repair.getCollection());
            Call<Fulfillment> call = repairs.fulfillmentValidated(repair.getFulfillment());
            call.enqueue(new OptionalCallback<>());
        } else if (result == CompareResult.INVALID) {
            // Fail??
            // Mark fulfillment as invalid?
            log.error("{} Unable to validate collection", repair.getCollection());
        } else {
            log.warn("{} Error communicating with ACE", repair.getCollection());
        }
    }


}
