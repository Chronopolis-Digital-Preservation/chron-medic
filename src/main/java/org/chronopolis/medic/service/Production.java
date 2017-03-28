package org.chronopolis.medic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Production runner which sleeps eternally
 *
 * Created by shake on 2/17/17.
 */
@Component
@Profile("default")
public class Production implements Service {

    private final Logger log = LoggerFactory.getLogger(Production.class);

    private final ApplicationContext context;
    private AtomicBoolean running;

    @Autowired
    public Production(ApplicationContext context) {
        this.context = context;
        this.running = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running.set(false);
        }));

        while (running.get()) {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                log.info("Interrupted");
                SpringApplication.exit(context);
            }
        }

        SpringApplication.exit(context);
    }

}
