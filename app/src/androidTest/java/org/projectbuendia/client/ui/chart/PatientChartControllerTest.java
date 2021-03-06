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

package org.projectbuendia.client.ui.chart;

import android.test.AndroidTestCase;
import android.util.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.projectbuendia.client.R;
import org.projectbuendia.client.data.app.AppEncounter;
import org.projectbuendia.client.data.app.AppModel;
import org.projectbuendia.client.data.app.AppPatient;
import org.projectbuendia.client.events.CrudEventBus;
import org.projectbuendia.client.events.FetchXformFailedEvent;
import org.projectbuendia.client.events.FetchXformSucceededEvent;
import org.projectbuendia.client.events.SubmitXformFailedEvent;
import org.projectbuendia.client.events.SubmitXformSucceededEvent;
import org.projectbuendia.client.events.data.ItemFetchedEvent;
import org.projectbuendia.client.model.Concepts;
import org.projectbuendia.client.net.model.ConceptType;
import org.projectbuendia.client.sync.LocalizedChartHelper;
import org.projectbuendia.client.sync.LocalizedObs;
import org.projectbuendia.client.sync.Order;
import org.projectbuendia.client.sync.SyncManager;
import org.projectbuendia.client.ui.FakeEventBus;
import org.projectbuendia.client.ui.chart.PatientChartController.MinimalHandler;
import org.projectbuendia.client.ui.chart.PatientChartController.OdkResultSender;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for {@link PatientChartController}. */
public final class PatientChartControllerTest extends AndroidTestCase {

    private static final String PATIENT_UUID_1 = "uuid1";
    private static final String PATIENT_NAME_1 = "bob";
    private static final String PATIENT_ID_1 = "id1";

    private static final LocalizedObs OBSERVATION_A =
            new LocalizedObs(0, 0, "g", "c", "c", "TEXT", "val", "localizedVal");

    private PatientChartController mController;

