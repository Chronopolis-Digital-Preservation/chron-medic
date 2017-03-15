package org.chronopolis.medic.runners;

import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.support.CallWrapper;
import org.chronopolis.medic.support.NotFoundCallWrapper;
import org.chronopolis.rest.models.repair.AuditStatus;
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
public class RepairAuditorTest {

    private Repair repair;
    private Fulfillment fulfillment;
    private RepairAuditor validator;

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

        validator = new RepairAuditor(repair, repairs, manager);
    }

    /**
     * Test when AuditStatus.SUCCESS is returned
     *
     * @throws Exception any exception which occurs
     */
    @Test
    public void runSuccess() throws Exception {
        AuditStatus success = AuditStatus.SUCCESS;
        Fulfillment fulfillment = new Fulfillment();
        fulfillment.setId(1L).setRepair(2L).setStatus(FulfillmentStatus.TRANSFERRED);

        when(repairs.getFulfillment(eq(repair.getFulfillment()))).thenReturn(new CallWrapper<>(fulfillment));
        when(manager.audit(any(Repair.class))).thenReturn(success);
        when(repairs.repairAudited(eq(repair.getId()), eq(success))).thenReturn(new CallWrapper<>(repair));

        validator.run();

        verify(repairs, times(1)).getFulfillment(eq(repair.getFulfillment()));
        verify(manager, times(1)).audit(any(Repair.class));
        verify(repairs, times(1)).repairAudited(eq(repair.getId()), eq(success));
    }

    /**
     * Test when we're unable to find the correlated Fulfillment
     *
     * @throws Exception any exception which occurs
     */
    @Test
    public void runFailedGet() throws Exception {
        Fulfillment fulfillment = new Fulfillment();
        when(repairs.getFulfillment(eq(repair.getFulfillment()))).thenReturn(new NotFoundCallWrapper<>(fulfillment));

        validator.run();

        verify(repairs, times(1)).getFulfillment(eq(repair.getFulfillment()));
        verify(manager, times(0)).audit(any(Repair.class));
        verify(repairs, times(0)).repairAudited(eq(repair.getId()), any(AuditStatus.class));
    }


    /**
     * There's no _real_ difference between success and this - do we need it?
     *
     * @throws Exception any exception which occurs
     */
    @Test
    public void runAuditOngoing() throws Exception {
        AuditStatus auditing = AuditStatus.AUDITING;
        Fulfillment fulfillment = new Fulfillment();
        fulfillment.setId(1L).setRepair(2L).setStatus(FulfillmentStatus.TRANSFERRED);

        when(repairs.getFulfillment(eq(repair.getFulfillment()))).thenReturn(new CallWrapper<>(fulfillment));
        when(manager.audit(any(Repair.class))).thenReturn(auditing);
        when(repairs.repairAudited(eq(repair.getId()), eq(auditing))).thenReturn(new CallWrapper<>(repair));

        validator.run();

        verify(repairs, times(1)).getFulfillment(eq(repair.getFulfillment()));
        verify(manager, times(1)).audit(any(Repair.class));
        verify(repairs, times(1)).repairAudited(eq(repair.getId()), eq(auditing));
    }

    /**
     * Test against a Fulfillment which is not transferred
     *
     * @throws Exception any exception which occurs
     */
    @Test
    public void runFulfillmentNotTransferred() throws Exception {
        Fulfillment fulfillment = new Fulfillment().setStatus(FulfillmentStatus.FAILED);
        when(repairs.getFulfillment(eq(repair.getFulfillment()))).thenReturn(new CallWrapper<>(fulfillment));

        validator.run();

        verify(repairs, times(1)).getFulfillment(eq(repair.getFulfillment()));
        verify(manager, times(0)).audit(any(Repair.class));
        verify(repairs, times(0)).repairAudited(eq(repair.getId()), any(AuditStatus.class));
    }

}