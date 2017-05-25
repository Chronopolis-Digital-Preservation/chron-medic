package org.chronopolis.medic.client.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.chronopolis.common.ace.AceService;
import org.chronopolis.common.ace.CompareRequest;
import org.chronopolis.common.ace.CompareResponse;
import org.chronopolis.common.ace.GsonCollection;
import org.chronopolis.medic.client.CompareResult;
import org.chronopolis.medic.config.repair.RepairConfiguration;
import org.chronopolis.medic.support.CallWrapper;
import org.chronopolis.medic.support.NotFoundCallWrapper;
import org.chronopolis.medic.support.Staging;
import org.chronopolis.rest.models.repair.AuditStatus;
import org.chronopolis.rest.models.repair.FulfillmentType;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RsyncStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Matchers.any;
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
    private final Logger log = LoggerFactory.getLogger(RepairManTest.class);

    private final Long PRESERVE_ID = 2L;
    private final Long VALIDATE_ID = 4L;
    private final Long POPULDATE_ID = 3L;
    private final Long ACE_COLLECTION_ID = 1L;

    private final String DEPOSITOR = "test-depositor";
    private final String COLLECTION = "test-collection";
    private final String COLLECTION_VALIDATE = "test-validate";

    @Mock
    private AceService ace;

    private RepairConfiguration configuration;
    private RepairMan manager;

    private Path preservation;
    private Path staging;

    @Before
    public void setup() throws URISyntaxException {
        ace = mock(AceService.class);

        URL root = ClassLoader.getSystemClassLoader().getResource("");

        preservation = Paths.get(root.toURI()).resolve("preservation");
        staging = Paths.get(root.toURI()).resolve("staging");

        configuration = new RepairConfiguration();
        configuration.setPreservation(preservation.toString());
        configuration.setStage(staging.toString());

        manager = new RepairMan(ace, configuration);
    }

    @Test
    public void replace() throws Exception {
        Long replaceId = PRESERVE_ID;
        String collection = "test-preserve";
        ImmutableList<String> relative = ImmutableList.of("data/backup-1", "data/sub/backup-2");
        ImmutableList<String> files = ImmutableList.of("/data/backup-1", "/data/sub/backup-2");
        Repair repair = new Repair()
                .setId(replaceId)
                .setFiles(files)
                .setCollection(collection)
                .setDepositor(DEPOSITOR);

        boolean success = manager.replace(repair);

        Assert.assertTrue(success);
        for (String file : relative) {
            Path path = preservation.resolve(DEPOSITOR).resolve(collection).resolve(file);
            Assert.assertTrue(path.toFile().exists());
        }
    }

    @Test
    public void remove() throws Exception {
        String collection = "test-remove";
        Path remove = staging.resolve(POPULDATE_ID.toString());
        ImmutableList<String> files = ImmutableList.of("/data/remove-1", "/data/sub/remove-2");
        Repair repair = new Repair()
                .setId(POPULDATE_ID)
                .setDepositor(DEPOSITOR)
                .setCollection(collection)
                .setFiles(files);

        Staging.populate(remove, repair);
        boolean success = manager.clean(repair);

        Assert.assertTrue(success);
        for (String file : files) {
            Path path = Paths.get(remove.toString(), file);
            Assert.assertFalse(path.toFile().exists());
        }
    }

    @Test
    public void replicateAce() throws Exception {
        Repair repair = new Repair();
        repair.setFrom("test-repair-from");
        repair.setType(FulfillmentType.ACE);

        boolean success = manager.replicate(repair);
        Assert.assertFalse(success);
    }

    @Test
    public void replicateRsync() throws Exception {
        boolean success = rsync("test-preserve", PRESERVE_ID);
        Assert.assertTrue(success);
    }

    @Test
    public void replicateRsyncFail() throws Exception {
        // use the populate id as it does not exist in staging
        boolean success = rsync("test-not-exists", POPULDATE_ID);
        Assert.assertFalse(success);
    }

    /**
     * run manager.replicate with an rsync strategy
     *
     * @param collection the collection to attempt
     * @param id
     * @return the return value of manager.replicate
     */
    private boolean rsync(String collection, Long id) {
        RsyncStrategy strategy = new RsyncStrategy()
                .setLink(staging.resolve(id.toString()).toString());
        Repair repair = new Repair()
                .setId(id)
                .setCredentials(strategy)
                .setType(FulfillmentType.NODE_TO_NODE)
                .setDepositor(DEPOSITOR)
                .setCollection(collection);

        return manager.replicate(repair);
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
        collection.setId(ACE_COLLECTION_ID);

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
        collection.setId(ACE_COLLECTION_ID);

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
        collection.setId(ACE_COLLECTION_ID);

        when(ace.getCollectionByName(eq(COLLECTION), eq(DEPOSITOR))).thenReturn(new CallWrapper<>(collection));
        AuditStatus status = manager.audit(repair);

        Assert.assertEquals(AuditStatus.SUCCESS, status);
        verify(ace, times(1)).getCollectionByName(eq(COLLECTION), eq(DEPOSITOR));
    }

    @Test
    public void checkAuditOngoing() throws Exception {
    }

    @Test
    public void testValidateSuccess() {
        String file = "/data/file-1";
        Long id = ACE_COLLECTION_ID;
        CompareResult result = validate(ImmutableList.of(file), ImmutableSet.of(file), ImmutableSet.of(), ImmutableSet.of(), id);

        Assert.assertNotNull(result);
        Assert.assertEquals(CompareResult.VALID, result);
        verify(ace, times(1)).getCollectionByName(eq(COLLECTION_VALIDATE), eq(DEPOSITOR));
        verify(ace, times(1)).compareToCollection(eq(id), any(CompareRequest.class));
    }

    @Test
    public void testValidateFileNotFound() {
        String file = "/data/file-1";
        String notFound = "/data/file-not-found";
        Long id = ACE_COLLECTION_ID;

        CompareResult result = validate(ImmutableList.of(file, notFound), ImmutableSet.of(file), ImmutableSet.of(), ImmutableSet.of(notFound), id);

        Assert.assertNotNull(result);
        Assert.assertEquals(CompareResult.INVALID, result);
        verify(ace, times(1)).getCollectionByName(eq(COLLECTION_VALIDATE), eq(DEPOSITOR));
        verify(ace, times(1)).compareToCollection(eq(id), any(CompareRequest.class));
    }

    @Test
    public void testValidateFileIsDifferent() {
        String file = "/data/file-1";
        String invalid = "/data/file-invalid";
        Long id = ACE_COLLECTION_ID;

        CompareResult result = validate(ImmutableList.of(file, invalid), ImmutableSet.of(file), ImmutableSet.of(invalid), ImmutableSet.of(), id);

        Assert.assertNotNull(result);
        Assert.assertEquals(CompareResult.INVALID, result);
        verify(ace, times(1)).getCollectionByName(eq(COLLECTION_VALIDATE), eq(DEPOSITOR));
        verify(ace, times(1)).compareToCollection(eq(id), any(CompareRequest.class));
    }

    private CompareResult validate(ImmutableList<String> files,
                                   ImmutableSet<String> match,
                                   ImmutableSet<String> diff,
                                   ImmutableSet<String> notFound,
                                   Long collectionId) {
        Repair repair = new Repair()
                .setId(VALIDATE_ID)
                .setFiles(files)
                .setDepositor(DEPOSITOR)
                .setCollection(COLLECTION_VALIDATE)
                .setAudit(AuditStatus.AUDITING);

        GsonCollection collection = new GsonCollection.Builder()
                .state("A")
                .group(DEPOSITOR)
                .name(COLLECTION_VALIDATE)
                .build();
        collection.setId(collectionId);

        CompareResponse response = new CompareResponse();
        response.setMatch(match);
        response.setDiff(diff);
        response.setNotFound(notFound);

        when(ace.getCollectionByName(eq(COLLECTION_VALIDATE), eq(DEPOSITOR))).thenReturn(new CallWrapper<>(collection));
        when(ace.compareToCollection(eq(collection.getId()), any(CompareRequest.class))).thenReturn(new CallWrapper<>(response));

        return manager.validate(repair);
    }

}