/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;
import trikita.log.Log;

class UpgradeScript8 implements IUpgradeScript {

    private static final String TAG = UpgradeScript8.class.getSimpleName();

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        Log.d("performUpgrade(): Upgrading db to version 8");
        // cleanup
        database.execSQL("DELETE FROM measurements WHERE lat < -90 OR lat > 90 OR lat = 0 OR lon < -180 OR lon > 180 OR lon = 0");
        String cellFilterGsm = "(cid >= 1 AND cid <= 268435455 AND lac >= 1 AND lac <= 65535 AND mnc >= 0 AND mnc <= 999 AND mcc >= 100 AND mcc <= 999)";
        String cellFilterCdma = "(cid >= 1 AND cid <= 65535 AND lac >= 1 AND lac <= 65535 AND mnc >= 0 AND mnc <= 32767 AND mcc = 2147483647)";
        String cellFilter = "NOT (" + cellFilterGsm + " OR " + cellFilterCdma + ")";
        database.execSQL("DELETE FROM measurements WHERE cell_id IN (SELECT row_id FROM cells WHERE " + cellFilter + ")");
        database.execSQL("DELETE FROM cells WHERE " + cellFilter);
        database.execSQL("DELETE FROM cells_archive WHERE " + cellFilter);
    }
}
