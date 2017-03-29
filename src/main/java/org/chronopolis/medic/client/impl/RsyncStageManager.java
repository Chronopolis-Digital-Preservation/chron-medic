package org.chronopolis.medic.client.impl;

import org.chronopolis.medic.client.StageManager;
import org.chronopolis.medic.client.StagingResult;
import org.chronopolis.medic.config.fulfillment.RsyncConfiguration;
import org.chronopolis.medic.config.repair.RepairConfiguration;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RsyncStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service which stages files for rsync
 *
 * Created by shake on 2/20/17.
 */
@Component
@Profile("rsync")
public class RsyncStageManager implements StageManager {
    private final Logger log = LoggerFactory.getLogger(RsyncStageManager.class);

    private final RepairConfiguration repairConfiguration;
    private final RsyncConfiguration configuration;

    @Autowired
    public RsyncStageManager(RepairConfiguration repairConfiguration, RsyncConfiguration syncConfiguration) {
        this.repairConfiguration = repairConfiguration;
        this.configuration = syncConfiguration;
    }

    @Override
    public StagingResult stage(Repair repair) {
        StagingResult result = new StagingResult();

        String depositor = repair.getDepositor();
        String collection = repair.getCollection();
        String stage = configuration.getStage();
        String preservation = repairConfiguration.getPreservation();
        List<String> files = repair.getFiles();

        // todo: we might want to allow for different staging strategies
        //       link, copy, raw

        // Could have this return a boolean
        // i.e. each operation returns a bool
        //      return allMatch(true) or anyMatch(false)
        log.info("{} staging content for {}", collection, repair.getTo());
        files.forEach(f -> {
            // Break this out into a method which returns an optional
            try {
                Path staged = Paths.get(stage, depositor, collection, f);
                Path preserved = Paths.get(preservation, depositor, collection, f);
                Files.createDirectories(staged.getParent());
                Files.createSymbolicLink(staged, preserved);
            } catch (IOException e) {
                log.error("", e);
                result.setSuccess(false);
            }
        });

        // will probably want to make this a bit cleaner
        result.setStrategy(new RsyncStrategy().setLink(
                repair.getTo() + "@" + configuration.getServer() + ":" + configuration.getPath() + "/" + repair.getDepositor() + "/" + repair.getCollection()));
        return result;
    }


    @Override
    public boolean clean(Repair repair) {
        String stage = configuration.getStage();
        List<String> files = repair.getFiles();
        log.info("{} cleaning staged content", repair.getCollection());
        return files.stream()
                .map(f -> Paths.get(stage, repair.getDepositor(), repair.getCollection(), f))
                .allMatch(this::tryDelete);
    }


    /**
     * Attempt to delete a file which may or may not exist
     *
     * @param file The file to delete
     * @return if the operation completed without exception
     */
    private boolean tryDelete(Path file) {
        try {
            log.trace("Trying to remove {}", file);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Error ", e);
            return false;
        }

        return true;
    }
}
