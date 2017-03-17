package org.chronopolis.medic.runners;

import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.support.CallWrapper;
import org.chronopolis.rest.models.repair.Repair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests
 *
 * Created by shake on 3/17/17.
 */
public class RepairCopierTest {

    private Repair repair;
    private RepairCopier copier;

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

        copier = new RepairCopier(repair, repairs, manager);
    }

    @Test
    public void testCopy() {
        when(manager.replace(eq(repair))).thenReturn(true);
        when(repairs.repairCopied(eq(repair.getId()))).thenReturn(new CallWrapper<>(repair));
        copier.run();

        verify(manager, times(1)).replace(eq(repair));
        verify(repairs, times(1)).repairCopied(eq(repair.getId()));

    }

    @Test
    public void testCopyFail() {
        when(manager.replace(eq(repair))).thenReturn(false);
        copier.run();

        verify(manager, times(1)).replace(eq(repair));
        verify(repairs, times(0)).repairCopied(eq(repair.getId()));
    }
}
