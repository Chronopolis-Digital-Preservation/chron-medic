package org.chronopolis.medic.client.impl;

import org.chronopolis.common.ace.AceService;
import org.chronopolis.common.ace.CompareFile;
import org.chronopolis.common.ace.CompareRequest;
import org.chronopolis.common.ace.CompareResponse;
import org.chronopolis.common.ace.GsonCollection;
import org.chronopolis.common.exception.FileTransferException;
import org.chronopolis.common.transfer.RSyncTransfer;
import org.chronopolis.medic.OptionalCallback;
import org.chronopolis.medic.client.CompareResult;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.config.repair.RepairConfiguration;
import org.chronopolis.medic.support.Cleaner;
import org.chronopolis.medic.support.Hasher;
import org.chronopolis.rest.models.repair.AuditStatus;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
    public boolean replace(Repair repair) {
        List<String> files = repair.getFiles();
        String preservation = configuration.getPreservation();
        String stage = configuration.getStage();
        String depositor = repair.getDepositor();
        String collection = repair.getCollection();

        log.info("{} replacing preservation copies with updated versions", collection);
        return files.stream()
                .allMatch(f -> tryCopy(Paths.get(stage, depositor, collection, f),
                                       Paths.get(preservation, depositor, collection, f)));
    }

    @Override
    public boolean clean(Repair repair) {
        List<String> files = repair.getFiles();
        String stage = configuration.getStage();
        String depositor = repair.getDepositor();
        String collection = repair.getCollection();

        log.info("{} cleaning staged copy", collection);
        Cleaner cleaner = new Cleaner(Paths.get(stage, depositor, collection), new HashSet<>(files));
        return cleaner.call();
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
            log.trace("Copying {} to {}", from, to);
            Files.createDirectories(to.getParent());
            Files.copy(from, to, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean replicate(Repair repair) {
        boolean success = true;
        // Need to figure out how to get the fulfillment strategy
        // and creator a downloader off of that
        // Probably want an interface or something to handle this
        // or maybe this can just return a replicator and our container above can
        // handle the replication/blocking

        // todo: reify the type (get type -> cast strategy to corresponding object)
        log.info("{} replicating correct version from {}", repair.getCollection(), repair.getFrom());
        switch (repair.getType()) {
            case ACE:
                success = false;
                log.warn("ACE transfers are not currently supported");
                break;
            case NODE_TO_NODE:
            case INGEST:
                success = transferRsync(repair);
                break;
        }

        return success;
    }

    /**
     * Method to spawn an rsync process for a repair
     *
     * @param repair the repair
     */
    private boolean transferRsync(Repair repair) {
        boolean success = true;
        RsyncStrategy rsync = (RsyncStrategy) repair.getCredentials();
        RSyncTransfer transfer = new RSyncTransfer(rsync.getLink());
        try {
            transfer.getFile(rsync.getLink(), Paths.get(configuration.getStage(), repair.getDepositor()));
            rsyncLog(repair, transfer.getOutput());
        } catch (FileTransferException e) {
            rsyncLog(repair, transfer.getErrors());
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
    private void rsyncLog(Repair repair, InputStream stream) {
        Logger log = LoggerFactory.getLogger("rsync-log");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try (Stream<String> lines = reader.lines()) {
            lines.forEach(line -> log.info("{} {}", repair.getCollection(), line));
        }
    }

    @Override
    public AuditStatus audit(Repair repair) {
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
                    .orElse(AuditStatus.AUDITING);
        } else if (current == AuditStatus.SUCCESS || current == AuditStatus.FAIL) {
            // just in case
            next = current;
        }

        return next;
    }

    @Override
    public CompareResult validate(Repair repair) {
        log.info("{} validating repaired files match ACE", repair.getCollection());
        OptionalCallback<GsonCollection> callback = new OptionalCallback<>();
        Call<GsonCollection> call = ace.getCollectionByName(repair.getCollection(), repair.getDepositor());
        call.enqueue(callback);
        Optional<CompareResult> validated = callback.get()
            .flatMap(collection -> compare(repair, collection))
            .map(response -> checkCompare(repair, response));
        return validated.orElse(CompareResult.CONNECTION_ERROR);
    }

    /**
     * Compile a list of {@link CompareFile}s and call the ACE compare
     * api with them
     *
     * @param repair the repair containing the files
     * @param collection the collection to compare against
     * @return the response of the ACE api
     */
    private Optional<CompareResponse> compare(Repair repair, GsonCollection collection) {
        log.debug("Getting information regarding compare");
        CompareRequest request = new CompareRequest();
        Hasher hasher = new Hasher(Paths.get(configuration.getStage()),
                Paths.get(configuration.getStage(), repair.getDepositor(), repair.getCollection()));
        // Collect files + digests
        List<CompareFile> comparisons = repair.getFiles()
                .stream()
                .map(file -> hasher.hash(Paths.get(repair.getDepositor(), repair.getCollection(), file)))
                .collect(Collectors.toList()); // might be able to just add here but w.e.
        request.setComparisons(comparisons);

        log.debug("Compiled {} files to compare", comparisons.size());
        OptionalCallback<CompareResponse> callback = new OptionalCallback<>();
        Call<CompareResponse> call = ace.compareToCollection(collection.getId(), request);
        call.enqueue(callback);
        return callback.get();
    }

    /**
     * Check a {@link CompareResponse} object and return the result of said comparison
     *
     * @param repair the ongoing repair
     * @param response the response to check
     * @return the value of the result
     */
    private CompareResult checkCompare(Repair repair, CompareResponse response) {
        CompareResult result = CompareResult.VALID;

        if (!response.getDiff().isEmpty() || !response.getNotFound().isEmpty()) {
            result = CompareResult.INVALID;
            String collection = repair.getCollection();
            int total = response.getDiff().size() + response.getNotFound().size();

            // log the problems
            log.warn("{} - Found {} errors", collection, total);
            response.getDiff().forEach(file -> log.warn("{} - {} is different", collection, file));
            response.getNotFound().forEach(file -> log.warn("{} - {} was not found", collection, file));
        }

        return result;
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
