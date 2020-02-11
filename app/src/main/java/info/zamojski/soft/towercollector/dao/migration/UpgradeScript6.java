/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;

class UpgradeScript6 implements IUpgradeScript {

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        // create cells archive table
        database.execSQL("CREATE TABLE cells_archive (row_id INTEGER PRIMARY KEY NOT NULL, mcc INTEGER NOT NULL, mnc INTEGER NOT NULL, lac INTEGER NOT NULL, cid INTEGER NOT NULL, psc INTEGER NOT NULL, net_type INTEGER NOT NULL, discovered_at INTEGER NOT NULL, UNIQUE (cid, lac, mnc, mcc, psc) ON CONFLICT IGNORE)");
        // create stats table
        database.execSQL("CREATE TABLE stats (row_id INTEGER PRIMARY KEY NOT NULL, total_locations INTEGER NOT NULL)");
        // insert default row
        database.execSQL("INSERT INTO stats (total_locations) SELECT COUNT(*) FROM measurements");
        // create on insert trigger
        database.execSQL("CREATE TRIGGER 'update_measurements_stats' AFTER INSERT ON measurements BEGIN "
                + "UPDATE stats SET total_locations = total_locations + 1; "
                + "END");
        // backup old table
        database.execSQL("ALTER TABLE cells RENAME TO cells_backup");
        // add new column with default null value
        database.execSQL("ALTER TABLE cells_backup ADD COLUMN discovered_at INTEGER DEFAULT NULL");
        // update value based on existing data
        database.execSQL("UPDATE cells_backup SET discovered_at = (SELECT MIN(measured_at) FROM measurements m WHERE m.cell_id = cells_backup.row_id)");
        // create new table
        database.execSQL("CREATE TABLE cells (row_id INTEGER PRIMARY KEY NOT NULL, mcc INTEGER NOT NULL, mnc INTEGER NOT NULL, lac INTEGER NOT NULL, cid INTEGER NOT NULL, psc INTEGER NOT NULL, net_type INTEGER NOT NULL, discovered_at INTEGER NOT NULL, UNIQUE (cid, lac, mnc, mcc, psc) ON CONFLICT IGNORE)");
        // create on delete trigger
        database.execSQL("CREATE TRIGGER 'archive_cell' BEFORE DELETE ON cells BEGIN INSERT INTO cells_archive (mcc, mnc, lac, cid, psc, net_type, discovered_at) VALUES (old.mcc, old.mnc, old.lac, old.cid, old.psc, old.net_type, old.discovered_at); END");
        // migrate data
        database.execSQL("CREATE TRIGGER 'migrate_cells' BEFORE DELETE ON cells_backup BEGIN "
                + "INSERT INTO cells (row_id, mcc, mnc, lac, cid, psc, net_type, discovered_at) VALUES (old.row_id, old.mcc, old.mnc, old.lac, old.cid, old.psc, old.net_type, old.discovered_at); "
                + "END");
        database.execSQL("DELETE FROM cells_backup");
        // delete backup
        database.execSQL("DROP TABLE cells_backup");
    }
}
