package org.chronopolis.medic;

import org.chronopolis.medic.config.IngestConfiguration;
import org.chronopolis.medic.config.fulfillment.RsyncConfiguration;
import org.chronopolis.medic.config.repair.RepairConfiguration;
import org.chronopolis.medic.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for our application
 *
 * Created by shake on 2/16/17.
 */
@SpringBootApplication
@EnableConfigurationProperties({RepairConfiguration.class, RsyncConfiguration.class})
public class App implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(App.class);

    private final Service repairService;
    private final RsyncConfiguration rsyncConfiguration;
    private final RepairConfiguration repairConfiguration;
    private final IngestConfiguration ingestConfiguration;

    @Autowired
    public App(Service repairService, RsyncConfiguration rsyncConfiguration, RepairConfiguration repairConfiguration, IngestConfiguration ingestConfiguration) {
        this.repairService = repairService;
        this.rsyncConfiguration = rsyncConfiguration;
        this.repairConfiguration = repairConfiguration;
        this.ingestConfiguration = ingestConfiguration;
    }

    public static void main(String[] args) {
        SpringApplication.exit(SpringApplication.run(App.class));
    }

    @Override
    public void run(String... strings) throws Exception {
        log.info("---Repair Configuration Settings---");
        log.info("Ingest endpoint: {}@{}", ingestConfiguration.getUsername(), ingestConfiguration.getEndpoint());
        log.info("Repair Stage: {}", repairConfiguration.getStage());
        log.info("Repair Presv: {}", repairConfiguration.getPreservation());
        log.info("Rsync : {}:{}", rsyncConfiguration.getServer(), rsyncConfiguration.getPath());
        log.info("Rsync Stage : {}", rsyncConfiguration.getStage());
        repairService.run();
    }
}
