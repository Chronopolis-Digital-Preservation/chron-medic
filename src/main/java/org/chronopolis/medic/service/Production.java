package org.chronopolis.medic.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Production runner which sleeps eternally
 *
 * Created by shake on 2/17/17.
 */
@Component
@Profile("production")
public class Production implements Service {
    @Override
    public void run() {

    }
}
