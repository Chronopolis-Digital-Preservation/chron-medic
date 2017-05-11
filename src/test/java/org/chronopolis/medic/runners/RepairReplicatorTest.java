package org.chronopolis.medic.runners;

import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.support.CallWrapper;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RepairStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * Created by shake on 3/3/17.
 */
public class RepairReplicatorTest {

    private Repair repair;
    private RepairReplicator replicator;

    @Mock
    private Repairs repairs;
    @Mock
    private RepairManager manager;

    @Before
    public void before() {
        repairs = mock(Repairs.class);
        manager = mock(RepairManager.class);
        repair = new Repair();
        repair.setFulfillment(1L);
        repair.setId(2L);

        replicator = new RepairReplicator(repair, repairs, manager);
    }

    @Test
    public void runSuccess() throws Exception {
        when(manager.replicate(any(Repair.class))).thenReturn(true);
        when(repairs.repairUpdate(eq(repair.getId()), eq(RepairStatus.TRANSFERRED))).thenReturn(new CallWrapper<>(repair));

        replicator.run();

        verify(manager, times(1)).replicate(any(Repair.class));
        verify(repairs, times(1)).repairUpdate(eq(repair.getId()), eq(RepairStatus.TRANSFERRED));
    }

    @Test
    public void runFailedReplicate() throws Exception {
        when(manager.replicate(any(Repair.class))).thenReturn(false);

        replicator.run();

        verify(manager, times(1)).replicate(any(Repair.class));
        verify(repairs, times(0)).repairUpdate(eq(repair.getId()), eq(RepairStatus.TRANSFERRED));
    }

}