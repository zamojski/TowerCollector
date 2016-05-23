/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;
import trikita.log.Log;

class UpgradeScript2 implements IUpgradeScript {

    private static final String TAG = UpgradeScript2.class.getSimpleName();

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        Log.d("performUpgrade(): Upgrading db to version 2");
        // remove unused table
        database.execSQL("DROP INDEX IF EXISTS IX_operators_mcc_mnc");
        database.execSQL("DROP TABLE IF EXISTS operators");
        // backup old table
        database.execSQL("ALTER TABLE measurements RENAME TO measurements_backup");
        // drop index on old table
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_date_time");
        // create new tables and indexes
        database.execSQL("CREATE TABLE cells (row_id INTEGER PRIMARY KEY NOT NULL, mcc INTEGER NOT NULL, mnc INTEGER NOT NULL, lac INTEGER NOT NULL, cid INTEGER NOT NULL, UNIQUE (cid, lac, mnc, mcc) ON CONFLICT IGNORE)");
        database.execSQL("CREATE TABLE measurements (row_id INTEGER PRIMARY KEY NOT NULL, cell_id INTEGER NOT NULL, asu INTEGER NOT NULL, latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, gps_accuracy FLOAT NOT NULL, date_time INTEGER NOT NULL, FOREIGN KEY(cell_id) REFERENCES cells(row_id))");
        database.execSQL("CREATE INDEX 'IX_measurements_date_time' on measurements (date_time DESC)");
        database.execSQL("CREATE INDEX 'IX_measurements_cell_id' on measurements (cell_id ASC)");
        // migrate data
        database.execSQL("CREATE TRIGGER 'migrate' BEFORE DELETE ON measurements_backup BEGIN INSERT INTO cells (mcc, mnc, lac, cid) VALUES (old.mcc, old.mnc, old.lac, old.cell_id); INSERT INTO measurements (cell_id, asu, latitude, longitude, gps_accuracy, date_time) VALUES ((SELECT row_id FROM cells WHERE cid=old.cell_id AND lac=old.lac AND mnc=old.mnc AND mcc=old.mcc), old.asu, old.latitude, old.longitude, old.gps_accuracy, old.date_time); END");
        database.execSQL("DELETE FROM measurements_backup");
        // delete backup
        database.execSQL("DROP TABLE measurements_backup");
    }

}
