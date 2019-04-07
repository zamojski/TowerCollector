/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao;

final class StatsTable implements ITable {

    static final String TABLE_NAME = "stats";
    static final String COLUMN_ROW_ID = "row_id";
    static final String COLUMN_TOTAL_MEASUREMENTS = "total_measurements";
    static final String COLUMN_TOTAL_DISCOVERED_CELLS = "total_discovered_cells";
    static final String COLUMN_TOTAL_SINCE = "total_since";

    private static final String QUERY_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ROW_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            COLUMN_TOTAL_MEASUREMENTS + " INTEGER NOT NULL, " +
            COLUMN_TOTAL_DISCOVERED_CELLS + " INTEGER NOT NULL, " +
            COLUMN_TOTAL_SINCE + " INTEGER NOT NULL)";

    private static final String QUERY_INSERT_DEFAULT_ROW = "INSERT INTO " + TABLE_NAME +
            " (" + COLUMN_TOTAL_MEASUREMENTS + ", " + COLUMN_TOTAL_DISCOVERED_CELLS + ", " + COLUMN_TOTAL_SINCE + ") " +
            "VALUES (0, 0, strftime('%s','now') * 1000)";

    @Override
    public String[] getCreateQueries() {
        return new String[]{
                QUERY_CREATE_TABLE,
                QUERY_INSERT_DEFAULT_ROW
        };
    }

}
