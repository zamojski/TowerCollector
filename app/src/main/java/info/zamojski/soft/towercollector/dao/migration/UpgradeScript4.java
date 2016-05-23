/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;
import trikita.log.Log;

class UpgradeScript4 implements IUpgradeScript {

    private static final String TAG = UpgradeScript4.class.getSimpleName();

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        Log.d("performUpgrade(): Upgrading db to version 4");
        // backup old tables
        database.execSQL("ALTER TABLE measurements RENAME TO measurements_backup");
        database.execSQL("ALTER TABLE cells RENAME TO cells_backup");
        // drop index on old table
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_measured_at");
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_cell_id");
        // create new table and index
        database.execSQL("CREATE TABLE cells (row_id INTEGER PRIMARY KEY NOT NULL, mcc INTEGER NOT NULL, mnc INTEGER NOT NULL, lac INTEGER NOT NULL, cid INTEGER NOT NULL, psc INTEGER NOT NULL, net_type INTEGER NOT NULL, UNIQUE (cid, lac, mnc, mcc, psc) ON CONFLICT IGNORE)");
        database.execSQL("CREATE TABLE measurements (row_id INTEGER PRIMARY KEY NOT NULL, cell_id INTEGER NOT NULL, ta INTEGER NOT NULL, asu INTEGER NOT NULL, dbm INTEGER NOT NULL, lat DOUBLE NOT NULL, lon DOUBLE NOT NULL, accuracy FLOAT NOT NULL, speed FLOAT NOT NULL, bearing FLOAT NOT NULL, altitude DOUBLE NOT NULL, measured_at INTEGER NOT NULL, FOREIGN KEY(cell_id) REFERENCES cells(row_id))");
        database.execSQL("CREATE INDEX 'IX_measurements_measured_at' on measurements (measured_at DESC)");
        database.execSQL("CREATE INDEX 'IX_measurements_cell_id' on measurements (cell_id ASC)");
        // migrate data
        database.execSQL("CREATE TRIGGER 'migrate_cells' BEFORE DELETE ON cells_backup BEGIN INSERT INTO cells (row_id, mcc, mnc, lac, cid, psc, net_type) VALUES (old.row_id, old.mcc, old.mnc, old.lac, old.cid, -1, 0); END");
        database.execSQL("DELETE FROM cells_backup");
        database.execSQL("CREATE TRIGGER 'migrate_measurements' BEFORE DELETE ON measurements_backup BEGIN INSERT INTO measurements (cell_id, ta, asu, dbm, lat, lon, accuracy, speed, bearing, altitude, measured_at) VALUES (old.cell_id, -1000, old.asu, CASE old.asu WHEN 99 THEN -1000 ELSE 2 * (old.asu) - 113 END, old.lat, old.lon, old.accuracy, old.speed, old.bearing, old.altitude, old.measured_at); END");
        database.execSQL("DELETE FROM measurements_backup");
        // delete backup
        database.execSQL("DROP TABLE measurements_backup");
        database.execSQL("DROP TABLE cells_backup");
    }

}
