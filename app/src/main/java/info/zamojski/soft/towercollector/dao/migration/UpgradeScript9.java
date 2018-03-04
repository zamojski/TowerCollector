/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;
import trikita.log.Log;

class UpgradeScript9 implements IUpgradeScript {

    private static final String TAG = UpgradeScript9.class.getSimpleName();

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        Log.d("performUpgrade(): Upgrading db to version 9");
        // backup old table
        database.execSQL("ALTER TABLE measurements RENAME TO measurements_backup");
        // drop indexes and trigger on old table
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_measured_at");
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_cell_id");
        database.execSQL("DROP TRIGGER IF EXISTS update_measurements_stats");
        // add new column with default null value
        database.execSQL("ALTER TABLE measurements_backup ADD COLUMN neighboring INTEGER DEFAULT NULL");
        // update value based on existing data
        database.execSQL("UPDATE measurements_backup SET neighboring = 0");
        // create new table
        database.execSQL("CREATE TABLE measurements (row_id INTEGER PRIMARY KEY NOT NULL, cell_id INTEGER NOT NULL, neighboring INTEGER NOT NULL, ta INTEGER NOT NULL, asu INTEGER NOT NULL, dbm INTEGER NOT NULL, lat REAL NOT NULL, lon REAL NOT NULL, accuracy REAL NOT NULL, speed REAL NOT NULL, bearing REAL NOT NULL, altitude REAL NOT NULL, measured_at INTEGER NOT NULL, operator_id INTEGER DEFAULT NULL, FOREIGN KEY(cell_id) REFERENCES cells(row_id), FOREIGN KEY(operator_id) REFERENCES operators(row_id))");
        database.execSQL("CREATE INDEX 'IX_measurements_measured_at' on measurements (measured_at DESC)");
        database.execSQL("CREATE INDEX 'IX_measurements_cell_id' on measurements (cell_id ASC)");
        // create on delete trigger
        database.execSQL("CREATE TRIGGER 'migrate_measurements' BEFORE DELETE ON measurements_backup BEGIN "
                + "INSERT INTO measurements (row_id, cell_id, neighboring, ta, asu, dbm, lat, lon, accuracy, speed, bearing, altitude, measured_at, operator_id) VALUES (old.row_id, old.cell_id, 0, old.ta, old.asu, old.dbm, old.lat, old.lon, old.accuracy, old.speed, old.bearing, old.altitude, old.measured_at, old.operator_id); "
                + "END");
        // migrate data
        database.execSQL("DELETE FROM measurements_backup");
        // delete backup
        database.execSQL("DROP TABLE measurements_backup");
        // finish table (to prevent unwanted stats update during migration)
        database.execSQL("CREATE TRIGGER 'update_measurements_stats' AFTER INSERT ON measurements BEGIN UPDATE stats SET total_locations = total_locations + 1; END");
    }
}
