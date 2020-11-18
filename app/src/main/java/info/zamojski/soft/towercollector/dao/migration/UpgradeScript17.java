/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;

class UpgradeScript17 implements IUpgradeScript {

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        // add new columns
        database.execSQL("ALTER TABLE cell_signals ADD COLUMN ec_no INTEGER NOT NULL DEFAULT " + Integer.MAX_VALUE);
        database.execSQL("ALTER TABLE cell_signals ADD COLUMN arfcn INTEGER NOT NULL DEFAULT " + Integer.MAX_VALUE);
    }
}
