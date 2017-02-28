package org.chronopolis.medic.runners;

import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.rest.models.repair.Repair;
import retrofit2.Call;

/**
 * Runnable to backup files requested by a repair
 *
 * Created by shake on 2/28/17.
 */
public class RepairBackup implements Runnable {

    private final Repair repair;
    private final Repairs repairs;
    private final RepairManager manager;

    public RepairBackup(Repair repair, Repairs repairs, RepairManager manager) {
        this.repair = repair;
        this.repairs = repairs;
        this.manager = manager;
    }

    @Override
    public void run() {
        boolean backup = manager.backup(repair);
        if (backup) {
            Call<Repair> call = repairs.repairBackedUp(repair.getId());
            call.enqueue(new OptionalCallback<>());
        }
    }
}
