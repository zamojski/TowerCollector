/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import timber.log.Timber;

public class DbMigrationHelper {

    private SQLiteDatabase database;
    private List<IUpgradeScript> upgradeScripts;

    public DbMigrationHelper(SQLiteDatabase database) {
        this.database = database;
        this.upgradeScripts = new ArrayList<>();
    }

    public void upgrade(int from, int to) {
        registerScripts(from);
        for (IUpgradeScript script : upgradeScripts) {
            try {
                Timber.i("upgrade(): Executing upgrade script %s", script.getClass().getSimpleName());
                script.performUpgrade(database);
            } catch (RuntimeException ex) {
                Timber.e(ex, "upgrade(): Upgrade script %s failed", script.getClass().getSimpleName());
                throw ex;
            }
        }
    }

    private void registerScripts(int from) {
        // order is important
        if (from < 2) {
            this.upgradeScripts.add(new UpgradeScript2());
        }
        if (from < 3) {
            this.upgradeScripts.add(new UpgradeScript3());
        }
        if (from < 4) {
            this.upgradeScripts.add(new UpgradeScript4());
        }
        if (from < 5) {
            this.upgradeScripts.add(new UpgradeScript5());
        }
        if (from < 6) {
            this.upgradeScripts.add(new UpgradeScript6());
        }
        if (from < 7) {
            this.upgradeScripts.add(new UpgradeScript7());
        }
        if (from < 8) {
            this.upgradeScripts.add(new UpgradeScript8());
        }
        if (from < 9) {
            this.upgradeScripts.add(new UpgradeScript9());
        }
        if (from < 10) {
            this.upgradeScripts.add(new UpgradeScript10());
        }
        if (from < 11) {
            this.upgradeScripts.add(new UpgradeScript11());
        }
        if (from < 12) {
            this.upgradeScripts.add(new UpgradeScript12());
        }
        if (from < 13) {
            this.upgradeScripts.add(new UpgradeScript13());
        }
        if (from < 14) {
            this.upgradeScripts.add(new UpgradeScript14());
        }
        if (from < 15) {
            this.upgradeScripts.add(new UpgradeScript15());
        }
        if (from < 16) {
            this.upgradeScripts.add(new UpgradeScript16());
        }
    }

}
