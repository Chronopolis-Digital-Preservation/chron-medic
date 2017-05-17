package org.chronopolis.medic.runners;

import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.support.CallWrapper;
import org.chronopolis.rest.models.repair.AuditStatus;
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
        repair.setId(2L);

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

        when(manager.audit(any(Repair.class))).thenReturn(success);
        when(repairs.repairAudited(eq(repair.getId()), eq(success))).thenReturn(new CallWrapper<>(repair));
        when(repairs.repairComplete(eq(repair.getId()))).thenReturn(new CallWrapper<>(repair));

        validator.run();

        verify(manager, times(1)).audit(any(Repair.class));
        verify(repairs, times(1)).repairAudited(eq(repair.getId()), eq(success));
        verify(repairs, times(1)).repairComplete(eq(repair.getId()));
    }

    /**
     *
     *
     * @throws Exception any exception which occurs
     */
    @Test
    public void runAuditOngoing() throws Exception {
        AuditStatus auditing = AuditStatus.AUDITING;

        when(manager.audit(any(Repair.class))).thenReturn(auditing);
        when(repairs.repairAudited(eq(repair.getId()), eq(auditing))).thenReturn(new CallWrapper<>(repair));

        validator.run();

        verify(manager, times(1)).audit(any(Repair.class));
        verify(repairs, times(1)).repairAudited(eq(repair.getId()), eq(auditing));
        verify(repairs, times(0)).repairComplete(eq(repair.getId()));
    }

}