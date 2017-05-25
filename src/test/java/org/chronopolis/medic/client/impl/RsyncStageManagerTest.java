package org.chronopolis.medic.client.impl;

import com.google.common.collect.ImmutableList;
import org.chronopolis.medic.client.StagingResult;
import org.chronopolis.medic.config.fulfillment.RsyncConfiguration;
import org.chronopolis.medic.config.repair.RepairConfiguration;
import org.chronopolis.medic.support.Staging;
import org.chronopolis.rest.models.repair.FulfillmentStrategy;
import org.chronopolis.rest.models.repair.FulfillmentType;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RsyncStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Tests for the RsyncStageManager
 *
 * Created by shake on 3/2/17.
 */
public class RsyncStageManagerTest {

    private final Logger log = LoggerFactory.getLogger(RsyncStageManagerTest.class);

    private final String TO = "test-node";
    private final String DEPOSITOR = "test-depositor";
    private final String COLLECTION_STAGE = "test-corrupt";
    private final String CORRUPT_1 = "data/corrupt-1";
    private final String CORRUPT_2 = "data/sub-folder/corrupt-2";
    private ImmutableList<String> files = ImmutableList.of(CORRUPT_1, CORRUPT_2);

    private Path preservation;
    private Path staging;
    private Path backup;

    private RsyncStageManager manager;
    private RsyncConfiguration rsyncConfiguration;
    private RepairConfiguration repairConfiguration;

    @Before
    public void setup() throws URISyntaxException {
        URL root = ClassLoader.getSystemClassLoader().getResource("");

        preservation = Paths.get(root.toURI()).resolve("preservation");
        staging = Paths.get(root.toURI()).resolve("staging");
        backup = Paths.get(root.toURI()).resolve("backup");

        repairConfiguration = new RepairConfiguration();
        repairConfiguration.setStage(backup.toString());
        repairConfiguration.setPreservation(preservation.toString());

        rsyncConfiguration = new RsyncConfiguration();
        rsyncConfiguration.setStage(staging.toString());
        rsyncConfiguration.setPath("staging");
        rsyncConfiguration.setServer("test-host.umiacs.edu");

        manager = new RsyncStageManager(repairConfiguration, rsyncConfiguration);
    }

    @Test
    public void stage() throws Exception {
        Long repairId = 100L;
        Repair repair = new Repair();
        repair.setId(repairId);
        repair.setTo(TO);
        repair.setDepositor(DEPOSITOR);
        repair.setCollection(COLLECTION_STAGE);
        repair.setFiles(files);
        StagingResult stage = manager.stage(repair);

        // Assert that our operation completed successfully
        Assert.assertTrue(stage.isSuccess());

        // Assert that our strategy is correct
        FulfillmentStrategy strategy = stage.getStrategy();
        Assert.assertEquals(FulfillmentType.NODE_TO_NODE, strategy.getType());
        RsyncStrategy reified = (RsyncStrategy) strategy;
        String link = TO + "@" + rsyncConfiguration.getServer() + ":" + rsyncConfiguration.getPath() + "/" + repairId;
        Assert.assertEquals(link, reified.getLink());

        // Assert that the files were made
        Path corrupt = Paths.get(rsyncConfiguration.getStage(), repairId.toString(), CORRUPT_1);
        Path corruptSub = Paths.get(rsyncConfiguration.getStage(), repairId.toString(), CORRUPT_2);
        Assert.assertTrue(corrupt.toFile().exists());
        Assert.assertTrue(corruptSub.toFile().exists());

        // clean up our staged files
        cleanStage(repairId, files);
    }

    @Test
    public void stageAlreadyExists() throws Exception {
        long repairId = 101L;
        Repair repair = new Repair();
        repair.setId(repairId);
        repair.setTo(TO);
        repair.setDepositor(DEPOSITOR);
        repair.setCollection(COLLECTION_STAGE);
        repair.setFiles(files);
        StagingResult stage = manager.stage(repair);
        StagingResult duplicate = manager.stage(repair);

        // Assert that our operation completed successfully
        Assert.assertFalse(duplicate.isSuccess());

        // clean up our staged files
        cleanStage(repairId, files);
    }

    /**
     * Clean up after ourselves
     *
     * @param id the id of the staged replications
     * @param files the files of the replication
     * @throws IOException if there's an error removing files
     */
    private void cleanStage(Long id, List<String> files) throws IOException {
        files.forEach(file -> {
            Path path = staging.resolve(id.toString()).resolve(file);
            log.info("Removing {}", path);
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
        });
    }

    @Test
    public void clean() throws Exception {
        String COLLECTION_CLEAN = "test-clean";
        String CLEAN_1 = "/data/clean-1";
        String CLEAN_2 = "/data/clean-2";
        String CLEAN_3 = "/data/sub/clean-3";
        ImmutableList<String> files = ImmutableList.of(CLEAN_1, CLEAN_2, CLEAN_3);

        Repair repair = new Repair();
        repair.setTo(TO);
        repair.setDepositor(DEPOSITOR);
        repair.setCollection(COLLECTION_CLEAN);
        repair.setFiles(files);

        Staging.populate(Paths.get(rsyncConfiguration.getStage(), DEPOSITOR, COLLECTION_CLEAN), repair);
        boolean clean = manager.clean(repair);
        Assert.assertTrue(clean);

        // Assert that our files no longer exist
        // todo: should also check that the directories are removed
        for (String file : files) {
            Path path = Paths.get(rsyncConfiguration.getStage(), repair.getDepositor(), repair.getCollection(), file);
            Assert.assertFalse(path.toFile().exists());
        }
    }

}