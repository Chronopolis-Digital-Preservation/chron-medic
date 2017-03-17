package org.chronopolis.medic.runners;

import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.rest.models.repair.Repair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

/**
 * Copy a repair's files to preservation storage and
 * update if successful
 *
 * Created by shake on 3/16/17.
 */
public class RepairCopier implements Runnable {
    private final Logger log = LoggerFactory.getLogger(RepairCopier.class);

    private final Repair repair;
    private final Repairs repairs;
    private final RepairManager manager;

    public RepairCopier(Repair repair, Repairs repairs, RepairManager manager) {
        this.repair = repair;
        this.repairs = repairs;
        this.manager = manager;
    }

    @Override
    public void run() {
        boolean replaced = manager.replace(repair);
        if (replaced) {
            log.info("{} copied data to preservation successfully", repair.getCollection());
            Call<Repair> call = repairs.repairCopied(repair.getId());
            call.enqueue(new OptionalCallback<>());
        } else {
            log.warn("{} unable to copy data", repair.getCollection());
        }
    }
}
