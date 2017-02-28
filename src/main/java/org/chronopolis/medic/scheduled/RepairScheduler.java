package org.chronopolis.medic.scheduled;

import com.google.common.collect.ImmutableMap;
import org.chronopolis.common.concurrent.TrackingThreadPoolExecutor;
import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.config.IngestConfiguration;
import org.chronopolis.medic.runners.RepairBackup;
import org.chronopolis.medic.runners.RepairCleaner;
import org.chronopolis.medic.runners.RepairReplicator;
import org.chronopolis.medic.runners.RepairValidator;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RepairStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import retrofit2.Call;

import java.util.Optional;

/**
 * Scheduled tasks which query the repair api for pending operations
 *
 * Created by shake on 2/16/17.
 */
@Profile("repair")
@EnableScheduling
public class RepairScheduler {

    private final Repairs repairs;
    private final RepairManager manager;
    private final IngestConfiguration configuration;
    private final TrackingThreadPoolExecutor<Repair> executor;

    @Autowired
    public RepairScheduler(Repairs repairs, RepairManager manager, IngestConfiguration configuration, TrackingThreadPoolExecutor<Repair> executor) {
        this.repairs = repairs;
        this.manager = manager;
        this.configuration = configuration;
        this.executor = executor;
    }

    /**
     * Query the repairs api with the requested parameter
     *
     * @param params the query parameters
     * @return a response object if available
     */
    private Optional<Page<Repair>> getRepairs(ImmutableMap<String, String> params) {
        Call<Page<Repair>> reapers = repairs.getRepairs(params);
        OptionalCallback<Page<Repair>> cb = new OptionalCallback<>();
        reapers.enqueue(cb);
        return cb.get();
    }

    // Scheduled Tasks to run query and start threads

    @Scheduled
    public void backup() {
        ImmutableMap<String, String> params = ImmutableMap.of("to", configuration.getUsername(),
                "backup", String.valueOf(false));
        getRepairs(params).ifPresent(this::doBackup);
    }

    @Scheduled
    public void repair() {
        ImmutableMap<String, String> params = ImmutableMap.of("to", configuration.getUsername(),
                "backup", String.valueOf(true),                 // only attempt repairs which are already backed up
                "status", RepairStatus.FULFILLING.toString());
        getRepairs(params).ifPresent(this::submit);
    }

    @Scheduled
    public void audit() {
        ImmutableMap<String, String> params = ImmutableMap.of("to", configuration.getUsername(),
                "backup", String.valueOf(true),                 // only attempt repairs which are already backed up
                "status", RepairStatus.FULFILLING.toString());
        getRepairs(params).ifPresent(this::runAudit);
    }

    @Scheduled
    public void clean() {
        ImmutableMap<String, String> params = ImmutableMap.of("to", configuration.getUsername(),
                "backup", String.valueOf(false),
                "status", RepairStatus.REPAIRED.toString());
        getRepairs(params).ifPresent(this::runClean);
    }

    // Helpers to submit threads to our executor

    private void doBackup(Page<Repair> repairPage) {
        repairPage.forEach(repair -> executor.submitIfAvailable(new RepairBackup(repair, repairs, manager), repair));
    }

    private void submit(Page<Repair> repairPage) {
        repairPage.forEach(repair -> executor.submitIfAvailable(new RepairReplicator(repair, repairs, manager), repair));
    }

    private void runAudit(Page<Repair> repairPage) {
        repairPage.forEach(repair -> executor.submitIfAvailable(new RepairValidator(repair, repairs, manager), repair));
    }

    private void runClean(Page<Repair> repairPage) {
        repairPage.forEach(repair -> executor.submitIfAvailable(new RepairCleaner(repair, repairs, manager), repair));
    }

}
