/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;

class UpgradeScript16 implements IUpgradeScript {

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        // backup old tables
        database.execSQL("ALTER TABLE cell_signals RENAME TO cell_signals_backup");
        // drop index on old table
        database.execSQL("DROP INDEX IF EXISTS IX_cell_signals_measurement_id");
        database.execSQL("DROP INDEX IF EXISTS IX_cell_signals_cell_id");
        // drop trigger on old table
        database.execSQL("DROP TRIGGER IF EXISTS update_cell_signals_stats");
        // create new table and index
        database.execSQL("CREATE TABLE cell_signals (row_id INTEGER PRIMARY KEY NOT NULL, measurement_id INTEGER NOT NULL, cell_id INTEGER NOT NULL, psc INTEGER NOT NULL, neighboring INTEGER NOT NULL, ta INTEGER NOT NULL, asu INTEGER NOT NULL, dbm INTEGER NOT NULL, rsrp INTEGER NOT NULL, rsrq INTEGER NOT NULL, rssi INTEGER NOT NULL, rssnr INTEGER NOT NULL, cqi INTEGER NOT NULL, rscp INTEGER NOT NULL, csi_rsrp INTEGER NOT NULL, csi_rsrq INTEGER NOT NULL, csi_sinr INTEGER NOT NULL, ss_rsrp INTEGER NOT NULL, ss_rsrq INTEGER NOT NULL, ss_sinr INTEGER NOT NULL, cdma_dbm INTEGER NOT NULL, cdma_ecio INTEGER NOT NULL, evdo_dbm INTEGER NOT NULL, evdo_ecio INTEGER NOT NULL, evdo_snr INTEGER NOT NULL, FOREIGN KEY(measurement_id) REFERENCES measurements(row_id), FOREIGN KEY(cell_id) REFERENCES cells(row_id))");
        database.execSQL("CREATE INDEX 'IX_cell_signals_measurement_id' on cell_signals (measurement_id DESC)");
        database.execSQL("CREATE INDEX 'IX_cell_signals_cell_id' on cell_signals (cell_id DESC)");
        // migrate data
        database.execSQL("CREATE TRIGGER 'migrate_cell_signals' BEFORE DELETE ON cell_signals_backup BEGIN INSERT INTO cell_signals (measurement_id, cell_id, psc, neighboring, ta, asu, dbm, rsrp, rsrq, rssi, rssnr, cqi, rscp, csi_rsrp, csi_rsrq, csi_sinr, ss_rsrp, ss_rsrq, ss_sinr, cdma_dbm, cdma_ecio, evdo_dbm, evdo_ecio, evdo_snr) VALUES (old.measurement_id, old.cell_id, old.psc, old.neighboring, old.ta, old.asu, old.dbm, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647, 2147483647); END");
        database.execSQL("DELETE FROM cell_signals_backup");
        // create new trigger
        database.execSQL("CREATE TRIGGER 'update_cell_signals_stats' AFTER INSERT ON cell_signals "
                + "BEGIN "
                + "UPDATE stats SET total_measurements = total_measurements + 1; "
                + "END;");
        // delete backup
        database.execSQL("DROP TABLE cell_signals_backup");
    }
}
