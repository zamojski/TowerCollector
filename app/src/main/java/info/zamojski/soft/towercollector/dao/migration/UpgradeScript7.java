/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;

class UpgradeScript7 implements IUpgradeScript {

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        // migrate data
        database.execSQL("UPDATE measurements SET ta = 2147483647 WHERE ta = -1000");
        database.execSQL("UPDATE measurements SET asu = 2147483647 WHERE asu = -1000");
        database.execSQL("UPDATE measurements SET dbm = 2147483647 WHERE dbm = -1000");
        database.execSQL("UPDATE cells_archive SET mcc = 2147483647 WHERE mcc = -1");
        database.execSQL("UPDATE cells_archive SET mnc = 2147483647 WHERE mnc = -1");
        database.execSQL("UPDATE cells_archive SET lac = 2147483647 WHERE lac = -1");
        database.execSQL("UPDATE cells_archive SET cid = 2147483647 WHERE cid = -1");
        database.execSQL("UPDATE cells_archive SET psc = 2147483647 WHERE psc = -1");
        database.execSQL("UPDATE cells SET mcc = 2147483647 WHERE mcc = -1");
        database.execSQL("UPDATE cells SET mnc = 2147483647 WHERE mnc = -1");
        database.execSQL("UPDATE cells SET lac = 2147483647 WHERE lac = -1");
        database.execSQL("UPDATE cells SET cid = 2147483647 WHERE cid = -1");
        database.execSQL("UPDATE cells SET psc = 2147483647 WHERE psc = -1");
    }
}
