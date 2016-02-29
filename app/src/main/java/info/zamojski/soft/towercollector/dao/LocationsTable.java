/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao;

final class LocationsTable implements ITable {

    static final String TABLE_NAME = "locations";
    static final String COLUMN_ROW_ID = "row_id";
    static final String COLUMN_LATITUDE = "lat";
    static final String COLUMN_LONGITUDE = "lon";
    static final String COLUMN_GPS_ACCURACY = "accuracy";
    static final String COLUMN_GPS_SPEED = "speed";
    static final String COLUMN_GPS_BEARING = "bearing";
    static final String COLUMN_GPS_ALTITUDE = "altitude";

    private static final String QUERY_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_ROW_ID + " CHARACTER(40) NOT NULL, "
            + COLUMN_LATITUDE + " REAL NOT NULL, "
            + COLUMN_LONGITUDE + " REAL NOT NULL, "
            + COLUMN_GPS_ACCURACY + " REAL NOT NULL, "
            + COLUMN_GPS_SPEED + " REAL NOT NULL, "
            + COLUMN_GPS_BEARING + " REAL NOT NULL, "
            + COLUMN_GPS_ALTITUDE + " REAL NOT NULL, "
            + "PRIMARY KEY (" + COLUMN_ROW_ID + ") ON CONFLICT IGNORE)";

    @Override
    public String[] getCreateQueries() {
        return new String[]{
                QUERY_CREATE_TABLE
        };
    }

}
