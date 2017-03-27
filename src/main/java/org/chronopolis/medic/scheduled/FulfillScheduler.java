package org.chronopolis.medic.scheduled;

import com.google.common.collect.ImmutableMap;
import org.chronopolis.common.concurrent.TrackingThreadPoolExecutor;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.client.StageManager;
import org.chronopolis.medic.config.IngestConfiguration;
import org.chronopolis.medic.runners.FulfillmentCleaner;
import org.chronopolis.medic.runners.FulfillmentStager;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.FulfillmentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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
public class FulfillScheduler extends Scheduler<Fulfillment> {

    private final Repairs repairs;
    private final StageManager manager;
    private final IngestConfiguration configuration;

    @Autowired
    public FulfillScheduler(Repairs repairs, StageManager manager, IngestConfiguration configuration, TrackingThreadPoolExecutor<Fulfillment> executor) {
        super(executor);
        this.repairs = repairs;
        this.manager = manager;
        this.configuration = configuration;
    }

    @Scheduled(cron = "${cron.fulfillment:0 0 * * * * }")
    public void fulfill() {
        String node = configuration.getUsername();
        ImmutableMap<String, String> params = ImmutableMap.of("from", node,
                "status", FulfillmentStatus.STAGING.toString());
        get(repairs::getFulfillments, params).ifPresent(this::submit);
    }

    @Scheduled(cron = "${cron.fulfillment:0 0 * * * * }")
    public void clean() {
        String node = configuration.getUsername();
        ImmutableMap<String, String> params = ImmutableMap.of(
                "from", node,
                "status", FulfillmentStatus.COMPLETE.toString(), // todo: also check failure
                "cleaned", String.valueOf(false));
        get(repairs::getFulfillments, params).ifPresent(this::submitForClean);
    }

    private void submitForClean(Page<Fulfillment> fulfillments) {
        fulfillments.forEach(fulfillment -> submit(fulfillment, new FulfillmentCleaner(repairs, manager, fulfillment)));
    }

    private void submit(Page<Fulfillment> fulfillments) {
        fulfillments.forEach(fulfillment -> submit(fulfillment, new FulfillmentStager(repairs, manager, fulfillment)));
    }

}
