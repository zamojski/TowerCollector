/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;

import timber.log.Timber;

class UpgradeScript14 implements IUpgradeScript {

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        Timber.d("performUpgrade(): Upgrading db to version 14");
        // remove corrupted data - may result in incorrect statistics being presented
        database.execSQL("DELETE FROM measurements WHERE row_id NOT IN (SELECT measurement_id FROM cell_signals)");
    }
}
