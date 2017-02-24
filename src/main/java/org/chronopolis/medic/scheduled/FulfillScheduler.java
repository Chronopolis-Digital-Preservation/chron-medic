package org.chronopolis.medic.scheduled;

import com.google.common.collect.ImmutableMap;
import org.chronopolis.common.concurrent.TrackingThreadPoolExecutor;
import org.chronopolis.medic.OptionalCallback;
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
import retrofit2.Call;

import java.util.Optional;

/**
 *
 * Created by shake on 2/16/17.
 */
@Profile("fulfill")
@EnableScheduling
public class FulfillScheduler {

    private final Repairs repairs;
    private final StageManager manager;
    private final IngestConfiguration configuration;
    private final TrackingThreadPoolExecutor<Fulfillment> executor;

    @Autowired
    public FulfillScheduler(Repairs repairs, StageManager manager, IngestConfiguration configuration, TrackingThreadPoolExecutor<Fulfillment> executor) {
        this.repairs = repairs;
        this.manager = manager;
        this.configuration = configuration;
        this.executor = executor;
    }

    @Scheduled
    public void fulfill() {
        String node = configuration.getUsername();
        Call<Page<Fulfillment>> call = repairs.getFulfillments(ImmutableMap.of(
                "from", node,
                "status", FulfillmentStatus.STAGING.toString()));

        OptionalCallback<Page<Fulfillment>> cb = new OptionalCallback<>();
        call.enqueue(cb);

        Optional<Page<Fulfillment>> fulfillments = cb.get();
        fulfillments.ifPresent(this::submit);
    }

    @Scheduled
    public void clean() {
        String node = configuration.getUsername();
        Call<Page<Fulfillment>> call = repairs.getFulfillments(ImmutableMap.of(
                "from", node,
                "status", FulfillmentStatus.COMPLETE.toString(),
                "cleaned", String.valueOf(false)));

        OptionalCallback<Page<Fulfillment>> cb = new OptionalCallback<>();
        call.enqueue(cb);

        Optional<Page<Fulfillment>> fulfillments = cb.get();
        fulfillments.ifPresent(this::submitForClean);
    }

    private void submitForClean(Page<Fulfillment> fulfillments) {
        fulfillments.forEach(fulfillment -> executor.submitIfAvailable(new FulfillmentCleaner(repairs, manager, fulfillment), fulfillment));
    }

    private void submit(Page<Fulfillment> fulfillments) {
        fulfillments.forEach(fulfillment -> executor.submitIfAvailable(new FulfillmentStager(repairs, manager, fulfillment), fulfillment));
    }

}
