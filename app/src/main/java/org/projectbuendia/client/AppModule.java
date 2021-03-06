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

package org.projectbuendia.client;

import android.app.Application;
import android.content.ContentResolver;
import android.preference.PreferenceManager;

import org.projectbuendia.client.data.app.AppModelModule;
import org.projectbuendia.client.diagnostics.DiagnosticsModule;
import org.projectbuendia.client.events.EventsModule;
import org.projectbuendia.client.net.NetModule;
import org.projectbuendia.client.sync.SyncAccountService;
import org.projectbuendia.client.sync.LocalizedChartHelper;
import org.projectbuendia.client.sync.SyncManager;
import org.projectbuendia.client.ui.BaseActivity;
import org.projectbuendia.client.ui.UpdateNotificationController;
import org.projectbuendia.client.ui.chart.PatientChartActivity;
import org.projectbuendia.client.ui.lists.LocationListActivity;
import org.projectbuendia.client.ui.newpatient.NewPatientActivity;
import org.projectbuendia.client.ui.lists.FilteredPatientListActivity;
import org.projectbuendia.client.ui.lists.PatientListController;
import org.projectbuendia.client.ui.lists.PatientListFragment;
import org.projectbuendia.client.ui.lists.BaseSearchablePatientListActivity;
import org.projectbuendia.client.ui.lists.SingleLocationActivity;
import org.projectbuendia.client.ui.lists.SingleLocationFragment;
import org.projectbuendia.client.ui.login.LoginActivity;
import org.projectbuendia.client.ui.login.LoginFragment;
import org.projectbuendia.client.updater.UpdateModule;
import org.projectbuendia.client.user.UserModule;
import org.projectbuendia.client.utils.UtilsModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/** A Dagger module that provides the top-level bindings for the app. */
@Module(
        includes = {
                AppModelModule.class,
                DiagnosticsModule.class,
                EventsModule.class,
                NetModule.class,
                UpdateModule.class,
                UserModule.class,
                UtilsModule.class
        },
        injects = {
                App.class,

                // TODO: Move these into activity-specific modules.
                // Activities
                NewPatientActivity.class,
                BaseActivity.class,
                PatientChartActivity.class,
                FilteredPatientListActivity.class,
                PatientListController.class,
                BaseSearchablePatientListActivity.class,
                SingleLocationActivity.class,
                LocationListActivity.class,
                PatientListFragment.class,
                SingleLocationFragment.class,
                UpdateNotificationController.class,
                LoginActivity.class,
                LoginFragment.class
        },
        staticInjections = {
                SyncAccountService.class
        })
public final class AppModule {

    private final App mApp;

    public AppModule(App app) {
        mApp = app;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return mApp;
    }

    @Provides
    @Singleton
    AppSettings provideAppSettings(Application app) {
        return new AppSettings(
                PreferenceManager.getDefaultSharedPreferences(app), app.getResources());
    }

    @Provides
    @Singleton
    ContentResolver provideContentResolver(Application app) {
        return app.getContentResolver();
    }

    @Provides
    @Singleton
    SyncManager provideSyncManager(AppSettings settings) {
        return new SyncManager(settings);
    }

    @Provides
    @Singleton
    LocalizedChartHelper provideLocalizedChartHelper(ContentResolver contentResolver) {
        return new LocalizedChartHelper(contentResolver);
    }
}
