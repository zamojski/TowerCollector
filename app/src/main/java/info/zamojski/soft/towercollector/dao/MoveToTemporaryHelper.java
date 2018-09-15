/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao;

final class MoveToTemporaryHelper {

    static final String MOVE_CELLS_QUERY = "INSERT OR REPLACE INTO " + TemporaryCellsTable.TABLE_NAME + " (" + TemporaryCellsTable.COLUMN_ROW_ID + ", " +
            TemporaryCellsTable.COLUMN_MCC + ", " + TemporaryCellsTable.COLUMN_MCC + ", " + TemporaryCellsTable.COLUMN_LAC + ", " +
            TemporaryCellsTable.COLUMN_CID + ", " + TemporaryCellsTable.COLUMN_NET_TYPE + ", " + TemporaryCellsTable.COLUMN_DISCOVERED_AT + ") " +
            "SELECT " + CellsTable.COLUMN_ROW_ID + ", " + CellsTable.COLUMN_MCC + ", " + CellsTable.COLUMN_MNC + ", " + CellsTable.COLUMN_LAC + ", " +
            CellsTable.COLUMN_CID + ", " + CellsTable.COLUMN_NET_TYPE + ", " + CellsTable.COLUMN_DISCOVERED_AT + " FROM " + CellsTable.TABLE_NAME + " " +
            "WHERE " + TemporaryCellsTable.COLUMN_ROW_ID + " IN (SELECT DISTINCT m." + MeasurementsTable.COLUMN_CELL_ID + " FROM " + MeasurementsTable.TABLE_NAME + " m WHERE m." + MeasurementsTable.COLUMN_ROW_ID + " IN (%s))";

    static final String MOVE_LOCATIONS_QUERY = "INSERT OR REPLACE INTO " + TemporaryLocationsTable.TABLE_NAME + " (" + TemporaryLocationsTable.COLUMN_ROW_ID + ", " +
            TemporaryLocationsTable.COLUMN_HASHCODE + ", " + TemporaryLocationsTable.COLUMN_LATITUDE + ", " + TemporaryLocationsTable.COLUMN_LONGITUDE + ", " + TemporaryLocationsTable.COLUMN_GPS_ACCURACY + ", " +
            TemporaryLocationsTable.COLUMN_GPS_SPEED + ", " + TemporaryLocationsTable.COLUMN_GPS_BEARING + ", " + TemporaryLocationsTable.COLUMN_GPS_ALTITUDE + ") " +
            "SELECT " + LocationsTable.COLUMN_ROW_ID + ", " + LocationsTable.COLUMN_HASHCODE + ", " + LocationsTable.COLUMN_LATITUDE + ", " + LocationsTable.COLUMN_LONGITUDE + ", " +
            LocationsTable.COLUMN_GPS_ACCURACY + ", " + LocationsTable.COLUMN_GPS_SPEED + ", " + LocationsTable.COLUMN_GPS_BEARING + ", " + LocationsTable.COLUMN_GPS_ALTITUDE + " FROM " + LocationsTable.TABLE_NAME + " " +
            "WHERE " + TemporaryLocationsTable.COLUMN_ROW_ID + " IN (SELECT DISTINCT m." + MeasurementsTable.COLUMN_LOCATION_ID + " FROM " + MeasurementsTable.TABLE_NAME + " m WHERE m." + MeasurementsTable.COLUMN_ROW_ID + " IN (%s))";

    static final String MOVE_MEASUREMENTS_QUERY = "INSERT OR REPLACE INTO temporary_measurements (" + TemporaryMeasurementsTable.COLUMN_ROW_ID + ", " + TemporaryMeasurementsTable.COLUMN_LOCATION_ID + ", " +
            TemporaryMeasurementsTable.COLUMN_CELL_ID + ", " + TemporaryMeasurementsTable.COLUMN_PSC + ", " + TemporaryMeasurementsTable.COLUMN_NEIGHBORING + ", " + TemporaryMeasurementsTable.COLUMN_TA + ", " +
            TemporaryMeasurementsTable.COLUMN_ASU + ", " + TemporaryMeasurementsTable.COLUMN_DBM + ", " + TemporaryMeasurementsTable.COLUMN_MEASURED_AT + ", " +
            TemporaryMeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT + ", " + TemporaryMeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT + ")" +
            "SELECT " + MeasurementsTable.COLUMN_ROW_ID + ", " + MeasurementsTable.COLUMN_LOCATION_ID + ", " + MeasurementsTable.COLUMN_CELL_ID + ", " + MeasurementsTable.COLUMN_PSC + ", " +
            MeasurementsTable.COLUMN_NEIGHBORING + ", " + MeasurementsTable.COLUMN_TA + ", " + MeasurementsTable.COLUMN_ASU + ", " + MeasurementsTable.COLUMN_DBM + ", " +
            MeasurementsTable.COLUMN_MEASURED_AT + ", %s, %s FROM " + MeasurementsTable.TABLE_NAME + " WHERE row_id IN (%s)";
}
