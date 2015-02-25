package org.msf.records.filter.db.patient;

import org.joda.time.LocalDate;
import org.msf.records.App;
import org.msf.records.R;
import org.msf.records.data.app.AppPatient;
import org.msf.records.filter.db.SimpleSelectionFilter;
import org.msf.records.sync.providers.Contracts;

/**
 * Returns only patients below a specified age in years, i.e.
 * whose birth dates were later than the specified number of years ago.
 */
final class AgeFilter extends SimpleSelectionFilter<AppPatient> {
    private static final int OLDEST_CHILD_AGE = 13;

    private final int mYears;

    public AgeFilter(int years) {
        mYears = years;
    }

    @Override
    public String getSelectionString() {
        return Contracts.Patients.BIRTHDATE + " > ?";
    }

    @Override
    public String[] getSelectionArgs(CharSequence constraint) {
        LocalDate earliestBirthdate = LocalDate.now().minusYears(mYears);
        return new String[] { earliestBirthdate.toString() };
    }

    @Override
    public String getDescription() {
        if (mYears < OLDEST_CHILD_AGE) {
            return App.getInstance().getString(R.string.age_filter_description_child, mYears);
        } else {
            return App.getInstance().getString(R.string.age_filter_description_adult, mYears);
        }
    }
}