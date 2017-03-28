package org.chronopolis.medic.scheduled;

import com.google.common.collect.ImmutableMap;
import org.chronopolis.common.concurrent.TrackingThreadPoolExecutor;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.config.IngestConfiguration;
import org.chronopolis.medic.runners.RepairAuditor;
import org.chronopolis.medic.runners.RepairCleaner;
import org.chronopolis.medic.runners.RepairCopier;
import org.chronopolis.medic.runners.RepairReplicator;
import org.chronopolis.medic.runners.RepairValidator;
import org.chronopolis.rest.models.repair.FulfillmentStatus;
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
 * Descending through dimensions darkened by dementia
 * Scheduled tasks which query the repair api for pending operations
 *
 * Created by shake on 2/16/17.
 */
@Component
@EnableScheduling
public class RepairScheduler extends Scheduler<Repair> {
    private final Logger log = LoggerFactory.getLogger(RepairScheduler.class);

    private final Repairs repairs;
    private final RepairManager manager;
    private final IngestConfiguration configuration;

    @Autowired
    public RepairScheduler(Repairs repairs,
                           RepairManager manager,
                           IngestConfiguration configuration,
                           TrackingThreadPoolExecutor<Repair> repairPool) {
        super(repairPool);
        this.repairs = repairs;
        this.manager = manager;
        this.configuration = configuration;
    }

    // Scheduled Tasks to run query and start threads

    @Scheduled(cron = "${cron.repair:0 0 * * * * }")
    public void repair() {
        ImmutableMap<String, String> params = ImmutableMap.of("to", configuration.getUsername(),
                "fulfillment-status", FulfillmentStatus.READY.toString());
        get(repairs::getRepairs, params).ifPresent(this::replicate);
    }


    @Scheduled(cron = "${cron.repair:0 0 * * * * }")
    public void validate() {
        ImmutableMap<String, String> params = ImmutableMap.of("to", configuration.getUsername(),
                "fulfillment-validated", String.valueOf(false),
                "fulfillment-status", FulfillmentStatus.TRANSFERRED.toString());
        get(repairs::getRepairs, params).ifPresent(this::runValidate);
    }

    @Scheduled(cron = "${cron.repair:0 0 * * * * }")
    public void copy() {
        ImmutableMap<String, String> params = ImmutableMap.of("to", configuration.getUsername(),
                "fulfillment-validated", String.valueOf(true),
                "fulfillment-status", FulfillmentStatus.TRANSFERRED.toString());
        get(repairs::getRepairs, params).ifPresent(this::runCopy);
    }


    @Scheduled(cron = "${cron.repair:0 0 * * * * }")
    public void audit() {
        ImmutableMap<String, String> params = ImmutableMap.of("to", configuration.getUsername(),
                "replaced", String.valueOf(true),
                "status", RepairStatus.FULFILLING.toString());
        get(repairs::getRepairs, params).ifPresent(this::runAudit);
    }


    @Scheduled(cron = "${cron.repair:0 0 * * * * }")
    public void clean() {
        ImmutableMap<String, String> params = ImmutableMap.of("to", configuration.getUsername(),
                "cleaned", String.valueOf(false),
                "status", RepairStatus.REPAIRED.toString()); // repaired or failed
        get(repairs::getRepairs, params).ifPresent(this::runClean);
    }


    // Helpers to submit threads to our executor. Maybe some way to trim a bit of the fat, but that's not a big concern atm.

    /*
     * An example of how we might cut down on some of the boilerplate. The only issue being we should really do Class<? extends RepairRunner>
     * so that we can ensure a constructor of Repair, Repairs, RepairManager exists (though I don't know if that would be enough).
     *
    private void submit(Page<Repair> page, Class<? extends Runnable> clazz) {
        page.forEach(repair -> {
            try {
                Constructor<? extends Runnable> constructor = clazz.getConstructor(Repair.class, Repairs.class, RepairManager.class);
                submit(repair, constructor.newInstance(repair, repairs, manager));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                log.warn("{} error in submission", repair.getCollection(), e);
            }
        });
    }
    */

    private void replicate(Page<Repair> page) {
        page.forEach(repair -> submit(repair, new RepairReplicator(repair, repairs, manager)));
    }

    private void runValidate(Page<Repair> page) {
        page.forEach(repair -> submit(repair, new RepairValidator(repair, repairs, manager)));
    }

    private void runCopy(Page<Repair> page) {
        page.forEach(repair -> submit(repair, new RepairCopier(repair, repairs, manager)));
    }

    private void runAudit(Page<Repair> repairPage) {
        repairPage.forEach(repair -> submit(repair, new RepairAuditor(repair, repairs, manager)));
    }

    private void runClean(Page<Repair> repairPage) {
        repairPage.forEach(repair -> submit(repair, new RepairCleaner(repair, repairs, manager)));
    }
}
