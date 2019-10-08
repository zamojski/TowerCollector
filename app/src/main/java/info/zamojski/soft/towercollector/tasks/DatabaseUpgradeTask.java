/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.tasks;

import org.acra.ACRA;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;
import timber.log.Timber;

public class DatabaseUpgradeTask {

    private int oldDbVersion;

    public DatabaseUpgradeTask(int oldDbVersion) {
        this.oldDbVersion = oldDbVersion;
    }

    public void upgrade() {
        Timber.d("doInBackground(): Loading data and running migration if necessary");
        try {
            // invalidate database (protects against crash when database swapped while application paused - generally for testing)
            MeasurementsDatabase.invalidateInstance(MyApplication.getApplication());
            long startTime = System.currentTimeMillis();
            // one of below will trigger data migration if necessary (long operation)
            MeasurementsDatabase.getInstance(MyApplication.getApplication()).forceDatabaseUpgrade();
            AnalyticsStatistics stats = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getAnalyticsStatistics();
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime);
            MyApplication.getAnalytics().sendMigrationFinished(duration, oldDbVersion, stats);
        } catch (RuntimeException ex) {
            Timber.e(ex, "doInBackground(): Database migration crashed");
            MyApplication.handleSilentException(ex);
            throw ex;
        }
    }
}
