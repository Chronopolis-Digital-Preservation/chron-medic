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
    private String stage;

    /**
     * Preservation area containing data
     */
    private String preservation;

    public String getStage() {
        return stage;
    }

    public RepairConfiguration setStage(String stage) {
        this.stage = stage;
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
