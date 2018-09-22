/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao;

public class NotUploadedMeasurementsView implements ITable {

    static final String VIEW_NAME = "not_uploaded_measurements";

    static final String QUERY_CREATE_VIEW = "CREATE VIEW " + VIEW_NAME + " AS "
            + "SELECT "
            /*+ MeasurementsTable.TABLE_NAME + "." */+ MeasurementsTable.COLUMN_ROW_ID + ", "
            + MeasurementsTable.COLUMN_LOCATION_ID + ", "
//            + LocationsTable.COLUMN_HASHCODE + ", "
//            + LocationsTable.COLUMN_LATITUDE + ", "
//            + LocationsTable.COLUMN_LONGITUDE + ", "
//            + LocationsTable.COLUMN_GPS_ACCURACY + ", "
//            + LocationsTable.COLUMN_GPS_SPEED + ", "
//            + LocationsTable.COLUMN_GPS_BEARING + ", "
//            + LocationsTable.COLUMN_GPS_ALTITUDE + ", "
            + MeasurementsTable.COLUMN_CELL_ID + ", "
//            + CellsTable.COLUMN_MCC + ", "
//            + CellsTable.COLUMN_MNC + ", "
//            + CellsTable.COLUMN_LAC + ", "
//            + CellsTable.COLUMN_CID + ", "
//            + CellsTable.COLUMN_NET_TYPE + ", "
//            + CellsTable.COLUMN_DISCOVERED_AT + ", "
            + MeasurementsTable.COLUMN_PSC + ", "
            + MeasurementsTable.COLUMN_NEIGHBORING + ", "
            + MeasurementsTable.COLUMN_TA + ", "
            + MeasurementsTable.COLUMN_ASU + ", "
            + MeasurementsTable.COLUMN_DBM + ", "
            + MeasurementsTable.COLUMN_MEASURED_AT + ", "
            + MeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT + ", "
            + MeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT
            + " FROM " + MeasurementsTable.TABLE_NAME + " WHERE " + MeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT + " IS NULL AND " + MeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT + " IS NULL";

    @Override
    public String[] getCreateQueries() {
        return new String[]{
                QUERY_CREATE_VIEW
        };
    }
}
