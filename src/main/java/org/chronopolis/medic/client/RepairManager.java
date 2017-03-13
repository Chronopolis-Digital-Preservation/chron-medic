package org.chronopolis.medic.client;

import org.chronopolis.rest.models.repair.AuditStatus;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.Repair;

/**
 * Interface defining how our repair manager will be handled
 *
 * Created by shake on 2/17/17.
 */
public interface RepairManager {

    /**
     * Update corrupt files specific by a repair with the
     * replicated versions
     *
     * @param repair The repair
     * @return if the files in the repair have been replaced
     */
    boolean replace(Repair repair);

    /**
     * When a repair is complete (and successful), replicated files
     * can be removed
     *
     * @param repair The repair
     * @return if the replicated files have been cleaned
     */
    boolean clean(Repair repair);

    /**
     * Replicate files from a remote node
     *
     * @param fulfillment the fulfillment to replicate from
     * @param repair the repair
     * @return the success of the replication
     */
    boolean replicate(Fulfillment fulfillment, Repair repair);

    /**
     * Check replicated files
     *
     * @param repair The repair to audit
     * @return the status of the repair collection in ACE
     */
    AuditStatus audit(Repair repair);

    /**
     * Validate a set of files against ACE
     *
     * @param repair the repair to validate
     * @return whether all the files are valid
     */
    CompareResult validate(Repair repair);

}
