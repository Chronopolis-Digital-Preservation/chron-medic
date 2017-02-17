package org.chronopolis.medic.client;

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
    void stage();

    /**
     * Remove files staged for a repair
     *
     */
    void clean();

}
