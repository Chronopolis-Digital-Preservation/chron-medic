package org.chronopolis.medic.config.repair;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for our repair processes
 *
 * Created by shake on 2/20/17.
 */
@ConfigurationProperties(prefix = "repair")
public class RepairConfiguration {

    /**
     * Directory to back files up to
     */
    private String backup;

    /**
     * Preservation area containing data
     */
    private String preservation;

    public String getBackup() {
        return backup;
    }

    public RepairConfiguration setBackup(String backup) {
        this.backup = backup;
        return this;
    }

    public String getPreservation() {
        return preservation;
    }

    public RepairConfiguration setPreservation(String preservation) {
        this.preservation = preservation;
        return this;
    }
}
