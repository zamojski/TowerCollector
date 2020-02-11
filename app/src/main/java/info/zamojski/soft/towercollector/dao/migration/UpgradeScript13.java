/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao.migration;

import android.database.sqlite.SQLiteDatabase;

class UpgradeScript13 implements IUpgradeScript {

    @Override
    public void performUpgrade(SQLiteDatabase database) {
        // backup old tables
        database.execSQL("ALTER TABLE locations RENAME TO locations_backup");
        database.execSQL("ALTER TABLE cells RENAME TO cells_backup");
        database.execSQL("ALTER TABLE cells_archive RENAME TO cells_archive_backup");
        database.execSQL("ALTER TABLE measurements RENAME TO measurements_backup");
        database.execSQL("ALTER TABLE stats RENAME TO stats_backup");
        database.execSQL("DROP VIEW not_uploaded_measurements");
        database.execSQL("CREATE VIEW not_uploaded_measurements_backup AS SELECT * FROM measurements_backup WHERE uploaded_to_ocid_at IS NULL AND uploaded_to_mls_at IS NULL");
        // drop triggers on old tables
        database.execSQL("DROP TRIGGER IF EXISTS update_measurements_stats");
        database.execSQL("DROP TRIGGER IF EXISTS archive_cell");
        // create new tables
        database.execSQL("CREATE TABLE stats (" +
                "    row_id                 INTEGER PRIMARY KEY" +
                "                                   NOT NULL," +
                "    total_measurements     INTEGER NOT NULL," +
                "    total_discovered_cells INTEGER NOT NULL," +
                "    total_since            INTEGER NOT NULL" +
                ");");
        database.execSQL("CREATE TABLE measurements (" +
                "    row_id              INTEGER PRIMARY KEY" +
                "                                NOT NULL," +
                "    location_hashcode   CHARACTER(40) NOT NULL," +
                "    lat                 REAL    NOT NULL," +
                "    lon                 REAL    NOT NULL," +
                "    accuracy            REAL    NOT NULL," +
                "    speed               REAL    NOT NULL," +
                "    bearing             REAL    NOT NULL," +
                "    altitude            REAL    NOT NULL," +
                "    measured_at         INTEGER NOT NULL," +
                "    uploaded_to_ocid_at INTEGER DEFAULT NULL," +
                "    uploaded_to_mls_at  INTEGER DEFAULT NULL" +
                ");");
        database.execSQL("CREATE TABLE cells (" +
                "    row_id        INTEGER PRIMARY KEY" +
                "                          NOT NULL," +
                "    mcc           INTEGER NOT NULL," +
                "    mnc           INTEGER NOT NULL," +
                "    lac           INTEGER NOT NULL," +
                "    cid           INTEGER NOT NULL," +
                "    net_type      INTEGER NOT NULL," +
                "    discovered_at INTEGER NOT NULL," +
                "    UNIQUE (cid, lac, mnc, mcc, net_type)" +
                "    ON CONFLICT IGNORE" +
                ");");
        database.execSQL("CREATE TABLE cell_signals (" +
                "    row_id         INTEGER PRIMARY KEY" +
                "                           NOT NULL," +
                "    measurement_id INTEGER NOT NULL," +
                "    cell_id        INTEGER NOT NULL," +
                "    psc            INTEGER NOT NULL," +
                "    neighboring    INTEGER NOT NULL," +
                "    ta             INTEGER NOT NULL," +
                "    asu            INTEGER NOT NULL," +
                "    dbm            INTEGER NOT NULL," +
                "    FOREIGN KEY (measurement_id) REFERENCES measurements (row_id)," +
                "    FOREIGN KEY (cell_id) REFERENCES cells (row_id) " +
                ");");
        // migrate stats
        database.execSQL("INSERT INTO stats (row_id, total_measurements, total_discovered_cells, total_since) SELECT" +
                "    1 AS row_id," +
                "    total_locations AS total_measurements," +
                "    (" +
                "        SELECT COUNT(*) FROM (SELECT cid, lac, mnc, mcc, net_type FROM cells_backup c WHERE c.row_id IN (SELECT DISTINCT cell_id FROM not_uploaded_measurements_backup)" +
                "        UNION SELECT cid, lac, mnc,mcc, net_type FROM cells_archive_backup)" +
                "    ) AS total_discovered_cells," +
                "    IFNULL((" +
                "        SELECT MIN(discovered_at) FROM cells_backup c WHERE c.row_id IN (SELECT DISTINCT cell_id FROM not_uploaded_measurements_backup)" +
                "        UNION SELECT discovered_at FROM cells_archive_backup" +
                "    ), strftime('%s','now') * 1000) AS total_since" +
                "    FROM stats_backup LIMIT 0,1");
        // move all archived cells to cells table to remove duplicates and simplify migration
        database.execSQL("INSERT INTO cells_archive_backup (mcc, mnc, lac, cid, net_type, discovered_at) SELECT mcc, mnc, lac, cid, net_type, discovered_at FROM cells_backup");
        // create temp table containing all data
        database.execSQL("CREATE TEMP TABLE temp_full AS " +
                "SELECT hashcode, psc, neighboring, ta, asu, dbm, measured_at, uploaded_to_ocid_at, uploaded_to_mls_at, lat, lon, accuracy, speed, bearing, altitude, mcc, mnc, lac, cid, net_type, discovered_at " +
                "FROM not_uploaded_measurements_backup m " +
                "INNER JOIN locations_backup l on m.location_id = l.row_id " +
                "INNER JOIN cells_archive_backup c on m.cell_id = c.row_id");
        // migrate data part 1
        database.execSQL("INSERT INTO cells (mcc, mnc, lac, cid, net_type, discovered_at) " +
                "SELECT DISTINCT mcc, mnc, lac, cid, net_type, discovered_at FROM cells_archive_backup");
        database.execSQL("INSERT INTO measurements (location_hashcode, lat, lon, accuracy, speed, bearing, altitude, measured_at, uploaded_to_ocid_at, uploaded_to_mls_at) " +
                "SELECT hashcode AS location_hashcode, lat, lon, accuracy, speed, bearing, altitude, measured_at, uploaded_to_ocid_at, uploaded_to_mls_at FROM temp_full GROUP BY hashcode, measured_at");
        // drop indexes on old tables
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_measured_at");
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_location_id");
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_cell_id");
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_uploaded_to_ocid_at");
        database.execSQL("DROP INDEX IF EXISTS IX_measurements_uploaded_to_mls_at");
        // create new indexes
        database.execSQL("CREATE INDEX 'IX_measurements_measured_at' ON measurements (measured_at DESC)");
        database.execSQL("CREATE INDEX 'IX_measurements_uploaded_to_ocid_at' ON measurements (uploaded_to_ocid_at ASC)");
        database.execSQL("CREATE INDEX 'IX_measurements_uploaded_to_mls_at' ON measurements (uploaded_to_mls_at ASC)");
        database.execSQL("CREATE INDEX 'IX_cell_signals_measurement_id' ON cell_signals (measurement_id DESC)");
        database.execSQL("CREATE INDEX 'IX_cell_signals_cell_id' ON cell_signals (cell_id DESC)");
        // migrate data part 2
        database.execSQL("INSERT INTO cell_signals (psc, neighboring, ta, asu, dbm, measurement_id, cell_id) " +
                "SELECT psc, neighboring, ta, asu, dbm, " +
                "   (SELECT m.row_id FROM measurements m WHERE m.location_hashcode = tf.hashcode AND m.measured_at = tf.measured_at) AS measurement_id, " +
                "   (SELECT c.row_id FROM cells c WHERE c.mcc = tf.mcc AND c.mnc = tf.mnc AND c.lac = tf.lac AND c.cid = tf.cid AND c.net_type = tf.net_type) AS cell_id " +
                "FROM temp_full tf");
        // drop temp table
        database.execSQL("DROP TABLE temp_full");
        // drop backups
        database.execSQL("DROP VIEW not_uploaded_measurements_backup");
        database.execSQL("DROP TABLE measurements_backup");
        database.execSQL("DROP TABLE locations_backup");
        database.execSQL("DROP TABLE cells_backup");
        database.execSQL("DROP TABLE cells_archive_backup");
        database.execSQL("DROP TABLE stats_backup");
        // create new triggers
        database.execSQL("CREATE TRIGGER 'update_cells_stats' AFTER INSERT ON cells " +
                "BEGIN " +
                "   UPDATE stats SET total_discovered_cells = total_discovered_cells + 1; " +
                "END;");
        database.execSQL("CREATE TRIGGER 'update_cell_signals_stats' AFTER INSERT ON cell_signals " +
                "BEGIN " +
                "   UPDATE stats SET total_measurements = total_measurements + 1; " +
                "END;");
        // create new view
        database.execSQL("CREATE VIEW not_uploaded_measurements AS SELECT * FROM measurements WHERE uploaded_to_ocid_at IS NULL AND uploaded_to_mls_at IS NULL");
    }
}
