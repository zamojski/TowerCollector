/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

class UpgradeScript3 implements IUpgradeScript {

    private static final String TAG = UpgradeScript3.class.getSimpleName();

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        Log.d(TAG, "performUpgrade(): Upgrading db to version 3");
        // backup old table
        database.execSQL("ALTER TABLE measurements RENAME TO measurements_backup");
        // drop index on old table
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_date_time");
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_cell_id");
        // create new table and index
        database.execSQL("CREATE TABLE measurements (row_id INTEGER PRIMARY KEY NOT NULL, cell_id INTEGER NOT NULL, asu INTEGER NOT NULL, lat DOUBLE NOT NULL, lon DOUBLE NOT NULL, accuracy FLOAT NOT NULL, speed FLOAT NOT NULL, bearing FLOAT NOT NULL, altitude DOUBLE NOT NULL, measured_at INTEGER NOT NULL, FOREIGN KEY(cell_id) REFERENCES cells(row_id))");
        database.execSQL("CREATE INDEX 'IX_measurements_measured_at' on measurements (measured_at DESC)");
        database.execSQL("CREATE INDEX 'IX_measurements_cell_id' on measurements (cell_id ASC)");
        // migrate data
        database.execSQL("CREATE TRIGGER 'migrate' BEFORE DELETE ON measurements_backup BEGIN INSERT INTO measurements (cell_id, asu, lat, lon, accuracy, speed, bearing, altitude, measured_at) VALUES (old.cell_id, old.asu, old.latitude, old.longitude, old.gps_accuracy, 0, 0, 0, old.date_time); END");
        database.execSQL("DELETE FROM measurements_backup");
        // delete backup
        database.execSQL("DROP TABLE measurements_backup");
    }

}
