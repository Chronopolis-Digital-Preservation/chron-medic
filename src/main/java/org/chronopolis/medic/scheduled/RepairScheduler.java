package org.chronopolis.medic.scheduled;

import com.google.common.collect.ImmutableMap;
import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.config.IngestConfiguration;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.FulfillmentStatus;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RepairStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Optional;

/**
 *
 * Created by shake on 2/16/17.
 */
@Profile("repair")
@EnableScheduling
public class RepairScheduler {

    private final Repairs repairs;
    private final RepairManager manager;
    private final IngestConfiguration configuration;

    @Autowired
    public RepairScheduler(Repairs repairs, RepairManager manager, IngestConfiguration configuration) {
        this.repairs = repairs;
        this.manager = manager;
        this.configuration = configuration;
    }

    @Scheduled
    public void backup() {
        final String node = configuration.getUsername();
        Call<Page<Repair>> call = repairs.getRepairs(ImmutableMap.of("to", node, "backup", String.valueOf(false)));

        // Would be nice to trim this down a little bit (always new cb, enqueue, get), maybe there's something we can do
        OptionalCallback<Page<Repair>> cb = new OptionalCallback<>();
        call.enqueue(cb);
        Optional<Page<Repair>> repairs = cb.get();
        repairs.ifPresent(this::doBackup);

    }

    private void doBackup(Page<Repair> repairs) {
        repairs.forEach(manager::backup);
    }

    @Scheduled
    public void repair() {
        final String node = configuration.getUsername();
        Call<Page<Repair>> reapers = repairs.getRepairs(ImmutableMap.of("to", node, "status", RepairStatus.FULFILLING.toString()));
        try {
            Response<Page<Repair>> execute = reapers.execute();
            if (execute.isSuccessful()) {
                Page<Repair> body = execute.body();
                body.forEach(this::submit);
            }
        } catch (IOException e) {
            // blah blah

        }
    }

    private void submit(Repair repair) {
        manager.backup(repair);
        if (repair.getStatus() == RepairStatus.FULFILLING) {
            OptionalCallback<Fulfillment> opFulfillment = new OptionalCallback<>();
            Call<Fulfillment> call = repairs.getFulfillment(repair.getFulfillment());
            call.enqueue(opFulfillment);

            Optional<Fulfillment> fulfillment = opFulfillment.get();
            fulfillment.filter(f -> f.getStatus() == FulfillmentStatus.READY)
                    .ifPresent(manager::replicate);
        }
    }

    @Scheduled
    public void audit() {
    }

    private void runAudit(Page<Repair> repairs) {
        repairs.forEach(manager::validateFiles);
    }

    @Scheduled
    public void clean() {
    }

    private void runClean(Page<Repair> repairs) {
        repairs.forEach(manager::removeBackup);
    }

}
