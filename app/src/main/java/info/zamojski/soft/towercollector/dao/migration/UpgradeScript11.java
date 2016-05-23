/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;
import trikita.log.Log;

class UpgradeScript11 implements IUpgradeScript {

    private static final String TAG = UpgradeScript11.class.getSimpleName();

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        Log.d("performUpgrade(): Upgrading db to version 11");
        // backup old tables
        database.execSQL("ALTER TABLE locations RENAME TO locations_backup");
        database.execSQL("ALTER TABLE measurements RENAME TO measurements_backup");
        // drop trigger on old table
        database.execSQL("DROP TRIGGER IF EXISTS update_measurements_stats");
        // create new tables
        database.execSQL("CREATE TABLE locations ("
            + "row_id INTEGER PRIMARY KEY NOT NULL, "
            + "hashcode CHARACTER(40) NOT NULL, "
            + "lat REAL NOT NULL, "
            + "lon REAL NOT NULL, "
            + "accuracy REAL NOT NULL, "
            + "speed REAL NOT NULL, "
            + "bearing REAL NOT NULL, "
            + "altitude REAL NOT NULL, "
            + "UNIQUE (hashcode) ON CONFLICT IGNORE)");
        database.execSQL("CREATE TABLE measurements ("
            + "row_id INTEGER PRIMARY KEY NOT NULL, "
            + "location_id INTEGER NOT NULL, "
            + "cell_id INTEGER NOT NULL, "
            + "psc INTEGER NOT NULL, "
            + "neighboring INTEGER NOT NULL, "
            + "ta INTEGER NOT NULL, "
            + "asu INTEGER NOT NULL, "
            + "dbm INTEGER NOT NULL, "
            + "measured_at INTEGER NOT NULL, "
            + "FOREIGN KEY(location_id) REFERENCES locations(row_id),"
            + "FOREIGN KEY(cell_id) REFERENCES cells(row_id))");
        // migrate data
        database.execSQL("INSERT INTO locations (hashcode, lat, lon, accuracy, speed, bearing, altitude) SELECT row_id, lat, lon, accuracy, speed, bearing, altitude FROM locations_backup");
        database.execSQL("INSERT INTO measurements (location_id, cell_id, psc, neighboring, ta, asu, dbm, measured_at) SELECT l.row_id, cell_id, psc, neighboring, ta, asu, dbm, measured_at FROM measurements_backup INNER JOIN locations l ON l.hashcode = location_id");
        // drop indexes on old table
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_measured_at");
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_location_id");
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_cell_id");
        // delete backup
        database.execSQL("DROP TABLE locations_backup");
        database.execSQL("DROP TABLE measurements_backup");
        // create new indexes on measurements
        database.execSQL("CREATE INDEX 'IX_measurements_measured_at' ON measurements (measured_at DESC);");
        database.execSQL("CREATE INDEX 'IX_measurements_location_id' ON measurements (location_id ASC);");
        database.execSQL("CREATE INDEX 'IX_measurements_cell_id' ON measurements (cell_id DESC);");
        // create new trigger on measurements
        database.execSQL("CREATE TRIGGER 'update_measurements_stats' AFTER INSERT ON measurements "
                + "BEGIN "
                + "UPDATE stats SET total_locations = total_locations + 1; "
                + "END;");
    }
}
