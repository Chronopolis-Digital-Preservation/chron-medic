package org.chronopolis.medic.runners;

import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.client.StageManager;
import org.chronopolis.medic.client.StagingResult;
import org.chronopolis.medic.support.CallWrapper;
import org.chronopolis.medic.support.NotFoundCallWrapper;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.FulfillmentStrategy;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RsyncStrategy;
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
public class FulfillmentStagerTest {

    private Repair repair;
    private Fulfillment fulfillment;
    private FulfillmentStager stager;

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

        stager = new FulfillmentStager(repairs, manager, fulfillment);
    }

    @Test
    public void runSuccess() throws Exception {
        StagingResult result = new StagingResult();
        result.setSuccess(true);
        result.setStrategy(new RsyncStrategy());

        // setup our mocks
        when(repairs.getRepair(eq(fulfillment.getRepair()))).thenReturn(new CallWrapper<>(repair));
        when(manager.stage(any(Repair.class))).thenReturn(result); // Use eq once we confirm repair methods work
        when(repairs.readyFulfillment(eq(fulfillment.getId()), any(FulfillmentStrategy.class))).thenReturn(new CallWrapper<>(fulfillment));

        stager.run();
        verify(repairs, times(1)).getRepair(eq(fulfillment.getRepair()));
        verify(manager, times(1)).stage(any(Repair.class));
        verify(repairs, times(1)).readyFulfillment(eq(fulfillment.getId()), any(FulfillmentStrategy.class));
    }

    @Test
    public void runFailedStage() throws Exception {
        StagingResult result = new StagingResult();
        result.setSuccess(false);
        // setup our mocks
        when(repairs.getRepair(eq(fulfillment.getRepair()))).thenReturn(new CallWrapper<>(repair));
        when(manager.stage(any(Repair.class))).thenReturn(result); // Use eq once we confirm repair methods work

        stager.run();
        verify(repairs, times(1)).getRepair(eq(fulfillment.getRepair()));
        verify(manager, times(1)).stage(any(Repair.class));
        verify(repairs, times(0)).readyFulfillment(eq(fulfillment.getId()), any(FulfillmentStrategy.class));
    }

    @Test
    public void runFailedGet() throws Exception {
        // setup our mocks
        when(repairs.getRepair(eq(fulfillment.getRepair()))).thenReturn(new NotFoundCallWrapper<>(repair));

        stager.run();
        verify(repairs, times(1)).getRepair(eq(fulfillment.getRepair()));
        verify(manager, times(0)).stage(any(Repair.class));
        verify(repairs, times(0)).readyFulfillment(eq(fulfillment.getId()), any(FulfillmentStrategy.class));
    }

}