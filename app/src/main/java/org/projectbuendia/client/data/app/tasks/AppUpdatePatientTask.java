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

package org.projectbuendia.client.data.app.tasks;

import android.content.ContentResolver;
import android.os.AsyncTask;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;

import org.projectbuendia.client.data.app.AppPatient;
import org.projectbuendia.client.data.app.AppPatientDelta;
import org.projectbuendia.client.data.app.converters.AppTypeConverters;
import org.projectbuendia.client.events.CrudEventBus;
import org.projectbuendia.client.events.data.PatientUpdateFailedEvent;
import org.projectbuendia.client.events.data.ItemFetchFailedEvent;
import org.projectbuendia.client.events.data.ItemFetchedEvent;
import org.projectbuendia.client.events.data.ItemUpdatedEvent;
import org.projectbuendia.client.filter.db.SimpleSelectionFilter;
import org.projectbuendia.client.filter.db.patient.UuidFilter;
import org.projectbuendia.client.net.Server;
import org.projectbuendia.client.net.model.Patient;
import org.projectbuendia.client.sync.providers.Contracts;

import java.util.concurrent.ExecutionException;

/**
 * An {@link AsyncTask} that updates a patient on a server.
 *
 * <p>If the operation succeeds, a {@link ItemUpdatedEvent} is posted on the given
 * {@link CrudEventBus} with both the old and updated patient data. If the operation fails, a
 * {@link PatientUpdateFailedEvent} is posted instead.
 */
public class AppUpdatePatientTask extends AsyncTask<Void, Void, PatientUpdateFailedEvent> {
    private static final SimpleSelectionFilter FILTER = new UuidFilter();

    private final TaskFactory mTaskFactory;
    private final AppTypeConverters mConverters;
    private final Server mServer;
    private final ContentResolver mContentResolver;
    private final String mUuid;
    private final AppPatient mOriginalPatient;
    private final AppPatientDelta mPatientDelta;
    private final CrudEventBus mBus;

    AppUpdatePatientTask(
            TaskFactory taskFactory,
            AppTypeConverters converters,
            Server server,
            ContentResolver contentResolver,
            AppPatient originalPatient,
            AppPatientDelta patientDelta,
            CrudEventBus bus) {
        mTaskFactory = taskFactory;
        mConverters = converters;
        mServer = server;
        mContentResolver = contentResolver;
        mUuid = (originalPatient == null) ? null : originalPatient.uuid;
        mOriginalPatient = originalPatient;
        mPatientDelta = patientDelta;
        mBus = bus;
    }

    @Override
    protected PatientUpdateFailedEvent doInBackground(Void... params) {
        RequestFuture<Patient> patientFuture = RequestFuture.newFuture();

        mServer.updatePatient(mUuid, mPatientDelta, patientFuture, patientFuture);
        try {
            patientFuture.get();
        } catch (InterruptedException e) {
            return new PatientUpdateFailedEvent(PatientUpdateFailedEvent.REASON_INTERRUPTED, e);
        } catch (ExecutionException e) {
            // TODO: Parse the VolleyError to see exactly what kind of error was raised.
            return new PatientUpdateFailedEvent(
                    PatientUpdateFailedEvent.REASON_NETWORK, (VolleyError) e.getCause());
        }

        int count = mContentResolver.update(
                Contracts.Patients.CONTENT_URI,
                mPatientDelta.toContentValues(),
                FILTER.getSelectionString(),
                FILTER.getSelectionArgs(mUuid));

        switch (count) {
            case 0:
                return new PatientUpdateFailedEvent(
                        PatientUpdateFailedEvent.REASON_NO_SUCH_PATIENT, null /*exception*/);
            case 1:
                return null;
            default:
                return new PatientUpdateFailedEvent(
                        PatientUpdateFailedEvent.REASON_SERVER, null /*exception*/);
        }
    }

    @Override
    protected void onPostExecute(PatientUpdateFailedEvent event) {
        // If an error occurred, post the error event.
        if (event != null) {
            mBus.post(event);
            return;
        }

        // Otherwise, start a fetch task to fetch the patient from the database.
        mBus.register(new UpdateEventSubscriber());
        FetchItemTask<AppPatient> task = mTaskFactory.newFetchSingleAsyncTask(
                Contracts.Patients.CONTENT_URI,
                null,
                new UuidFilter(),
                mUuid,
                mConverters.patient,
                mBus);
        task.execute();
    }

    // After updating a patient, we fetch the patient from the database. The result of the fetch
    // determines if updating a patient was truly successful and propagates a new event to report
    // success/failure.
    @SuppressWarnings("unused") // Called by reflection from EventBus.
    private final class UpdateEventSubscriber {
        public void onEventMainThread(ItemFetchedEvent<AppPatient> event) {
            mBus.post(new ItemUpdatedEvent<>(mOriginalPatient, event.item));
            mBus.unregister(this);
        }

        public void onEventMainThread(ItemFetchFailedEvent event) {
            mBus.post(new PatientUpdateFailedEvent(
                    PatientUpdateFailedEvent.REASON_CLIENT, new Exception(event.error)));
            mBus.unregister(this);
        }
    }
}
