package org.chronopolis.medic.scheduled;

import com.google.common.collect.ImmutableList;
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
        // todo: how can we submit a cleaning thread which uses a real Repair... maybe... we should scan first
        //       then submit separate threads based on if the repair needs to be cleaned or not
        //       regardless not too important at the moment... it can be in the next release
        String cleaning = "cleaning-only";
        Repair fake = new Repair();
        fake.setId(-1L);
        fake.setTo(cleaning);
        fake.setFiles(ImmutableList.of());
        fake.setRequester(cleaning);
        fake.setDepositor(cleaning);
        fake.setCollection(cleaning);

        FulfillmentCleaner cleaner = new FulfillmentCleaner(repairs, manager);
        submit(fake, cleaner);
    }

    private void submit(Page<Repair> page) {
        log.info("{} repairs to stage", page.getContent().size());
        page.forEach(repair -> submit(repair, new FulfillmentStager(repairs, manager, repair)));
    }

}
