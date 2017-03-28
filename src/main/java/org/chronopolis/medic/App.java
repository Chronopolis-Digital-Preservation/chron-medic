package org.chronopolis.medic;

import org.chronopolis.medic.config.fulfillment.RsyncConfiguration;
import org.chronopolis.medic.config.repair.RepairConfiguration;
import org.chronopolis.medic.service.Service;
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

    private final Service repairService;

    @Autowired
    public App(Service repairService) {
        this.repairService = repairService;
    }

    public static void main(String[] args) {
        SpringApplication.exit(SpringApplication.run(App.class));
    }

    @Override
    public void run(String... strings) throws Exception {
        repairService.run();
    }
}
