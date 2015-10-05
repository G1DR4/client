// Copyright 2015 The Project Buendia Authors
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at: http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distrib-
// uted under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
// OR CONDITIONS OF ANY KIND, either express or implied.  See the License for
// specific language governing permissions and limitations under the License.

package org.projectbuendia.client.ui.sync;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;

import org.projectbuendia.client.R;
import org.projectbuendia.client.events.FetchXformSucceededEvent;
import org.projectbuendia.client.models.Patient;

import java.util.UUID;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;


/** Tests the loading of the encounter xform from the patient chart activity. */
public class PatientChartActivityXformSyncTest extends SyncTestCase {

    private static final String FORM_LABEL = "[test] Form";

    @Override public void setUp() throws Exception {
        super.setUp();
        click(viewWithText("Guest User"));
    }

    /**
     * Tests that clicking the load xform button after a fresh sync causes the xform to
     * eventually load.
     */
    public void testXformRetrievedFromServer() {

        loadChart();
        openEncounterForm();
        click(viewWithText("Discard"));
    }

    private void openEncounterForm() {

        Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());

        EventBusIdlingResource<FetchXformSucceededEvent> xformIdlingResource =
                new EventBusIdlingResource<FetchXformSucceededEvent>(
                        UUID.randomUUID().toString(), mEventBus);
        click(viewWithText(FORM_LABEL));
        Espresso.registerIdlingResources(xformIdlingResource);

        expectVisibleSoon(viewWithText("Encounter"));
    }

    private void loadChart() {

        waitForProgressFragment();
        // Open patient list.
        click(viewWithId(R.id.action_search));
        // waitForProgressFragment() doesn't quite work here as we're actually waiting on the
        // search button in the action bar to finish its loading task.
        expectVisibleSoon(viewThat(hasTextContaining("Triage")));
        // Click first patient.
        click(dataThat(is(Patient.class))
                .inAdapterView(withId(R.id.fragment_patient_list))
                .atPosition(0));

    }

}
