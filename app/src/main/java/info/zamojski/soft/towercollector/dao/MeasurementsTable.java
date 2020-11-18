/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao;

final class MeasurementsTable implements ITable {

    static final String TABLE_NAME = "measurements";
    static final String COLUMN_ROW_ID = "row_id";
    static final String COLUMN_LOCATION_HASHCODE = "location_hashcode";
    static final String COLUMN_LATITUDE = "lat";
    static final String COLUMN_LONGITUDE = "lon";
    static final String COLUMN_GPS_ACCURACY = "accuracy";
    static final String COLUMN_GPS_SPEED = "speed";
    static final String COLUMN_GPS_BEARING = "bearing";
    static final String COLUMN_GPS_ALTITUDE = "altitude";
    static final String COLUMN_MEASURED_AT = "measured_at";
    static final String COLUMN_UPLOADED_TO_OCID_AT = "uploaded_to_ocid_at";
    static final String COLUMN_UPLOADED_TO_MLS_AT = "uploaded_to_mls_at";

    private static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    static final String QUERY_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ROW_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            COLUMN_LOCATION_HASHCODE + " CHARACTER(40) NOT NULL, " +
            COLUMN_LATITUDE + " REAL NOT NULL, " +
            COLUMN_LONGITUDE + " REAL NOT NULL, " +
            COLUMN_GPS_ACCURACY + " REAL NOT NULL, " +
            COLUMN_GPS_SPEED + " REAL NOT NULL, " +
            COLUMN_GPS_BEARING + " REAL NOT NULL, " +
            COLUMN_GPS_ALTITUDE + " REAL NOT NULL, " +
            COLUMN_MEASURED_AT + " INTEGER NOT NULL, " +
            COLUMN_UPLOADED_TO_OCID_AT + " INTEGER DEFAULT NULL, " +
            COLUMN_UPLOADED_TO_MLS_AT + " INTEGER DEFAULT NULL)";

    private static final String QUERY_CREATE_INDEX_MEASURED_AT = "CREATE INDEX 'IX_" + TABLE_NAME + "_" + COLUMN_MEASURED_AT +
            "' ON " + TABLE_NAME + " (" + COLUMN_MEASURED_AT + " DESC)";

    private static final String QUERY_CREATE_INDEX_UPLOADED_TO_OCID_AT = "CREATE INDEX 'IX_" + TABLE_NAME + "_" + COLUMN_UPLOADED_TO_OCID_AT +
            "' ON " + TABLE_NAME + " (" + COLUMN_UPLOADED_TO_OCID_AT + " ASC)";

    private static final String QUERY_CREATE_INDEX_UPLOADED_TO_MLS_AT = "CREATE INDEX 'IX_" + TABLE_NAME + "_" + COLUMN_UPLOADED_TO_MLS_AT +
            "' ON " + TABLE_NAME + " (" + COLUMN_UPLOADED_TO_MLS_AT + " ASC)";

    @Override
    public String[] getCreateQueries() {
        return new String[]{
                QUERY_DROP_TABLE,
                QUERY_CREATE_TABLE,
                QUERY_CREATE_INDEX_MEASURED_AT,
                QUERY_CREATE_INDEX_UPLOADED_TO_OCID_AT,
                QUERY_CREATE_INDEX_UPLOADED_TO_MLS_AT
        };
    }
}
