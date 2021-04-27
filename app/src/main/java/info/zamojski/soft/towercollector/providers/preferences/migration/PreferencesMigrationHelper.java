/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers.preferences.migration;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class PreferencesMigrationHelper {
    private final Context context;
    private final List<IUpgradeScript> upgradeScripts;

    public PreferencesMigrationHelper(Context context) {
        this.context = context;
        this.upgradeScripts = new ArrayList<>();
    }

    public void upgrade(int from, int to) {
        registerScripts(from);
        for (IUpgradeScript script : upgradeScripts) {
            try {
                Timber.i("upgrade(): Executing upgrade script %s", script.getClass().getSimpleName());
                script.performUpgrade(context);
            } catch (RuntimeException ex) {
                Timber.e(ex, "upgrade(): Upgrade script %s failed", script.getClass().getSimpleName());
                throw ex;
            }
        }
    }

    private void registerScripts(int from) {
        // order is important
        if (from < 1) {
            this.upgradeScripts.add(new UpgradeScript1());
        }
    }
}
