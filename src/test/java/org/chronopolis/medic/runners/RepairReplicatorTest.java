package org.chronopolis.medic.runners;

import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.support.CallWrapper;
import org.chronopolis.medic.support.NotFoundCallWrapper;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.FulfillmentStatus;
import org.chronopolis.rest.models.repair.Repair;
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
    private Fulfillment fulfillment;
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
        fulfillment = new Fulfillment();
        fulfillment.setId(1L);
        fulfillment.setRepair(2L);

        replicator = new RepairReplicator(repair, repairs, manager);
    }

    @Test
    public void runSuccess() throws Exception {
        when(repairs.getFulfillment(eq(repair.getFulfillment()))).thenReturn(new CallWrapper<>(fulfillment));
        when(manager.replicate(any(Fulfillment.class), any(Repair.class))).thenReturn(true);
        when(repairs.fulfillmentUpdated(eq(repair.getFulfillment()), eq(FulfillmentStatus.TRANSFERRED))).thenReturn(new CallWrapper<>(fulfillment));

        replicator.run();

        verify(repairs, times(1)).getFulfillment(eq(repair.getFulfillment()));
        verify(manager, times(1)).replicate(any(Fulfillment.class), any(Repair.class));
        verify(repairs, times(1)).fulfillmentUpdated(eq(repair.getFulfillment()), eq(FulfillmentStatus.TRANSFERRED));
    }

    @Test
    public void runFailedReplicate() throws Exception {
        when(repairs.getFulfillment(eq(repair.getFulfillment()))).thenReturn(new CallWrapper<>(fulfillment));
        when(manager.replicate(any(Fulfillment.class), any(Repair.class))).thenReturn(false);

        replicator.run();

        verify(repairs, times(1)).getFulfillment(eq(repair.getFulfillment()));
        verify(manager, times(1)).replicate(any(Fulfillment.class), any(Repair.class));
        verify(repairs, times(0)).fulfillmentUpdated(eq(repair.getFulfillment()), eq(FulfillmentStatus.TRANSFERRED));
    }

    @Test
    public void runFailedGet() throws Exception {
        // setup our mocks
        when(repairs.getFulfillment(eq(repair.getFulfillment()))).thenReturn(new NotFoundCallWrapper<>(fulfillment));

        replicator.run();

        verify(repairs, times(1)).getFulfillment(eq(repair.getFulfillment()));
        verify(manager, times(0)).replicate(any(Fulfillment.class), any(Repair.class));
        verify(repairs, times(0)).fulfillmentUpdated(eq(repair.getFulfillment()), eq(FulfillmentStatus.TRANSFERRED));
    }

}