package org.chronopolis.medic.runners;

import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.client.StageManager;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RepairStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Runnable which cleans the staging area for a complete fulfillment
 * and updates the repair api
 *
 * Created by shake on 2/24/17.
 */
public class FulfillmentCleaner implements Runnable {
    private final Logger log = LoggerFactory.getLogger(FulfillmentCleaner.class);

    private final Repairs repairs;
    private final StageManager manager;

    public FulfillmentCleaner(Repairs repairs, StageManager manager) {
        this.repairs = repairs;
        this.manager = manager;
    }

    @Override
    public void run() {
        log.info("Searching for replications to clean");
        try (Stream<Path> ongoing = manager.ongoing()) {
            ongoing.map(this::checkComplete)
                    .forEach(f -> f.ifPresent(manager::clean));
        } catch (IOException e) {
            log.warn("", e);
        }
    }

    private Optional<Repair> checkComplete(Path path) {
        OptionalCallback<Repair> cb = new OptionalCallback<>();
        Long id = Long.valueOf(path.getName(path.getNameCount() - 1).toString());
        log.debug("Checking {}", id);
        Call<Repair> call = repairs.getRepair(id);
        call.enqueue(cb);
        return cb.get()
                .filter(repair -> repair.getStatus() == RepairStatus.REPAIRED
                        || repair.getStatus() == RepairStatus.FAILED);
    }

}
