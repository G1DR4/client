package org.msf.records.ui.patientlist;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.msf.records.R;
import org.msf.records.events.location.LocationsLoadedEvent;
import org.msf.records.filter.PatientFilters;
import org.msf.records.filter.SimpleSelectionFilter;
import org.msf.records.ui.OdkActivityLauncher;
import org.msf.records.ui.SectionedSpinnerAdapter;
import org.msf.records.ui.patientcreation.PatientCreationActivity;
import org.odk.collect.android.tasks.DiskSyncTask;

/**
 * An activity representing a list of Patients.
 */
public class PatientListActivity extends PatientSearchActivity {

    private static final int ODK_ACTIVITY_REQUEST = 1;
    private static final String SELECTED_FILTER_KEY = "selected_filter";

    private PatientListFragment mFragment;

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {
        super.onCreateImpl(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        int selectedFilter = 0;  // Default filter == all patients
        if (savedInstanceState != null) {
            selectedFilter = savedInstanceState.getInt(SELECTED_FILTER_KEY, 0);
        }
        setupCustomActionBar(selectedFilter);

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_FILTER_KEY, getActionBar().getSelectedNavigationIndex());
    }

    public void onEventMainThread(LocationsLoadedEvent event) {
        // Update filters when locations update, as zones may have changed.
        setupCustomActionBar(getActionBar().getSelectedNavigationIndex());
    }

    private void setupCustomActionBar(int selectedFilter) {
        final SimpleSelectionFilter[] filters = PatientFilters.getFiltersForDisplay();
        SectionedSpinnerAdapter<SimpleSelectionFilter> adapter = new SectionedSpinnerAdapter<>(
                this,
                R.layout.patient_list_spinner_dropdown_item,
                R.layout.patient_list_spinner_expanded_dropdown_item,
                R.layout.patient_list_spinner_expanded_section_divider,
                filters);

        ActionBar.OnNavigationListener callback = new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long id) {
                mFragment.filterBy(filters[position]);
                return true;
            }
        };

        final ActionBar actionBar = getActionBar();
        actionBar.setLogo(R.drawable.ic_launcher);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(adapter, callback);
        actionBar.setSelectedNavigationItem(selectedFilter);

        mFragment = (PatientListFragment)getSupportFragmentManager()
                .findFragmentById(R.id.patient_list);
        mFragment.filterBy(filters[selectedFilter]);
    }

    @Override
    public void onExtendOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_add).setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        startActivity(PatientCreationActivity.class);

                        return true;
                    }
                });
        super.onExtendOptionsMenu(menu);
    }

    private enum ScanAction {
        PLAY_WITH_ODK,
        FETCH_XFORMS,
        FAKE_SCAN,
    }

    private void startScanBracelet() {
        ScanAction scanAction = ScanAction.PLAY_WITH_ODK;
        switch (scanAction) {
            case PLAY_WITH_ODK:
                showFirstFormFromSdcard();
                break;
            case FAKE_SCAN:
                showFakeScanProgress();
                break;
        }
    }

    private void showFirstFormFromSdcard() {
        // Sync the local sdcard forms into the database
        new DiskSyncTask().execute((Void[]) null);
        OdkActivityLauncher.showOdkCollect(this, ODK_ACTIVITY_REQUEST, 1L);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != ODK_ACTIVITY_REQUEST) {
            return;
        }
        OdkActivityLauncher.sendOdkResultToServer(this, null /* create a new patient */, false,
                resultCode, data);
    }

    private void showFakeScanProgress() {
        final ProgressDialog progressDialog = ProgressDialog
                .show(PatientListActivity.this, null, "Scanning for near by bracelets ...", true);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    private void startActivity(Class<?> activityClass) {
      Intent intent = new Intent(PatientListActivity.this, activityClass);
      startActivity(intent);
    }
}
