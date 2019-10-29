/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;

import timber.log.Timber;

class UpgradeScript15 implements IUpgradeScript {

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        Timber.d("performUpgrade(): Upgrading db to version 15");
        // remove corrupted data - may result in incorrect statistics being presented (again, in case it failed somehow after upgrade)
        database.execSQL("DELETE FROM measurements WHERE row_id NOT IN (SELECT measurement_id FROM cell_signals)");
        // change value from int max to long max (it should never happen anyway)
        database.execSQL("UPDATE cells SET cid = " + Long.MAX_VALUE + " WHERE cid = " + Integer.MAX_VALUE);
    }
}
