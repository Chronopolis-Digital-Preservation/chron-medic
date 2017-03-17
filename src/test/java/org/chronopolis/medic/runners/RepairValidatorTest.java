package org.chronopolis.medic.runners;

import org.chronopolis.medic.client.CompareResult;
import org.chronopolis.medic.client.RepairManager;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.support.CallWrapper;
import org.chronopolis.rest.models.repair.Fulfillment;
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
 * Validate runner tests
 *
 * Created by shake on 3/17/17.
 */
public class RepairValidatorTest {

    private Repair repair;
    private Fulfillment fulfillment;
    private RepairValidator validator;

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

        validator = new RepairValidator(repair, repairs, manager);
    }

    @Test
    public void testValidate() {
        when(manager.validate(eq(repair))).thenReturn(CompareResult.VALID);
        when(repairs.fulfillmentValidated(eq(repair.getFulfillment()))).thenReturn(new CallWrapper<>(fulfillment));
        validator.run();

        verify(manager, times(1)).validate(eq(repair));
        verify(repairs, times(1)).fulfillmentValidated(eq(repair.getFulfillment()));
    }

    @Test
    public void testInvalid() {
        when(manager.validate(eq(repair))).thenReturn(CompareResult.INVALID);
        validator.run();

        verify(manager, times(1)).validate(eq(repair));
        verify(repairs, times(0)).fulfillmentValidated(eq(repair.getFulfillment()));
    }

    @Test
    public void testConnectError() {
        when(manager.validate(eq(repair))).thenReturn(CompareResult.CONNECTION_ERROR);
        validator.run();

        verify(manager, times(1)).validate(eq(repair));
        verify(repairs, times(0)).fulfillmentValidated(eq(repair.getFulfillment()));
    }

}