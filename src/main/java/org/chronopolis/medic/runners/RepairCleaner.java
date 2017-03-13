package org.chronopolis.medic.runners;

import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.rest.models.repair.Repair;
import retrofit2.Call;

/**
 * Runnable to clean backed up content after a successful repair
 *
 * Created by shake on 2/28/17.
 */
public class RepairCleaner implements Runnable {

    private final Repair repair;
    private final Repairs repairs;
    private final RepairManager manager;

    public RepairCleaner(Repair repair, Repairs repairs, RepairManager manager) {
        this.repair = repair;
        this.repairs = repairs;
        this.manager = manager;
    }

    @Override
    public void run() {
        boolean removed = manager.clean(repair);
        if (removed) {
            Call<Repair> call = repairs.repairCleaned(repair.getId());
            call.enqueue(new OptionalCallback<>());
        }
    }
}
