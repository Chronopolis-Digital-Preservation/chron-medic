package org.chronopolis.medic.client;

/**
 * Interface defining how our repair manager will be handled
 *
 * Created by shake on 2/17/17.
 */
public interface RepairManager {

    /**
     * Backup files which will be repaired
     *
     */
    void backup();

    /**
     * When a repair is complete (and successful), backed up files
     * can be removed
     *
     */
    void removeBackup();

    /**
     * Replicate files from a remote node
     *
     */
    void replicate();

    /**
     * Check replicated files
     *
     */
    void validateFiles();

}
