/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import info.zamojski.soft.towercollector.utils.HashUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

class UpgradeScript10 implements IUpgradeScript {

    private static final String TAG = UpgradeScript10.class.getSimpleName();

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        Log.d(TAG, "performUpgrade(): Upgrading db to version 10");
        // backup old tables
        database.execSQL("ALTER TABLE measurements RENAME TO measurements_backup;");
        database.execSQL("ALTER TABLE cells RENAME TO cells_backup;");
        database.execSQL("ALTER TABLE cells_archive RENAME TO cells_archive_backup;");
        // drop old triggers
        database.execSQL("DROP TRIGGER IF EXISTS archive_cell;");
        database.execSQL("DROP TRIGGER IF EXISTS update_measurements_stats;");
        // create cells_archive table
        database.execSQL("CREATE TABLE cells_archive ( "
                + "row_id INTEGER PRIMARY KEY NOT NULL, "
                + "mcc INTEGER NOT NULL, "
                + "mnc INTEGER NOT NULL, "
                + "lac INTEGER NOT NULL, "
                + "cid INTEGER NOT NULL, "
                + "net_type INTEGER NOT NULL, "
                + "discovered_at INTEGER NOT NULL, "
                + "UNIQUE (cid, lac, mnc, mcc, net_type) ON CONFLICT IGNORE);");
        // create new cells table
        database.execSQL("CREATE TABLE cells ( "
                + "row_id INTEGER PRIMARY KEY NOT NULL, "
                + "mcc INTEGER NOT NULL, "
                + "mnc INTEGER NOT NULL, "
                + "lac INTEGER NOT NULL, "
                + "cid INTEGER NOT NULL, "
                + "net_type INTEGER NOT NULL, "
                + "discovered_at INTEGER NOT NULL, "
                + "UNIQUE (cid, lac, mnc, mcc, net_type) ON CONFLICT IGNORE);");
        // create new locations table
        database.execSQL("CREATE TABLE locations ( "
                + "row_id CHARACTER(40) NOT NULL, "
                + "lat REAL NOT NULL, "
                + "lon REAL NOT NULL, "
                + "accuracy REAL NOT NULL, "
                + "speed REAL NOT NULL, "
                + "bearing REAL NOT NULL, "
                + "altitude REAL NOT NULL, "
                + "UNIQUE (row_id) ON CONFLICT IGNORE);");
        // create new measurements table
        database.execSQL("CREATE TABLE measurements ( "
                + "row_id INTEGER PRIMARY KEY NOT NULL, "
                + "location_id INTEGER NOT NULL, "
                + "cell_id INTEGER NOT NULL, "
                + "psc INTEGER NOT NULL, "
                + "neighboring INTEGER NOT NULL, "
                + "ta INTEGER NOT NULL, "
                + "asu INTEGER NOT NULL, "
                + "dbm INTEGER NOT NULL, "
                + "measured_at INTEGER NOT NULL, "
                + "FOREIGN KEY(location_id) REFERENCES locations(row_id), "
                + "FOREIGN KEY(cell_id) REFERENCES cells(row_id));");
        // migrate cells_archive
        database.execSQL("INSERT INTO cells_archive (mcc, mnc, lac, cid, net_type, discovered_at) "
                + "SELECT DISTINCT mcc, mnc, lac, cid, net_type, discovered_at FROM cells_archive_backup;");
        // migrate cells
        database.execSQL("INSERT INTO cells (mcc, mnc, lac, cid, net_type, discovered_at) "
                + "SELECT DISTINCT mcc, mnc, lac, cid, net_type, discovered_at FROM cells_backup;");
        // migrate locations and measurements
        database.beginTransaction();
        try {
            String migrationQuery = "SELECT "
                    + "mb.lat, "
                    + "mb.lon, "
                    + "mb.accuracy, "
                    + "mb.speed, "
                    + "mb.bearing, "
                    + "mb.altitude, "
                    + "(SELECT row_id FROM cells WHERE mcc = cb.mcc AND mnc = cb.mnc "
                    + "AND lac = cb.lac AND cid = cb.cid AND net_type = cb.net_type) AS cell_id, "
                    + "cb.psc, "
                    + "mb.neighboring, "
                    + "mb.ta, "
                    + "mb.asu, "
                    + "mb.dbm, "
                    + "mb.measured_at "
                    + "FROM measurements_backup mb "
                    + "INNER JOIN cells_backup cb ON mb.cell_id = cb.row_id;";
            Cursor mCursor = database.rawQuery(migrationQuery, null);

            while (mCursor.moveToNext()) {
                double latitude = mCursor.getDouble(0);
                double longitude = mCursor.getDouble(1);
                double accuracy = mCursor.getDouble(2);
                double speed = mCursor.getDouble(3);
                double bearing = mCursor.getDouble(4);
                double altitude = mCursor.getDouble(5);
                int cellId = mCursor.getInt(6);
                int psc = mCursor.getInt(7);
                int neighboring = mCursor.getInt(8);
                int ta = mCursor.getInt(9);
                int asu = mCursor.getInt(10);
                int dbm = mCursor.getInt(11);
                long measuredAt = mCursor.getLong(12);
                String locationHashCode = HashUtils.toSha1(latitude, longitude, accuracy, speed, bearing, altitude);

                ContentValues locationValues = new ContentValues();
                locationValues.put("row_id", locationHashCode);
                locationValues.put("lat", latitude);
                locationValues.put("lon", longitude);
                locationValues.put("accuracy", accuracy);
                locationValues.put("speed", speed);
                locationValues.put("bearing", bearing);
                locationValues.put("altitude", altitude);
                @SuppressWarnings("unused")
                long locationRowId = database.insert("locations", null, locationValues);

                ContentValues measurementValues = new ContentValues();
                measurementValues.put("location_id", locationHashCode);
                measurementValues.put("cell_id", cellId);
                measurementValues.put("psc", psc);
                measurementValues.put("neighboring", neighboring);
                measurementValues.put("ta", ta);
                measurementValues.put("asu", asu);
                measurementValues.put("dbm", dbm);
                measurementValues.put("measured_at", measuredAt);
                @SuppressWarnings("unused")
                long measurementRowId = database.insert("measurements", null, measurementValues);
            }

            mCursor.close();

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        // drop old indexes
        database.execSQL("DROP INDEX 'IX_measurements_cell_id';");
        database.execSQL("DROP INDEX 'IX_measurements_measured_at';");
        // drop old table backups
        database.execSQL("DROP TABLE measurements_backup;");
        database.execSQL("DROP TABLE cells_backup;");
        database.execSQL("DROP TABLE cells_archive_backup;");
        database.execSQL("DROP TABLE operators;");
        // create new trigger on cells
        database.execSQL("CREATE TRIGGER 'archive_cell' BEFORE DELETE ON cells "
                + "BEGIN "
                + "INSERT INTO cells_archive (mcc, mnc, lac, cid, net_type, discovered_at) "
                + "VALUES (old.mcc, old.mnc, old.lac, old.cid, old.net_type, old.discovered_at); "
                + "END;");
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
