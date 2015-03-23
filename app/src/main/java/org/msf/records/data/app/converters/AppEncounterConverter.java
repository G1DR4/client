/*
 * Copyright 2015 The Project Buendia Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.msf.records.data.app.converters;

import android.database.Cursor;

import org.joda.time.DateTime;
import org.msf.records.data.app.AppEncounter;
import org.msf.records.sync.providers.Contracts;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AppTypeConverter} that converts {@link AppEncounter}s. Expects the {@link Cursor} to
 * contain only a single encounter, represented by multiple observations, with one observation per
 * row.
 *
 * <p>Unlike other {@link AppTypeConverter}s, {@link AppEncounterConverter} must be instantiated
 * once per patient, since {@link AppEncounter} contains the patient's UUID as one of its fields,
 * which is not present in the database representation of an encounter.
 */
public class AppEncounterConverter implements AppTypeConverter<AppEncounter> {
    private String mPatientUuid;

    public AppEncounterConverter(String patientUuid) {
        mPatientUuid = patientUuid;
    }

    @Override
    public AppEncounter fromCursor(Cursor cursor) {
        final String encounterUuid = cursor.getString(
                cursor.getColumnIndex(Contracts.ObservationColumns.ENCOUNTER_UUID));
        final long timestamp = cursor.getLong(
                cursor.getColumnIndex(Contracts.ObservationColumns.ENCOUNTER_TIME));
        final DateTime dateTime = new DateTime(timestamp);
        List<AppEncounter.AppObservation> observationList = new ArrayList<>();
        cursor.move(-1);
        while (cursor.moveToNext()) {
            String value =
                    cursor.getString((cursor.getColumnIndex(Contracts.ObservationColumns.VALUE)));
            AppEncounter.AppObservation.Type type =
                    AppEncounter.AppObservation.estimatedTypeFor(value);
            observationList.add(new AppEncounter.AppObservation(
                    cursor.getString(
                            (cursor.getColumnIndex(Contracts.ObservationColumns.CONCEPT_UUID))),
                    value,
                    type
            ));
        }
        AppEncounter.AppObservation[] observations =
                new AppEncounter.AppObservation[observationList.size()];
        observationList.toArray(observations);

        return new AppEncounter(mPatientUuid, encounterUuid, dateTime, observations);
    }
}
