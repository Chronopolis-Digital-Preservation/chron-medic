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

}
