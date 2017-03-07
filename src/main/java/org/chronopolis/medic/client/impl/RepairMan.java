package org.chronopolis.medic.client.impl;

import org.chronopolis.common.ace.AceService;
import org.chronopolis.common.ace.GsonCollection;
import org.chronopolis.common.exception.FileTransferException;
import org.chronopolis.common.transfer.RSyncTransfer;
import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.config.repair.RepairConfiguration;
import org.chronopolis.rest.models.repair.AuditStatus;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RsyncStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * I'm Repairman-man-man-man....
 *
 * Created by shake on 2/20/17.
 */
@Component
public class RepairMan implements RepairManager {
    private final Logger log = LoggerFactory.getLogger(RepairMan.class);

    private final AceService ace;
    private final RepairConfiguration configuration;

    @Autowired
    public RepairMan(AceService ace, RepairConfiguration configuration) {
        this.ace = ace;
        this.configuration = configuration;
    }

    @Override
    public boolean backup(Repair repair) {
        List<String> files = repair.getFiles();
        String preservation = configuration.getPreservation();
        String backup = configuration.getBackup();
        String depositor = repair.getDepositor();
        String collection = repair.getCollection();

        return files.stream()
                .map(f -> tryCopy(Paths.get(preservation, depositor, collection, f),
                                  Paths.get(backup, depositor, collection, f)))
                .allMatch(b -> b); // there has to be a better way to do this
        // could also use noneMatch false or smth to short circuit
    }



    @Override
    public boolean removeBackup(Repair repair) {
        List<String> files = repair.getFiles();
        String backup = configuration.getBackup();
        String depositor = repair.getDepositor();
        String collection = repair.getCollection();

        return files.stream()
                .map(f -> Paths.get(backup, depositor, collection, f))
                .map(this::tryDelete)
                .allMatch(b -> b);
    }

    /**
     * Helper method to try a copy operation for a file
     *
     * @param from The path to copy from
     * @param to The path to copy to
     * @return If the operation completed without exception
     */
    private boolean tryCopy(Path from, Path to) {
        try {
            Files.createDirectories(to.getParent());
            Files.copy(from, to, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("", e);
            return false;
        }
        return true;
    }

    /**
     * Helper method to try and delete a file
     *
     * @param path The path to delete
     * @return If the operation completed without exception
     */
    private boolean tryDelete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean replicate(Fulfillment fulfillment, Repair repair) {
        boolean success = true;
        // Need to figure out how to get the fulfillment strategy
        // and creator a downloader off of that
        // Probably want an interface or something to handle this
        // or maybe this can just return a replicator and our container above can
        // handle the replication/blocking

        // todo: reify the type (get type -> cast strategy to corresponding object)
        switch (fulfillment.getType()) {
            case ACE:
                success = false;
                log.warn("ACE transfers are not currently supported");
                break;
            case NODE_TO_NODE:
            case INGEST:
                success = transferRsync(fulfillment, repair);
                break;
        }

        return success;
    }

    /**
     * Method to spawn an rsync process for a repair
     *
     * todo: do we want to pull to a staging area then validate?
     *  @param fulfillment the fulfillment containing the rsync information
     * @param repair the repair
     */
    private boolean transferRsync(Fulfillment fulfillment, Repair repair) {
        boolean success = true;
        RsyncStrategy rsync = (RsyncStrategy) fulfillment.getCredentials();
        RSyncTransfer transfer = new RSyncTransfer(rsync.getLink());
        try {
            transfer.getFile(rsync.getLink(), Paths.get(configuration.getPreservation(), repair.getDepositor()));
            log(repair, transfer.getOutput());
        } catch (FileTransferException e) {
            log(repair, transfer.getErrors());
            log.error("Error transferring", e);
            success = false;
        }

        return success;

    }

    /**
     * Log all lines from a stream
     *
     * @param repair the repair being operated on
     * @param stream the stream to capture
     */
    private void log(Repair repair, InputStream stream) {
        Logger log = LoggerFactory.getLogger("rsync-log");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try (Stream<String> lines = reader.lines()) {
            lines.forEach(line -> log.info("{} {}", repair.getCollection(), line));
        }
    }

    @Override
    public AuditStatus validateFiles(Repair repair) {
        AuditStatus current = repair.getAudit();
        AuditStatus next = AuditStatus.PRE;

        OptionalCallback<GsonCollection> cb = new OptionalCallback<>();
        Call<GsonCollection> call = ace.getCollectionByName(repair.getCollection(), repair.getDepositor());
        call.enqueue(cb);

        if (current == AuditStatus.PRE) {
            log.info("{} starting audit", repair.getCollection());
            // This will need to be tested pretty thoroughly
            boolean present = cb.get()
                    .map(collection -> ace.startAudit(collection.getId(), true))
                    .map(this::queueAudit)
                    .isPresent();

            // Success? Return the next state to update to
            // cb.get.isPresent
            if (present) {
                next = AuditStatus.AUDITING;
            }
        } else if (current == AuditStatus.AUDITING) {
            log.info("{} checking audit", repair.getCollection());

            // Audit Running? Find a way to do nothing
            next = cb.get()
                    .map(this::checkCollection)
                    .orElseGet(() -> AuditStatus.AUDITING);
        }

        return next;
    }

    /**
     * Helper to simply queue an audit and return the callback
     *
     */
    private OptionalCallback<Void> queueAudit(Call<Void> audit) {
        OptionalCallback<Void> cb = new OptionalCallback<>();
        audit.enqueue(cb);
        return cb;
    }

    /**
     * Check if a collection is auditing, and what the status is
     *
     * @param collection the collection to check
     * @return the status of the (last) audit
     */
    private AuditStatus checkCollection(GsonCollection collection) {
        AuditStatus status = AuditStatus.AUDITING;
        if (Objects.equals(collection.getState(), "A")) {
            status = AuditStatus.SUCCESS;
        } else if (Objects.equals(collection.getState(), "E")) {
            status = AuditStatus.FAIL;
        }

        return status;
    }
}
