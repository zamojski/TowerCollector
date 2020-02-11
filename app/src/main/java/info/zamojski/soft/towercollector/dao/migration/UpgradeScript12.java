/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;

class UpgradeScript12 implements IUpgradeScript {

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        // add new columns with default values
        database.execSQL("ALTER TABLE measurements ADD COLUMN uploaded_to_ocid_at INTEGER DEFAULT NULL");
        database.execSQL("ALTER TABLE measurements ADD COLUMN uploaded_to_mls_at INTEGER DEFAULT NULL");
        // create new indexes
        database.execSQL("CREATE INDEX 'IX_measurements_uploaded_to_ocid_at' ON measurements (uploaded_to_ocid_at ASC)");
        database.execSQL("CREATE INDEX 'IX_measurements_uploaded_to_mls_at' ON measurements (uploaded_to_mls_at ASC)");
        // create new view
        database.execSQL("CREATE VIEW not_uploaded_measurements AS SELECT * FROM measurements WHERE uploaded_to_ocid_at IS NULL AND uploaded_to_mls_at IS NULL");
    }
}