    @Mock private AppModel mMockAppModel;
    @Mock private PatientChartController.Ui mMockUi;
    @Mock private OdkResultSender mMockOdkResultSender;
    @Mock private LocalizedChartHelper mMockChartHelper;
    @Mock private SyncManager mMockSyncManager;
    private FakeEventBus mFakeCrudEventBus;
    private FakeEventBus mFakeGlobalEventBus;
    private FakeHandler mFakeHandler;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        mFakeCrudEventBus = new FakeEventBus();
        mFakeGlobalEventBus = new FakeEventBus();
        mFakeHandler = new FakeHandler();
        mController = new PatientChartController(
                mMockAppModel,
                mFakeGlobalEventBus,
                mFakeCrudEventBus,
                mMockUi,
                PATIENT_UUID_1,
                mMockOdkResultSender,
                mMockChartHelper,
                null,
                mMockSyncManager,
                mFakeHandler);
    }

    /** Tests that suspend() unregisters from the event bus. */
    public void testSuspend_UnregistersFromEventBus() {
        // GIVEN an initialized controller
        mController.init();
        // WHEN the controller is suspended
        mController.suspend();
        // THEN the controller unregisters from the event bus
        assertEquals(0, mFakeCrudEventBus.countRegisteredReceivers());
    }

    /** Tests that init() requests a single patient from the app model. */
    public void testInit_RequestsPatientDetails() {
        // WHEN the controller is inited
        mController.init();
        // THEN it requests that patient's details be fetched
        mMockAppModel.fetchSinglePatient(mFakeCrudEventBus, PATIENT_UUID_1);
    }

    /** Tests that observations are updated in the UI when patient details fetched. */
    public void testPatientDetailsLoaded_SetsObservationsOnUi() {
        // GIVEN the observations provider is set up to return some dummy data
        List<LocalizedObs> allObservations =
                ImmutableList.of(OBSERVATION_A);
        Map<String, LocalizedObs> recentObservations =
                ImmutableMap.of(OBSERVATION_A.conceptUuid, OBSERVATION_A);
        when(mMockChartHelper.getObservations(PATIENT_UUID_1))
                .thenReturn(allObservations);
        when(mMockChartHelper.getMostRecentObservations(PATIENT_UUID_1))
                .thenReturn(recentObservations);
        // GIVEN controller is initialized
        mController.init();
        // WHEN that patient's details are loaded
        AppPatient patient = AppPatient.builder().build();
        mFakeCrudEventBus.post(new ItemFetchedEvent<>(patient));
        // TODO: When the handler UI updating hack in PatientChartController is removed, this can
        // also be removed.
        mFakeHandler.runUntilEmpty();
        // THEN the controller puts observations on the UI
        verify(mMockUi).updatePatientHistoryUi(
                new ArrayList<Pair<String, String>>(), allObservations, ImmutableList.<Order>of(), null, null);
        verify(mMockUi).updatePatientVitalsUi(recentObservations, null, null);
    }

    /** Tests that the UI is given updated patient data when patient data is fetched. */
    public void testPatientDetailsLoaded_UpdatesUi() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN that patient's details are loaded
        AppPatient patient = AppPatient.builder().build();
        mFakeCrudEventBus.post(new ItemFetchedEvent<>(patient));
        // THEN the controller updates the UI
        verify(mMockUi).updatePatientDetailsUi(patient);
    }

    /** Tests that selecting a new general condition results in adding a new encounter. */
    public void testSetCondition_AddsEncounterForNewCondition() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN a new general condition is set from the dialog
        mController.setCondition(Concepts.GENERAL_CONDITION_PALLIATIVE_UUID);
        // THEN a new encounter is added
        verify(mMockAppModel).addEncounter(
                any(CrudEventBus.class),
                any(AppPatient.class),
                any(AppEncounter.class));
    }

    /** Tests that requesting an xform through clicking 'add observation' shows loading dialog. */
    public void testAddObservation_showsLoadingDialog() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN 'add observation' is pressed
        mController.onAddObservationPressed();
        // THEN the controller displays the loading dialog
        verify(mMockUi).showFormLoadingDialog(true);
    }

    /** Tests that requesting an xform through clicking on a vital shows loading dialog. */
    public void testVitalClick_showsLoadingDialog() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN a vital is pressed
        mController.onAddObservationPressed("foo");
        // THEN the controller displays the loading dialog
        verify(mMockUi).showFormLoadingDialog(true);
    }

    /** Tests that requesting an xform through clicking on test results shows loading dialog. */
    public void testTestResultsClick_showsLoadingDialog() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN test results are pressed
        mController.onAddTestResultsPressed();
        // THEN the controller displays the loading dialog
        verify(mMockUi).showFormLoadingDialog(true);
    }

    /** Tests that the xform can be fetched again if the first fetch fails. */
    public void testXformLoadFailed_ReenablesXformFetch() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN an xform request fails
        mFakeGlobalEventBus.post(new FetchXformFailedEvent(FetchXformFailedEvent.Reason.UNKNOWN));
        // THEN the controller re-enables xform fetch
        verify(mMockUi).reEnableFetch();
    }

    /** Tests that an error message is displayed when the xform fails to load. */
    public void testXformLoadFailed_ShowsError() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN an xform request fails
        mFakeGlobalEventBus.post(new FetchXformFailedEvent(FetchXformFailedEvent.Reason.UNKNOWN));
        // THEN the controller displays an error message
        verify(mMockUi).showError(R.string.fetch_xform_failed_unknown_reason);
    }

    /** Tests that a failed xform fetch hides the loading dialog. */
    public void testXformLoadFailed_HidesLoadingDialog() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN an xform request fails
        mFakeGlobalEventBus.post(new FetchXformFailedEvent(FetchXformFailedEvent.Reason.UNKNOWN));
        // THEN the controller hides the loading dialog
        verify(mMockUi).showFormLoadingDialog(false);
    }

    /** Tests that the xform can be fetched again if the first fetch succeeds. */
    public void testXformLoadSucceeded_ReenablesXformFetch() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN an xform request succeeds
        mFakeGlobalEventBus.post(new FetchXformSucceededEvent());
        // THEN the controller re-enables xform fetch
        verify(mMockUi).reEnableFetch();
    }

    /** Tests that a successful xform fetch hides the loading dialog. */
    public void testXformLoadSucceeded_HidesLoadingDialog() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN an xform request succeeds
        mFakeGlobalEventBus.post(new FetchXformSucceededEvent());
        // THEN the controller hides the loading dialog
        verify(mMockUi).showFormLoadingDialog(false);
    }

    // TODO/completeness: Test that starting an xform submission shows the submission dialog.

    /** Tests that errors in xform submission are reported to the user. */
    public void testXformSubmitFailed_ShowsErrorMessage() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN an xform fails to submit
        mFakeGlobalEventBus.post(new SubmitXformFailedEvent(SubmitXformFailedEvent.Reason.UNKNOWN));
        // THEN the controller shows an error
        verify(mMockUi).showError(R.string.submit_xform_failed_unknown_reason);
    }

    /** Tests that errors in xform submission hide the submission dialog. */
    public void testXformSubmitFailed_HidesSubmissionDialog() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN an xform fails to submit
        mFakeGlobalEventBus.post(new SubmitXformFailedEvent(SubmitXformFailedEvent.Reason.UNKNOWN));
        // THEN the controller hides the submission dialog
        verify(mMockUi).showFormSubmissionDialog(false);
    }

    /** Tests that successful xform submission hides the submission dialog. */
    public void testXformSubmitSucceeded_EventuallyHidesSubmissionDialog() {
        // GIVEN controller is initialized
        mController.init();
        // WHEN an xform submits successfully
        mFakeGlobalEventBus.post(new SubmitXformSucceededEvent());
        // THEN the controller hides the submission dialog
        // TODO: When the handler UI updating hack in PatientChartController is removed, this can
        // also be removed.
        mFakeHandler.runUntilEmpty();
        verify(mMockUi).showFormSubmissionDialog(false);
    }

    private final class FakeHandler implements MinimalHandler {
        private final ArrayDeque<Runnable> mTasks = new ArrayDeque<>();

        @Override
        public void post(Runnable runnable) {
            mTasks.add(runnable);
        }

        public void runUntilEmpty() {
            while (!mTasks.isEmpty()) {
                Runnable runnable = mTasks.pop();
                runnable.run();
            }
        }
    }
}
