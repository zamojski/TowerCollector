/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.tasks;

import android.content.Context;
import android.widget.Toast;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.providers.preferences.PreferencesProvider;
import info.zamojski.soft.towercollector.providers.preferences.migration.PreferencesMigrationHelper;
import timber.log.Timber;

public class PreferencesUpgradeTask {

    private final Context context;
    private final int oldPreferencesVersion;
    private final int newPreferencesVersion;

    public PreferencesUpgradeTask(Context context, int oldPreferencesVersion) {
        this.context = context;
        this.oldPreferencesVersion = oldPreferencesVersion;
        this.newPreferencesVersion = PreferencesProvider.PREFERENCES_VERSION;
    }

    public void upgrade() {
        Timber.d("upgrade(): Loading data and running migration if necessary");
        try {
            Timber.i("upgrade(): Upgrading preferences from version %s to %s", oldPreferencesVersion, newPreferencesVersion);
            PreferencesMigrationHelper migrationHelper = new PreferencesMigrationHelper(context);
            migrationHelper.upgrade(oldPreferencesVersion, newPreferencesVersion);
            MyApplication.getPreferencesProvider().setPreferencesVersion(newPreferencesVersion);
        } catch (RuntimeException ex) {
            Timber.e(ex, "upgrade(): Preferences migration from version %s crashed", oldPreferencesVersion);
            Toast.makeText(context, "Preferences upgrade failed. Please clear app data or reinstall.", Toast.LENGTH_LONG).show();
            MyApplication.handleSilentException(ex);
            throw ex;
        }
    }
}
