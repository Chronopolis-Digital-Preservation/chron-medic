package org.chronopolis.medic.client.impl;

import com.google.common.collect.ImmutableList;
import org.chronopolis.common.ace.AceService;
import org.chronopolis.common.ace.GsonCollection;
import org.chronopolis.medic.config.repair.RepairConfiguration;
import org.chronopolis.medic.support.CallWrapper;
import org.chronopolis.medic.support.NotFoundCallWrapper;
import org.chronopolis.rest.models.repair.AuditStatus;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.FulfillmentType;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RsyncStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the repair manager implementation
 *
 * Created by shake on 3/6/17.
 */
public class RepairManTest {

    private final String DEPOSITOR = "test-depositor";
    private final String COLLECTION = "test-collection";

    @Mock
    private AceService ace;

    private RepairConfiguration configuration;
    private RepairMan manager;

    private Path preservation;
    private Path staging;
    private Path backup;

    @Before
    public void setup() throws URISyntaxException {
        ace = mock(AceService.class);

        URL root = ClassLoader.getSystemClassLoader().getResource("");

        preservation = Paths.get(root.toURI()).resolve("preservation");
        staging = Paths.get(root.toURI()).resolve("staging");
        backup = Paths.get(root.toURI()).resolve("backup");

        configuration = new RepairConfiguration();
        configuration.setPreservation(preservation.toString());
        configuration.setStage(backup.toString());

        manager = new RepairMan(ace, configuration);
    }

    @Test
    public void backup() throws Exception {
        String collection = "test-backup";
        ImmutableList<String> files = ImmutableList.of("data/backup-1", "data/sub/backup-2");
        Repair repair = new Repair()
                .setFiles(files)
                .setCollection(collection)
                .setDepositor(DEPOSITOR);

        boolean success = manager.replace(repair);

        Assert.assertTrue(success);
        for (String file : files) {
            Path path = backup.resolve(DEPOSITOR).resolve(collection).resolve(file);
            Assert.assertTrue(path.toFile().exists());
        }
    }

    @Test
    public void removeBackup() throws Exception {
        String collection = "test-remove";
        ImmutableList<String> files = ImmutableList.of("data/remove-1", "data/sub/remove-2");
        Repair repair = new Repair()
                .setDepositor(DEPOSITOR)
                .setCollection(collection)
                .setFiles(files);

        boolean success = manager.clean(repair);

        Assert.assertTrue(success);
        for (String file : files) {
            Path path = backup.resolve(DEPOSITOR).resolve(collection).resolve(file);
            Assert.assertFalse(path.toFile().exists());
        }
    }

    @Test
    public void replicateAce() throws Exception {
        Fulfillment fulfillment = new Fulfillment()
                .setType(FulfillmentType.ACE);
        Repair repair = new Repair();

        boolean success = manager.replicate(fulfillment, repair);
        Assert.assertFalse(success);
    }

    @Test
    public void replicateRsync() throws Exception {
        boolean success = rsync("test-clean");
        Assert.assertTrue(success);
    }

    @Test
    public void replicateRsyncFail() throws Exception {
        boolean success = rsync("test-not-exists");
        Assert.assertFalse(success);
    }

    /**
     * run manager.replicate with an rsync strategy
     *
     * @param collection the collection to attempt
     * @return the return value of manager.replicate
     */
    private boolean rsync(String collection) {
        RsyncStrategy strategy = new RsyncStrategy()
                .setLink(staging.resolve(DEPOSITOR).resolve(collection).toString());
        Fulfillment fulfillment = new Fulfillment()
                .setCredentials(strategy)
                .setType(FulfillmentType.NODE_TO_NODE);
        Repair repair = new Repair()
                .setDepositor(DEPOSITOR)
                .setCollection(collection);

        return manager.replicate(fulfillment, repair);
    }

    @Test
    public void startAudit() throws Exception {
        Repair repair = new Repair()
                .setDepositor(DEPOSITOR)
                .setCollection(COLLECTION)
                .setAudit(AuditStatus.PRE);

        GsonCollection collection = new GsonCollection.Builder()
                .group(DEPOSITOR)
                .name(COLLECTION)
                .build();
        collection.setId(1L);

        when(ace.getCollectionByName(eq(COLLECTION), eq(DEPOSITOR))).thenReturn(new CallWrapper<>(collection));
        when(ace.startAudit(eq(collection.getId()), eq(true))).thenReturn(new CallWrapper<>(null)); // uhhh I guess
        AuditStatus status = manager.audit(repair);

        Assert.assertEquals(AuditStatus.AUDITING, status);
        verify(ace, times(1)).getCollectionByName(eq(COLLECTION), eq(DEPOSITOR));
        verify(ace, times(1)).startAudit(eq(collection.getId()), eq(true));
    }

    @Test
    public void startAuditFail() throws Exception {
        Repair repair = new Repair()
                .setDepositor(DEPOSITOR)
                .setCollection(COLLECTION)
                .setAudit(AuditStatus.PRE);

        GsonCollection collection = new GsonCollection.Builder()
                .group(DEPOSITOR)
                .name(COLLECTION)
                .build();
        collection.setId(1L);

        when(ace.getCollectionByName(eq(COLLECTION), eq(DEPOSITOR))).thenReturn(new NotFoundCallWrapper<>(collection));
        AuditStatus status = manager.audit(repair);

        Assert.assertEquals(AuditStatus.PRE, status);
        verify(ace, times(1)).getCollectionByName(eq(COLLECTION), eq(DEPOSITOR));
        verify(ace, times(0)).startAudit(eq(collection.getId()), eq(true));
    }

    @Test
    public void checkAuditSuccess() throws Exception {
        Repair repair = new Repair()
                .setDepositor(DEPOSITOR)
                .setCollection(COLLECTION)
                .setAudit(AuditStatus.AUDITING);

        GsonCollection collection = new GsonCollection.Builder()
                .state("A")
                .group(DEPOSITOR)
                .name(COLLECTION)
                .build();
        collection.setId(1L);

        when(ace.getCollectionByName(eq(COLLECTION), eq(DEPOSITOR))).thenReturn(new CallWrapper<>(collection));
        AuditStatus status = manager.audit(repair);

        Assert.assertEquals(AuditStatus.SUCCESS, status);
        verify(ace, times(1)).getCollectionByName(eq(COLLECTION), eq(DEPOSITOR));
    }

    @Test
    public void checkAuditOngoing() throws Exception {
    }

}