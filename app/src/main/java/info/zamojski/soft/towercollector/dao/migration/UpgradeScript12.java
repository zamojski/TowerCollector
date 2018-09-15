/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;

import timber.log.Timber;

class UpgradeScript12 implements IUpgradeScript {

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        Timber.d("performUpgrade(): Upgrading db to version 12");
        // create new tables
        database.execSQL("CREATE TABLE temporary_locations ("
                + "row_id INTEGER PRIMARY KEY NOT NULL, "
                + "hashcode CHARACTER(40) NOT NULL, "
                + "lat REAL NOT NULL, "
                + "lon REAL NOT NULL, "
                + "accuracy REAL NOT NULL, "
                + "speed REAL NOT NULL, "
                + "bearing REAL NOT NULL, "
                + "altitude REAL NOT NULL, "
                + "UNIQUE (hashcode) ON CONFLICT IGNORE)");
        database.execSQL("CREATE TABLE temporary_cells ("
                + "row_id  INTEGER PRIMARY KEY NOT NULL, "
                + "mcc INTEGER NOT NULL, "
                + "mnc INTEGER NOT NULL, "
                + "lac INTEGER NOT NULL, "
                + "cid INTEGER NOT NULL, "
                + "net_type INTEGER NOT NULL, "
                + "discovered_at INTEGER NOT NULL, "
                + "UNIQUE (cid, lac, mnc, mcc, net_type) ON CONFLICT IGNORE)");
        database.execSQL("CREATE TABLE temporary_measurements ("
                + "row_id INTEGER PRIMARY KEY NOT NULL, "
                + "location_id INTEGER NOT NULL, "
                + "cell_id INTEGER NOT NULL, "
                + "psc INTEGER NOT NULL, "
                + "neighboring INTEGER NOT NULL, "
                + "ta INTEGER NOT NULL, "
                + "asu INTEGER NOT NULL, "
                + "dbm INTEGER NOT NULL, "
                + "measured_at INTEGER NOT NULL, "
                + "uploaded_to_ocid_at INTEGER DEFAULT NULL, "
                + "uploaded_to_mls_at INTEGER DEFAULT NULL, "
                + "FOREIGN KEY(location_id) REFERENCES temporary_locations(row_id),"
                + "FOREIGN KEY(cell_id) REFERENCES temporary_cells(row_id))");
        database.execSQL("CREATE INDEX 'IX_temporary_measurements_measured_at' ON temporary_measurements (measured_at DESC)");
        database.execSQL("CREATE INDEX 'IX_temporary_measurements_location_id' ON temporary_measurements (location_id ASC)");
        database.execSQL("CREATE INDEX 'IX_temporary_measurements_cell_id' ON temporary_measurements (cell_id DESC)");
        database.execSQL("CREATE INDEX 'IX_temporary_measurements_uploaded_to_ocid_at' ON temporary_measurements (uploaded_to_ocid_at ASC)");
        database.execSQL("CREATE INDEX 'IX_temporary_measurements_uploaded_to_mls_at' ON temporary_measurements (uploaded_to_mls_at ASC)");
    }
}
