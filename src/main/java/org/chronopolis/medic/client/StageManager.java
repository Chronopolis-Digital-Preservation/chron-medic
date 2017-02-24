package org.chronopolis.medic.client;

import org.chronopolis.rest.models.repair.Repair;

/**
 * Interface defining how our staging service will act
 *
 * Created by shake on 2/17/17.
 */
public interface StageManager {

    /**
     * Stage files requested by a repair
     *
     */
    StagingResult stage(Repair repair);

    /**
     * Remove files staged for a repair
     *
     */
    boolean clean(Repair repair);

}
