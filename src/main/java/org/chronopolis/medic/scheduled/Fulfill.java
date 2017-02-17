package org.chronopolis.medic.scheduled;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 *
 * Created by shake on 2/16/17.
 */
@Profile("fulfill")
@EnableScheduling
public class Fulfill {

    @Scheduled
    public void fulfill() {
    }

}
