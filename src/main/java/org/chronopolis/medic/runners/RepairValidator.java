package org.chronopolis.medic.runners;

import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.rest.models.repair.Repair;

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
    }
}
