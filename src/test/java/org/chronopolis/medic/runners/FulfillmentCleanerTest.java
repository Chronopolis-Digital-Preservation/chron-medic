package org.chronopolis.medic.runners;

import org.chronopolis.medic.client.Repairs;
import org.chronopolis.medic.client.StageManager;
import org.chronopolis.medic.support.CallWrapper;
import org.chronopolis.rest.models.repair.Repair;
import org.junit.Before;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * todo: this currently does nothing. we should create some files for it to clean.
 *
 * Created by shake on 3/3/17.
 */
public class FulfillmentCleanerTest {

    private Repair repair;
    private FulfillmentCleaner cleaner;

    @Mock
    private Repairs repairs;
    @Mock
    private StageManager manager;

    @Before
    public void before() {
        repairs = mock(Repairs.class);
        manager = mock(StageManager.class);
        repair = new Repair();

        cleaner = new FulfillmentCleaner(repairs, manager);
    }

    // @Test
    public void runSuccess() throws Exception {
        // setup our mocks
        when(manager.clean(any(Repair.class))).thenReturn(true); // Use eq once we confirm repair methods work
        when(repairs.repairCleaned(eq(repair.getId()))).thenReturn(new CallWrapper<>(repair));

        cleaner.run();
        verify(manager, times(1)).clean(any(Repair.class));
        verify(repairs, times(1)).repairCleaned(eq(repair.getId()));
    }

    // @Test
    public void runFailedClean() throws Exception {
        // setup our mocks
        when(manager.clean(any(Repair.class))).thenReturn(false); // Use eq once we confirm repair methods work

        cleaner.run();
        verify(manager, times(1)).clean(any(Repair.class));
        verify(repairs, times(0)).repairCleaned(eq(repair.getId()));
    }

}