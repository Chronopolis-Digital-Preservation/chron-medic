package org.chronopolis.medic.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * CLI runner
 *
 * Created by shake on 2/17/17.
 */
@Component
@Profile("cli")
public class Cli implements Service {

    @Override
    public void run() {
    }

    public void repair() {
    }

    public void fulfill() {
    }
}
