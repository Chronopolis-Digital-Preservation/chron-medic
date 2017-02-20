package org.chronopolis.medic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * Created by shake on 2/20/17.
 */
@ConfigurationProperties(prefix = "ingest")
public class IngestConfiguration {

    private String endpoint = "http://localhost:8080/";
    private String username = "username";
    private String password = "replace-me";

    public String getEndpoint() {
        return endpoint;
    }

    public IngestConfiguration setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public IngestConfiguration setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public IngestConfiguration setPassword(String password) {
        this.password = password;
        return this;
    }
}
