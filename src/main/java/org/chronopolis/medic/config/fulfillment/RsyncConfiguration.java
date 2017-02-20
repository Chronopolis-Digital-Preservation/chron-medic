package org.chronopolis.medic.config.fulfillment;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for rsync
 *
 * Created by shake on 2/20/17.
 */
@ConfigurationProperties(prefix = "rsync")
public class RsyncConfiguration {

    /**
     * Path to append for rsync
     */
    private String path;

    /**
     * Staging area to copy data to
     */
    private String stage;

    /**
     * Server fqdn to use in the rsync string
     */
    private String server;

    public String getPath() {
        return path;
    }

    public RsyncConfiguration setPath(String path) {
        this.path = path;
        return this;
    }

    public String getStage() {
        return stage;
    }

    public RsyncConfiguration setStage(String stage) {
        this.stage = stage;
        return this;
    }

    public String getServer() {
        return server;
    }

    public RsyncConfiguration setServer(String server) {
        this.server = server;
        return this;
    }

}
