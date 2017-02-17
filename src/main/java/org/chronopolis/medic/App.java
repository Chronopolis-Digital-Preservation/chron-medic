package org.chronopolis.medic;

import org.chronopolis.medic.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entrypoint for our application
 *
 * Created by shake on 2/16/17.
 */
@SpringBootApplication
@EnableConfigurationProperties
public class App implements CommandLineRunner {

    @Autowired
    private Service repairService;

    public static void main(String[] args) {
        SpringApplication.exit(SpringApplication.run(App.class));
    }

    @Override
    public void run(String... strings) throws Exception {
        repairService.run();
    }
}
