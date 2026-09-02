/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;

class UpgradeScript19 implements IUpgradeScript {

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        // re-apply one more time to fix issue on devices which run buggy UpgradeScript18 (now fixed there as well)
        database.execSQL("DROP VIEW not_uploaded_measurements");
        database.execSQL("CREATE VIEW not_uploaded_measurements AS SELECT * FROM measurements WHERE uploaded_to_ocid_at IS NULL AND uploaded_to_mls_at IS NULL AND uploaded_to_t0st_at IS NULL");
    }
}
