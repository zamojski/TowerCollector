/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao;

final class CellSignalsTable implements ITable {

    static final String TABLE_NAME = "cell_signals";
    static final String COLUMN_ROW_ID = "row_id";
    static final String COLUMN_MEASUREMENT_ID = "measurement_id";
    static final String COLUMN_CELL_ID = "cell_id";
    static final String COLUMN_PSC = "psc";
    static final String COLUMN_NEIGHBORING = "neighboring";
    static final String COLUMN_TA = "ta";
    static final String COLUMN_ASU = "asu";
    static final String COLUMN_DBM = "dbm";
    static final String COLUMN_RSRP = "rsrp";
    static final String COLUMN_RSRQ = "rsrq";
    static final String COLUMN_RSSI = "rssi";
    static final String COLUMN_RSSNR = "rssnr";
    static final String COLUMN_CQI = "cqi";
    static final String COLUMN_RSCP = "rscp";
    static final String COLUMN_CSI_RSRP = "csi_rsrp";
    static final String COLUMN_CSI_RSRQ = "csi_rsrq";
    static final String COLUMN_CSI_SINR = "csi_sinr";
    static final String COLUMN_SS_RSRP = "ss_rsrp";
    static final String COLUMN_SS_RSRQ = "ss_rsrq";
    static final String COLUMN_SS_SINR = "ss_sinr";
    static final String COLUMN_CDMA_DBM = "cdma_dbm";
    static final String COLUMN_CDMA_ECIO = "cdma_ecio";
    static final String COLUMN_EVDO_DBM = "evdo_dbm";
    static final String COLUMN_EVDO_ECIO = "evdo_ecio";
    static final String COLUMN_EVDO_SNR = "evdo_snr";
    static final String COLUMN_EC_NO = "ec_no";
    static final String COLUMN_ARFCN = "arfcn";

    private static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String QUERY_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ROW_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            COLUMN_MEASUREMENT_ID + " INTEGER NOT NULL, " +
            COLUMN_CELL_ID + " INTEGER NOT NULL, " +
            COLUMN_PSC + " INTEGER NOT NULL, " +
            COLUMN_NEIGHBORING + " INTEGER NOT NULL, " +
            COLUMN_TA + " INTEGER NOT NULL, " +
            COLUMN_ASU + " INTEGER NOT NULL, " +
            COLUMN_DBM + " INTEGER NOT NULL, " +
            COLUMN_RSRP + " INTEGER NOT NULL, " +
            COLUMN_RSRQ + " INTEGER NOT NULL, " +
            COLUMN_RSSI + " INTEGER NOT NULL, " +
            COLUMN_RSSNR + " INTEGER NOT NULL, " +
            COLUMN_CQI + " INTEGER NOT NULL, " +
            COLUMN_RSCP + " INTEGER NOT NULL, " +
            COLUMN_CSI_RSRP + " INTEGER NOT NULL, " +
            COLUMN_CSI_RSRQ + " INTEGER NOT NULL, " +
            COLUMN_CSI_SINR + " INTEGER NOT NULL, " +
            COLUMN_SS_RSRP + " INTEGER NOT NULL, " +
            COLUMN_SS_RSRQ + " INTEGER NOT NULL, " +
            COLUMN_SS_SINR + " INTEGER NOT NULL, " +
            COLUMN_CDMA_DBM + " INTEGER NOT NULL, " +
            COLUMN_CDMA_ECIO + " INTEGER NOT NULL, " +
            COLUMN_EVDO_DBM + " INTEGER NOT NULL, " +
            COLUMN_EVDO_ECIO + " INTEGER NOT NULL, " +
            COLUMN_EVDO_SNR + " INTEGER NOT NULL, " +
            COLUMN_EC_NO + " INTEGER NOT NULL, " +
            COLUMN_ARFCN + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + COLUMN_MEASUREMENT_ID + ") REFERENCES " + MeasurementsTable.TABLE_NAME + "(" + MeasurementsTable.COLUMN_ROW_ID + ")," +
            "FOREIGN KEY(" + COLUMN_CELL_ID + ") REFERENCES " + CellsTable.TABLE_NAME + "(" + CellsTable.COLUMN_ROW_ID + "))";

    private static final String QUERY_CREATE_INDEX_MEASUREMENT_ID = "CREATE INDEX 'IX_" + TABLE_NAME + "_" + COLUMN_MEASUREMENT_ID + "' ON " + TABLE_NAME + " (" + COLUMN_MEASUREMENT_ID + " DESC)";
    private static final String QUERY_CREATE_INDEX_CELL_ID = "CREATE INDEX 'IX_" + TABLE_NAME + "_" + COLUMN_CELL_ID + "' ON " + TABLE_NAME + " (" + COLUMN_CELL_ID + " DESC)";

    private static final String QUERY_CREATE_TRIGGER_ON_INSERT = "CREATE TRIGGER 'update_cell_signals_stats' AFTER INSERT ON " + TABLE_NAME + " " +
            "BEGIN " +
            "   UPDATE " + StatsTable.TABLE_NAME + " SET " + StatsTable.COLUMN_TOTAL_MEASUREMENTS + "  = " + StatsTable.COLUMN_TOTAL_MEASUREMENTS + " + 1; " +
            "END";

    @Override
    public String[] getCreateQueries() {
        return new String[]{
                QUERY_DROP_TABLE,
                QUERY_CREATE_TABLE,
                QUERY_CREATE_INDEX_MEASUREMENT_ID,
                QUERY_CREATE_INDEX_CELL_ID,
                QUERY_CREATE_TRIGGER_ON_INSERT
        };
    }
}
