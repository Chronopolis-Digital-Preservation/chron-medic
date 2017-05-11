package org.chronopolis.medic.scheduled;

import com.google.common.collect.ImmutableMap;
import org.chronopolis.common.concurrent.TrackingThreadPoolExecutor;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.client.StageManager;
import org.chronopolis.medic.config.IngestConfiguration;
import org.chronopolis.medic.runners.FulfillmentCleaner;
import org.chronopolis.medic.runners.FulfillmentStager;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RepairStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Fulfillment tasks which are run. Consists of staging and cleaning.
 *
 * Created by shake on 2/16/17.
 */
@Component
@EnableScheduling
public class FulfillScheduler extends Scheduler<Repair> {

    private final Logger log = LoggerFactory.getLogger(FulfillScheduler.class);

    private final Repairs repairs;
    private final StageManager manager;
    private final IngestConfiguration configuration;

    @Autowired
    public FulfillScheduler(Repairs repairs, StageManager manager, IngestConfiguration configuration, TrackingThreadPoolExecutor<Repair> executor) {
        super(executor);
        this.repairs = repairs;
        this.manager = manager;
        this.configuration = configuration;
    }

    @Scheduled(cron = "${cron.fulfillment:0 0 * * * * }")
    public void fulfill() {
        String node = configuration.getUsername();
        ImmutableMap<String, String> params = ImmutableMap.of("from", node,
                "status", RepairStatus.STAGING.toString());
        get(repairs::getRepairs, params).ifPresent(this::submit);
    }

    @Scheduled(cron = "${cron.fulfillment:0 0 * * * * }")
    public void clean() {
        String node = configuration.getUsername();
        ImmutableMap<String, String> params = ImmutableMap.of(
                "from", node,
                "status", RepairStatus.REPAIRED.toString(), // todo: also check failure
                "cleaned", String.valueOf(false));
        get(repairs::getRepairs, params).ifPresent(this::submitForClean);
    }

    private void submitForClean(Page<Repair> page) {
        log.info("{} repairs to clean", page.getContent().size());
        page.forEach(repair -> submit(repair, new FulfillmentCleaner(repairs, manager, repair)));
    }

    private void submit(Page<Repair> page) {
        log.info("{} repairs to stage", page.getContent().size());
        page.forEach(repair -> submit(repair, new FulfillmentStager(repairs, manager, repair)));
    }

}
