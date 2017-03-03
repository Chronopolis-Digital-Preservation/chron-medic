package org.chronopolis.medic.runners;

import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.client.StageManager;
import org.chronopolis.medic.support.CallWrapper;
import org.chronopolis.medic.support.NotFoundCallWrapper;
import org.chronopolis.rest.models.repair.Fulfillment;
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
public class FulfillmentCleanerTest {

    private Repair repair;
    private Fulfillment fulfillment;
    private FulfillmentCleaner cleaner;

    @Mock
    private Repairs repairs;
    @Mock
    private StageManager manager;

    @Before
    public void before() {
        repairs = mock(Repairs.class);
        manager = mock(StageManager.class);
        fulfillment = new Fulfillment();
        fulfillment.setId(1L);
        fulfillment.setRepair(2L);
        repair = new Repair();

        cleaner = new FulfillmentCleaner(repairs, manager, fulfillment);
    }

    @Test
    public void runSuccess() throws Exception {
        // setup our mocks
        when(repairs.getRepair(eq(fulfillment.getRepair()))).thenReturn(new CallWrapper<>(repair));
        when(manager.clean(any(Repair.class))).thenReturn(true); // Use eq once we confirm repair methods work
        when(repairs.fulfillmentCleaned(eq(fulfillment.getId()))).thenReturn(new CallWrapper<>(fulfillment));

        cleaner.run();
        verify(repairs, times(1)).getRepair(eq(fulfillment.getRepair()));
        verify(manager, times(1)).clean(any(Repair.class));
        verify(repairs, times(1)).fulfillmentCleaned(eq(fulfillment.getId()));
    }

    @Test
    public void runFailedClean() throws Exception {
        // setup our mocks
        when(repairs.getRepair(eq(fulfillment.getRepair()))).thenReturn(new CallWrapper<>(repair));
        when(manager.clean(any(Repair.class))).thenReturn(false); // Use eq once we confirm repair methods work

        cleaner.run();
        verify(repairs, times(1)).getRepair(eq(fulfillment.getRepair()));
        verify(manager, times(1)).clean(any(Repair.class));
        verify(repairs, times(0)).fulfillmentCleaned(eq(fulfillment.getId()));
    }

    @Test
    public void runFailedGet() throws Exception {
        // setup our mocks
        when(repairs.getRepair(eq(fulfillment.getRepair()))).thenReturn(new NotFoundCallWrapper<>(repair));

        cleaner.run();
        verify(repairs, times(1)).getRepair(eq(fulfillment.getRepair()));
        verify(manager, times(0)).clean(any(Repair.class));
        verify(repairs, times(0)).fulfillmentCleaned(eq(fulfillment.getId()));
    }

}