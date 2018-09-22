/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao;

final class MeasurementsTable implements ITable {

    static final String TABLE_NAME = "measurements";
    static final String COLUMN_ROW_ID = "row_id";
    static final String COLUMN_LOCATION_ID = "location_id";
    static final String COLUMN_CELL_ID = "cell_id";
    static final String COLUMN_PSC = "psc";
    static final String COLUMN_NEIGHBORING = "neighboring";
    static final String COLUMN_TA = "ta";
    static final String COLUMN_ASU = "asu";
    static final String COLUMN_DBM = "dbm";
    static final String COLUMN_MEASURED_AT = "measured_at";
    static final String COLUMN_UPLOADED_TO_OCID_AT = "uploaded_to_ocid_at";
    static final String COLUMN_UPLOADED_TO_MLS_AT = "uploaded_to_mls_at";

    static final String QUERY_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_ROW_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + COLUMN_LOCATION_ID + " INTEGER NOT NULL, "
            + COLUMN_CELL_ID + " INTEGER NOT NULL, "
            + COLUMN_PSC + " INTEGER NOT NULL, "
            + COLUMN_NEIGHBORING + " INTEGER NOT NULL, "
            + COLUMN_TA + " INTEGER NOT NULL, "
            + COLUMN_ASU + " INTEGER NOT NULL, "
            + COLUMN_DBM + " INTEGER NOT NULL, "
            + COLUMN_MEASURED_AT + " INTEGER NOT NULL, "
            + COLUMN_UPLOADED_TO_OCID_AT + " INTEGER DEFAULT NULL, "
            + COLUMN_UPLOADED_TO_MLS_AT + " INTEGER DEFAULT NULL, "
            + "FOREIGN KEY(" + COLUMN_LOCATION_ID + ") REFERENCES " + LocationsTable.TABLE_NAME + "(" + LocationsTable.COLUMN_ROW_ID + "),"
            + "FOREIGN KEY(" + COLUMN_CELL_ID + ") REFERENCES " + CellsTable.TABLE_NAME + "(" + CellsTable.COLUMN_ROW_ID + "))";

    private static final String QUERY_CREATE_INDEX_MEASURED_AT = "CREATE INDEX 'IX_" + TABLE_NAME + "_" + COLUMN_MEASURED_AT
            + "' ON " + TABLE_NAME + " (" + COLUMN_MEASURED_AT + " DESC)";

    private static final String QUERY_CREATE_INDEX_LOCATION_ID = "CREATE INDEX 'IX_" + TABLE_NAME + "_" + COLUMN_LOCATION_ID
            + "' ON " + TABLE_NAME + " (" + COLUMN_LOCATION_ID + " ASC)";

    private static final String QUERY_CREATE_INDEX_CELL_ID = "CREATE INDEX 'IX_" + TABLE_NAME + "_" + COLUMN_CELL_ID
            + "' ON " + TABLE_NAME + " (" + COLUMN_CELL_ID + " DESC)";

    private static final String QUERY_CREATE_INDEX_UPLOADED_TO_OCID_AT = "CREATE INDEX 'IX_" + TABLE_NAME + "_" + COLUMN_UPLOADED_TO_OCID_AT
            + "' ON " + TABLE_NAME + " (" + COLUMN_UPLOADED_TO_OCID_AT + " ASC)";

    private static final String QUERY_CREATE_INDEX_UPLOADED_TO_MLS_AT = "CREATE INDEX 'IX_" + TABLE_NAME + "_" + COLUMN_UPLOADED_TO_MLS_AT
            + "' ON " + TABLE_NAME + " (" + COLUMN_UPLOADED_TO_MLS_AT + " ASC)";

    private static final String QUERY_CREATE_TRIGGER_ON_INSERT = "CREATE TRIGGER 'update_measurements_stats' AFTER INSERT ON " + TABLE_NAME + " BEGIN UPDATE "
            + StatsTable.TABLE_NAME + " SET " + StatsTable.COLUMN_TOTAL_LOCATIONS + " = " + StatsTable.COLUMN_TOTAL_LOCATIONS + " + 1; END";

    @Override
    public String[] getCreateQueries() {
        return new String[]{
                QUERY_CREATE_TABLE,
                QUERY_CREATE_INDEX_MEASURED_AT,
                QUERY_CREATE_INDEX_LOCATION_ID,
                QUERY_CREATE_INDEX_CELL_ID,
                QUERY_CREATE_TRIGGER_ON_INSERT,
                QUERY_CREATE_INDEX_UPLOADED_TO_OCID_AT,
                QUERY_CREATE_INDEX_UPLOADED_TO_MLS_AT
        };
    }
}
