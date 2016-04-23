/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.dao.migration.DbMigrationHelper;
import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;
import info.zamojski.soft.towercollector.model.Boundaries;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.model.Statistics;
import info.zamojski.soft.towercollector.utils.HashUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.acra.ACRA;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import trikita.log.Log;

public class MeasurementsDatabase {

    private static final String TAG = MeasurementsDatabase.class.getSimpleName();

    public static final String DATABASE_FILE_NAME = "measurements.db";
    public static final int DATABASE_FILE_VERSION = 11;

    private static final int NUM_OF_DELETIONS_PER_ONE_QUERY = 50;

    private final MeasurementsOpenHelper helper;

    private static volatile MeasurementsDatabase instance = null;

    private boolean insertionFailureReported = false;
    
    private Measurement lastMeasurementCache;
    private Statistics lastStatisticsCache;

    private MeasurementsDatabase(Context context) {
        helper = new MeasurementsOpenHelper(context);
    }

    public boolean insertMeasurements(Measurement[] measurements) {
        Log.d(TAG, "insertMeasurement(): Inserting %s measurements", measurements.length);
        boolean[] results = new boolean[measurements.length];
        boolean overallResult = true;
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();
            StringBuilder resultSb = new StringBuilder();
            for (int mIndex = 0; mIndex < measurements.length; mIndex++) {
                Measurement measurement = measurements[mIndex];
                resultSb.append(measurement.toString());
                // insert cell
                {
                    ContentValues values = new ContentValues();
                    values.put(CellsTable.COLUMN_MCC, measurement.getMcc());
                    values.put(CellsTable.COLUMN_MNC, measurement.getMnc());
                    values.put(CellsTable.COLUMN_LAC, measurement.getLac());
                    values.put(CellsTable.COLUMN_CID, measurement.getCid());
                    values.put(CellsTable.COLUMN_NET_TYPE, measurement.getNetworkType().ordinal());
                    values.put(CellsTable.COLUMN_DISCOVERED_AT, System.currentTimeMillis());
                    long rowId = db.insert(CellsTable.TABLE_NAME, null, values);
                    boolean localResult = (rowId != -1);
                    Log.d(TAG, "insertMeasurement(): Cell inserted = %s", localResult);
                    resultSb.append("\tcell inserted=").append(localResult);
                }
                // don't use value returned by insert, because it sometimes returns wrong value -> query always
                long cellId = -1;
                {
                    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                    queryBuilder.setTables(CellsTable.TABLE_NAME);
                    String[] columns = new String[]{CellsTable.COLUMN_ROW_ID};
                    String selection = CellsTable.COLUMN_CID + " = ? AND " + CellsTable.COLUMN_LAC + " = ? AND " + CellsTable.COLUMN_MNC + " = ? AND " + CellsTable.COLUMN_MCC + " = ? AND " + CellsTable.COLUMN_NET_TYPE + " = ?";
                    String[] selectionArgs = new String[]{String.valueOf(measurement.getCid()), String.valueOf(measurement.getLac()), String.valueOf(measurement.getMnc()), String.valueOf(measurement.getMcc()), String.valueOf(measurement.getNetworkType().ordinal())};
                    Cursor cursorTotal = queryBuilder.query(db, columns, selection, selectionArgs, null, null, null);
                    boolean localResult = false;
                    if (cursorTotal.moveToNext()) {
                        cellId = cursorTotal.getInt(cursorTotal.getColumnIndex(CellsTable.COLUMN_ROW_ID));
                        localResult = true;
                    }
                    cursorTotal.close();
                    results[mIndex] = localResult;
                    Log.d(TAG, "insertMeasurement(): Cell found = %s", localResult);
                    resultSb.append("\tcell found=").append(localResult);
                }
                // calculate hashcode
                String locationHashCode = HashUtils.toSha1(measurement.getLatitude(), measurement.getLongitude(), measurement.getGpsAccuracy(),
                        measurement.getGpsSpeed(), measurement.getGpsBearing(), measurement.getGpsAltitude());
                // insert location
                {
                    ContentValues values = new ContentValues();
                    values.put(LocationsTable.COLUMN_HASHCODE, locationHashCode);
                    values.put(LocationsTable.COLUMN_LATITUDE, measurement.getLatitude());
                    values.put(LocationsTable.COLUMN_LONGITUDE, measurement.getLongitude());
                    values.put(LocationsTable.COLUMN_GPS_ACCURACY, measurement.getGpsAccuracy());
                    values.put(LocationsTable.COLUMN_GPS_SPEED, measurement.getGpsSpeed());
                    values.put(LocationsTable.COLUMN_GPS_BEARING, measurement.getGpsBearing());
                    values.put(LocationsTable.COLUMN_GPS_ALTITUDE, measurement.getGpsAltitude());
                    long rowId = db.insert(LocationsTable.TABLE_NAME, null, values);
                    boolean localResult = (rowId != -1);
                    Log.d(TAG, "insertMeasurement(): Location inserted = %s", localResult);
                    resultSb.append("\tlocation inserted=").append(localResult);
                }
                // don't use value returned by insert, because it sometimes returns wrong value -> query always
                long locationId = -1;
                {
                    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                    queryBuilder.setTables(LocationsTable.TABLE_NAME);
                    String[] columns = new String[] { CellsTable.COLUMN_ROW_ID };
                    String selection = LocationsTable.COLUMN_HASHCODE + " = ?";
                    String[] selectionArgs = new String[] { locationHashCode };
                    Cursor cursorTotal = queryBuilder.query(db, columns, selection, selectionArgs, null, null, null);
                    boolean localResult = false;
                    if (cursorTotal.moveToNext()) {
                        locationId = cursorTotal.getInt(cursorTotal.getColumnIndex(CellsTable.COLUMN_ROW_ID));
                        localResult = true;
                    }
                    cursorTotal.close();
                    results[mIndex] = localResult;
                    Log.d(TAG, "insertMeasurement(): Location found = %s", localResult);
                    resultSb.append("\tlocation found=").append(localResult);
                }
                // insert measurement (if previous queries returned correct result)
                if (results[mIndex]) {
                    ContentValues values = new ContentValues();
                    values.put(MeasurementsTable.COLUMN_CELL_ID, cellId);
                    values.put(MeasurementsTable.COLUMN_LOCATION_ID, locationId);
                    values.put(MeasurementsTable.COLUMN_PSC, measurement.getPsc());
                    values.put(MeasurementsTable.COLUMN_NEIGHBORING, measurement.isNeighboring());
                    values.put(MeasurementsTable.COLUMN_TA, measurement.getTa());
                    values.put(MeasurementsTable.COLUMN_ASU, measurement.getAsu());
                    values.put(MeasurementsTable.COLUMN_DBM, measurement.getDbm());
                    values.put(MeasurementsTable.COLUMN_MEASURED_AT, measurement.getTimestamp());
                    long rowId = db.insert(MeasurementsTable.TABLE_NAME, null, values);
                    boolean localResult = (rowId != -1);
                    results[mIndex] &= localResult;
                    Log.d(TAG, "insertMeasurement(): Measurement inserted = %s", localResult);
                    resultSb.append("\tmeasurement inserted=").append(localResult);
                }
                resultSb.append(";\r\n");
            }
            // commit
            for (boolean result : results) {
                overallResult &= result;
            }
            if (overallResult) {
                db.setTransactionSuccessful();
                Log.d(TAG, "insertMeasurements(): Measurements inserted successfully");
                Log.d(TAG, "insertMeasurements(): Insertion report: %s", resultSb.toString());
            } else {
                Log.d(TAG, "insertMeasurements(): Measurements not inserted");
                Log.d(TAG, "insertMeasurements(): Insertion report: %s", resultSb.toString());
                // report exception because it shouldn't occur (one time per app run)
                if (!insertionFailureReported) {
                    Throwable ex = new MeasurementInsertionFailedException("Measurements not inserted", resultSb.toString());
                    MyApplication.getAnalytics().sendException(ex, Boolean.FALSE);
                    ACRA.getErrorReporter().handleSilentException(ex);
                    insertionFailureReported = true;
                }
            }
        } finally {
            invalidateCache();
            db.endTransaction();
        }
        return overallResult;
    }

    public boolean insertMeasurement(Measurement measurement) {
        return insertMeasurements(new Measurement[]{measurement});
    }

    public Measurement getFirstMeasurement() {
        Measurement firstMeasurement = null;
        List<Measurement> measurements = getMeasurements(null, null, null, null, MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_MEASURED_AT + " ASC, " + MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_ROW_ID + " ASC", "1");
        if (!measurements.isEmpty())
            firstMeasurement = measurements.get(0);
        Log.d(TAG, "getFirstMeasurement(): %s", firstMeasurement);
        return firstMeasurement;
    }

    public Measurement getLastMeasurement() {
        // Try to get from cache then read from DB
        if (this.lastMeasurementCache != null) {
            Log.d(TAG, "getLastMeasurement(): Value from cache: %s", this.lastMeasurementCache);
            return this.lastMeasurementCache;
        }
        Measurement lastMeasurement = null;
        List<Measurement> measurements = getMeasurements(null, null, null, null,
                MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_MEASURED_AT + " DESC, "
                        + MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_NEIGHBORING + " DESC, "
                        + MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_ROW_ID + " DESC",
                "1");
        if (!measurements.isEmpty()) {
            lastMeasurement = measurements.get(0);
        }
        Log.d(TAG, "getLastMeasurement(): Value from DB: %s", lastMeasurement);
        this.lastMeasurementCache = lastMeasurement;
        return lastMeasurement;
    }

    public int getAllMeasurementsCount() {
        int count = 0;
        Log.d(TAG, "getAllMeasurementsCount(): Getting number of measurements");
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MeasurementsTable.TABLE_NAME);
        String[] columns = new String[]{"COUNT(*) AS LOCATIONS_COUNT"};
        Cursor cursorTotal = queryBuilder.query(db, columns, null, null, null, null, null);
        if (cursorTotal.moveToNext()) {
            count = cursorTotal.getInt(cursorTotal.getColumnIndex("LOCATIONS_COUNT"));
        }
        cursorTotal.close();
        return count;
    }

    public Statistics getMeasurementsStatistics() {
        // Try to get from cache then read from DB
        if (this.lastStatisticsCache != null) {
            Log.d(TAG, "getMeasurementsStatistics(): Value from cache: %s", this.lastStatisticsCache);
            return this.lastStatisticsCache;
        }
        Statistics stats = new Statistics();
        SQLiteDatabase db = helper.getReadableDatabase();
        // calculate midnight date (beginning of day)
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        final String todayCellsCount = "TODAY_CELLS_COUNT";
        final String todayLocationsCount = "TODAY_LOCATIONS_COUNT";
        final String todayDiscoveredCellsCount = "TODAY_DISCOVERED_CELLS_COUNT";
        final String localCellsCount = "LOCAL_CELLS_COUNT";
        final String localLocationsCount = "LOCAL_LOCATIONS_COUNT";
        final String localDiscoveredCellsCount = "LOCAL_DISCOVERED_CELLS_COUNT";
        final String localDiscoveredCellsSince = "LOCAL_DISCOVERED_CELLS_SINCE";
        final String globalLocationsCount = "GLOBAL_LOCATIONS_COUNT";
        final String globalDiscoveredCellsCount = "GLOBAL_DISCOVERED_CELLS_COUNT";
        final String globalDiscoveredCellsSince = "GLOBAL_DISCOVERED_CELLS_SINCE";
        String todayTime = String.valueOf(todayCalendar.getTimeInMillis());
        String[] selectionArgs = new String[]{todayTime, todayTime};
        // get all in one query (raw is the only possible solution)
        // partial queries
        String cellsTablePrimaryKeyColumns = CellsTable.COLUMN_CID + ", " + CellsTable.COLUMN_LAC
                + ", " + CellsTable.COLUMN_MNC + ", " + CellsTable.COLUMN_MCC + ", " + CellsTable.COLUMN_NET_TYPE;
        String cellsArchiveTablePrimaryKeyColumns = CellsArchiveTable.COLUMN_CID + ", " + CellsArchiveTable.COLUMN_LAC
                + ", " + CellsArchiveTable.COLUMN_MNC + ", " + CellsArchiveTable.COLUMN_MCC + ", " + CellsArchiveTable.COLUMN_NET_TYPE;
        // full queries
        String todayCellsLocationsQuery = "SELECT COUNT(DISTINCT " + MeasurementsTable.COLUMN_CELL_ID + ") AS " + todayCellsCount + ", "
                + "COUNT(*) AS " + todayLocationsCount + " FROM " + MeasurementsTable.TABLE_NAME + " WHERE " + MeasurementsTable.COLUMN_MEASURED_AT + " >= ?";
        String todayDiscoveredCellsQuery = "SELECT COUNT(*) AS " + todayDiscoveredCellsCount + " FROM (SELECT " + cellsTablePrimaryKeyColumns
                + " FROM " + CellsTable.TABLE_NAME + " WHERE " + CellsTable.COLUMN_DISCOVERED_AT
                + " >= ? EXCEPT SELECT " + cellsArchiveTablePrimaryKeyColumns + " FROM " + CellsArchiveTable.TABLE_NAME + ")";
        String localCellsQuery = "SELECT COUNT(" + CellsTable.COLUMN_ROW_ID + ") AS " + localCellsCount + ", MIN(" + CellsTable.COLUMN_DISCOVERED_AT + ") AS "
                + localDiscoveredCellsSince + " FROM " + CellsTable.TABLE_NAME;
        String localLocationsQuery = "SELECT COUNT(" + MeasurementsTable.COLUMN_ROW_ID + ") AS " + localLocationsCount + " FROM " + MeasurementsTable.TABLE_NAME;
        String localDiscoveredCellsQuery = "SELECT COUNT(*) AS " + localDiscoveredCellsCount + " FROM (SELECT " + cellsTablePrimaryKeyColumns
                + " FROM " + CellsTable.TABLE_NAME + " EXCEPT SELECT " + cellsArchiveTablePrimaryKeyColumns + " FROM " + CellsArchiveTable.TABLE_NAME + ")";
        String globalLocationsQuery = "SELECT " + StatsTable.COLUMN_TOTAL_LOCATIONS + " AS " + globalLocationsCount + " FROM "
                + StatsTable.TABLE_NAME + " LIMIT 0, 1";
        String globalDiscoveredCellsQuery = "SELECT COUNT(*) AS " + globalDiscoveredCellsCount + " FROM (SELECT "
                + cellsTablePrimaryKeyColumns + " FROM " + CellsTable.TABLE_NAME + " UNION SELECT "
                + cellsArchiveTablePrimaryKeyColumns + " FROM " + CellsArchiveTable.TABLE_NAME + ")";
        String globalDiscoveredSinceQuery = "SELECT " + CellsTable.COLUMN_DISCOVERED_AT + " AS " + globalDiscoveredCellsSince
                + " FROM " + CellsTable.TABLE_NAME + " UNION SELECT " + CellsArchiveTable.COLUMN_DISCOVERED_AT
                + " FROM " + CellsArchiveTable.TABLE_NAME + " ORDER BY " + CellsTable.COLUMN_DISCOVERED_AT + " ASC LIMIT 0, 1";
        //Log.d(TAG, todayDiscoveredCellsQuery);
        //Log.d(TAG, localDiscoveredCellsQuery);
        //Log.d(TAG, globalDiscoveredCellsQuery);
        String query = "SELECT * FROM ((" + todayCellsLocationsQuery + ") "
                + "JOIN (" + todayDiscoveredCellsQuery + ") "
                + "JOIN (" + localCellsQuery + ") "
                + "JOIN (" + localLocationsQuery + ") "
                + "JOIN (" + localDiscoveredCellsQuery + ") "
                + "JOIN (" + globalLocationsQuery + ") "
                + "JOIN (" + globalDiscoveredCellsQuery + ") "
                + "JOIN (" + globalDiscoveredSinceQuery + "))";
        // Log.d(TAG, query);
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor.moveToNext()) {
            stats.setCellsToday(cursor.getInt(cursor.getColumnIndex(todayCellsCount)));
            stats.setLocationsToday(cursor.getInt(cursor.getColumnIndex(todayLocationsCount)));
            stats.setDiscoveredCellsToday(cursor.getInt(cursor.getColumnIndex(todayDiscoveredCellsCount)));
            stats.setCellsLocal(cursor.getInt(cursor.getColumnIndex(localCellsCount)));
            stats.setLocationsLocal(cursor.getInt(cursor.getColumnIndex(localLocationsCount)));
            stats.setDiscoveredCellsLocal(cursor.getInt(cursor.getColumnIndex(localDiscoveredCellsCount)));
            stats.setSinceLocal(cursor.getLong(cursor.getColumnIndex(localDiscoveredCellsSince)));
            stats.setLocationsGlobal(cursor.getInt(cursor.getColumnIndex(globalLocationsCount)));
            stats.setDiscoveredCellsGlobal(cursor.getInt(cursor.getColumnIndex(globalDiscoveredCellsCount)));
            stats.setSinceGlobal(cursor.getLong(cursor.getColumnIndex(globalDiscoveredCellsSince)));
        }
        cursor.close();
        Log.d(TAG, "getMeasurementsStatistics(): Value from DB: %s", stats);
        this.lastStatisticsCache = stats;
        return stats;
    }

    public AnalyticsStatistics getAnalyticsStatistics() {
        Log.d(TAG, "getAnalyticsStatistics(): Getting analytics stats");
        AnalyticsStatistics stats = new AnalyticsStatistics();
        SQLiteDatabase db = helper.getReadableDatabase();
        // get all in one query (raw is the only possible solution)
        String query = "SELECT * FROM (SELECT COUNT(*) AS TOTAL_CELLS_COUNT FROM " + CellsTable.TABLE_NAME + ") JOIN (SELECT COUNT(*) AS TOTAL_LOCATIONS_COUNT, COUNT(DISTINCT DATE(" + MeasurementsTable.COLUMN_MEASURED_AT + " / 1000, 'unixepoch')) AS TOTAL_DAYS_COUNT FROM " + MeasurementsTable.TABLE_NAME + ")";
        // Log.d(TAG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToNext()) {
            stats.setCells(cursor.getInt(cursor.getColumnIndex("TOTAL_CELLS_COUNT")));
            stats.setLocations(cursor.getInt(cursor.getColumnIndex("TOTAL_LOCATIONS_COUNT")));
            stats.setDays(cursor.getInt(cursor.getColumnIndex("TOTAL_DAYS_COUNT")));
        }
        cursor.close();
        Log.d(TAG, "getAnalyticsStatistics(): %s", stats);
        return stats;
    }

    public Boundaries getLocationBounds() {
        Log.d(TAG, "getLocationBounds(): Getting GPS bounds");
        Boundaries boundaries = null;
        SQLiteDatabase db = helper.getReadableDatabase();
        // get all in one query (raw is the only possible solution)
        String query = "SELECT MIN(" + LocationsTable.COLUMN_LATITUDE + ") AS MIN_LAT, MIN(" + LocationsTable.COLUMN_LONGITUDE + ") AS MIN_LON, MAX(" + LocationsTable.COLUMN_LATITUDE + ") AS MAX_LAT, MAX(" + LocationsTable.COLUMN_LONGITUDE + ") AS MAX_LON FROM " + LocationsTable.TABLE_NAME;
        // Log.d(TAG, query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToNext()) {
            double minLat = cursor.getDouble(cursor.getColumnIndex("MIN_LAT"));
            double minLon = cursor.getDouble(cursor.getColumnIndex("MIN_LON"));
            double maxLat = cursor.getDouble(cursor.getColumnIndex("MAX_LAT"));
            double maxLon = cursor.getDouble(cursor.getColumnIndex("MAX_LON"));
            boundaries = new Boundaries(minLat, minLon, maxLat, maxLon);
        }
        cursor.close();
        return boundaries;
    }

    public List<Measurement> getOlderMeasurements(long maxTimestamp, int offset, int limit) {
        Log.d(TAG, "getOlderMeasurements(): Getting %s measurements with timestamp <= %s skipping first %s", limit, maxTimestamp, offset);
        return getMeasurements(MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_MEASURED_AT + " <= ?", new String[]{String.valueOf(maxTimestamp)}, null, null, MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_MEASURED_AT + " ASC, " + MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_ROW_ID + " ASC", String.valueOf(offset) + ", " + String.valueOf(limit));
    }

    private List<Measurement> getMeasurements(String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit) {
        Log.d(TAG, "getMeasurements(): Getting selected measurements");
        List<Measurement> measurementList = new ArrayList<Measurement>(128);
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MeasurementsTable.TABLE_NAME
                + " INNER JOIN " + LocationsTable.TABLE_NAME + " ON (" + MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_LOCATION_ID + " = " + LocationsTable.TABLE_NAME + "." + LocationsTable.COLUMN_ROW_ID + ")"
                + " INNER JOIN " + CellsTable.TABLE_NAME + " ON (" + MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_CELL_ID + " = " + CellsTable.TABLE_NAME + "." + CellsTable.COLUMN_ROW_ID + ")");
        //queryBuilder.appendWhere(MeasurementsQueries.TABLE_NAME + "." + COLUMN_CELL_ID + "=" + CellsQueries.TABLE_NAME + "." + COLUMN_ROW_ID);
        String[] returnedColumns = {MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_ROW_ID,
                MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_PSC,
                MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_NEIGHBORING,
                MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_TA,
                MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_ASU,
                MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_DBM,
                MeasurementsTable.TABLE_NAME + "." + MeasurementsTable.COLUMN_MEASURED_AT,
                LocationsTable.TABLE_NAME + "." + LocationsTable.COLUMN_LATITUDE,
                LocationsTable.TABLE_NAME + "." + LocationsTable.COLUMN_LONGITUDE,
                LocationsTable.TABLE_NAME + "." + LocationsTable.COLUMN_GPS_ACCURACY,
                LocationsTable.TABLE_NAME + "." + LocationsTable.COLUMN_GPS_SPEED,
                LocationsTable.TABLE_NAME + "." + LocationsTable.COLUMN_GPS_BEARING,
                LocationsTable.TABLE_NAME + "." + LocationsTable.COLUMN_GPS_ALTITUDE,
                CellsTable.TABLE_NAME + "." + CellsTable.COLUMN_CID,
                CellsTable.TABLE_NAME + "." + CellsTable.COLUMN_LAC,
                CellsTable.TABLE_NAME + "." + CellsTable.COLUMN_MNC,
                CellsTable.TABLE_NAME + "." + CellsTable.COLUMN_MCC,
                CellsTable.TABLE_NAME + "." + CellsTable.COLUMN_NET_TYPE};
        // Log.d(TAG, queryBuilder.buildQuery(returnedColumns, selection, selectionArgs, groupBy, having, sortOrder, limit));
        Cursor cursor = queryBuilder.query(db, returnedColumns, selection, selectionArgs, groupBy, having, sortOrder, limit);
        int rowIdColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_ROW_ID);
        int mccColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_MCC);
        int mncColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_MNC);
        int lacColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_LAC);
        int cidColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_CID);
        int netTypeColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_NET_TYPE);
        int pscColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_PSC);
        int neighboringColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_NEIGHBORING);
        int taColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_TA);
        int asuColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_ASU);
        int dbmColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_DBM);
        int latitudeColumnIndex = cursor.getColumnIndex(LocationsTable.COLUMN_LATITUDE);
        int longitudeColumnIndex = cursor.getColumnIndex(LocationsTable.COLUMN_LONGITUDE);
        int gpsAccuracyColumnIndex = cursor.getColumnIndex(LocationsTable.COLUMN_GPS_ACCURACY);
        int gpsSpeedColumnIndex = cursor.getColumnIndex(LocationsTable.COLUMN_GPS_SPEED);
        int gpsBearingColumnIndex = cursor.getColumnIndex(LocationsTable.COLUMN_GPS_BEARING);
        int gpsAltitudeColumnIndex = cursor.getColumnIndex(LocationsTable.COLUMN_GPS_ALTITUDE);
        int timestampColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_MEASURED_AT);
        while (cursor.moveToNext()) {
            Measurement measurement = new Measurement();
            measurement.setRowId(cursor.getInt(rowIdColumnIndex));
            measurement.setMcc(cursor.getInt(mccColumnIndex));
            measurement.setMnc(cursor.getInt(mncColumnIndex));
            measurement.setLac(cursor.getInt(lacColumnIndex));
            measurement.setCid(cursor.getInt(cidColumnIndex));
            measurement.setNetworkType(NetworkGroup.fromValue(cursor.getInt(netTypeColumnIndex)));
            measurement.setNeighboring(cursor.getInt(neighboringColumnIndex) == 1 ? true : false);
            measurement.setPsc(cursor.getInt(pscColumnIndex));
            measurement.setTa(cursor.getInt(taColumnIndex));
            measurement.setAsu(cursor.getInt(asuColumnIndex));
            measurement.setDbm(cursor.getInt(dbmColumnIndex));
            measurement.setLatitude(cursor.getDouble(latitudeColumnIndex));
            measurement.setLongitude(cursor.getDouble(longitudeColumnIndex));
            measurement.setGpsAccuracy(cursor.getFloat(gpsAccuracyColumnIndex));
            measurement.setGpsSpeed(cursor.getFloat(gpsSpeedColumnIndex));
            measurement.setGpsBearing(cursor.getFloat(gpsBearingColumnIndex));
            measurement.setGpsAltitude(cursor.getDouble(gpsAltitudeColumnIndex));
            measurement.setTimestamp(cursor.getLong(timestampColumnIndex));
            measurementList.add(measurement);
        }
        cursor.close();
        return measurementList;
    }

    public int deleteAllMeasurements() {
        Log.d(TAG, "deleteAllMeasurements(): Deleting all measurements");
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        int deletedMeasurements = 0;
        try {
            deletedMeasurements = db.delete(MeasurementsTable.TABLE_NAME, "1", null);
            int deletedLocations = db.delete(LocationsTable.TABLE_NAME, "1", null);
            int deletedCells = db.delete(CellsTable.TABLE_NAME, "1", null);
            db.setTransactionSuccessful();
            Log.d(TAG, "deleteAllMeasurements(): Deleted %s measurements, %s cells, %s locations", deletedMeasurements, deletedCells, deletedLocations);
        } finally {
            invalidateCache();
            db.endTransaction();
        }
        return deletedMeasurements;
    }

    public int deleteMeasurements(int[] rowIds) {
        if (rowIds == null || rowIds.length == 0) {
            Log.d(TAG, "deleteMeasurements(): Nothing to delete");
            return 0;
        }
        Log.d(TAG, "deleteMeasurements(): Deleting %s measurements", rowIds.length);
        // in transaction
        int deleted = 0;
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            // delete partially
            int numOfDeletions = (int) Math.ceil(1.0 * rowIds.length / NUM_OF_DELETIONS_PER_ONE_QUERY);
            for (int i = 0; i < numOfDeletions; i++) {
                // calculate range
                int lower = i * NUM_OF_DELETIONS_PER_ONE_QUERY;
                int upper = (i + 1) * NUM_OF_DELETIONS_PER_ONE_QUERY;
                // last
                if (i + 1 == numOfDeletions && upper != rowIds.length)
                    upper = rowIds.length;
                // construct query
                String[] whereArgs = new String[upper - lower];
                StringBuilder whereClauseBuilder = new StringBuilder(MeasurementsTable.COLUMN_ROW_ID + " IN (");
                for (int j = 0; j < (upper - lower); j++) {
                    if (j == 0) {
                        whereClauseBuilder.append("?");
                    }
                    else {
                        whereClauseBuilder.append(", ?");
                    }
                    whereArgs[j] = String.valueOf(rowIds[lower + j]);
                }
                whereClauseBuilder.append(")");
                // delete
                deleted += db.delete(MeasurementsTable.TABLE_NAME, whereClauseBuilder.toString(), whereArgs);
            }
            // validate total result
            if (deleted == rowIds.length) {
                // if all removed successfully then delete orphaned cells and locations
                long deletedLocations = db.delete(LocationsTable.TABLE_NAME, LocationsTable.COLUMN_ROW_ID + " NOT IN (SELECT DISTINCT " + MeasurementsTable.COLUMN_LOCATION_ID + " FROM " + MeasurementsTable.TABLE_NAME + ")", null);
                long deletedCells = db.delete(CellsTable.TABLE_NAME, CellsTable.COLUMN_ROW_ID + " NOT IN (SELECT DISTINCT " + MeasurementsTable.COLUMN_CELL_ID + " FROM " + MeasurementsTable.TABLE_NAME + ")", null);
                Log.d(TAG, "deleteMeasurements(): Deleted orphaned %s cells, %s locations", deletedCells, deletedLocations);
                db.setTransactionSuccessful();
            } else
                deleted = 0;
        } finally {
            invalidateCache();
            db.endTransaction();
        }
        return deleted;
    }
    
    private void invalidateCache() {
        lastMeasurementCache = null;
        lastStatisticsCache = null;
    }

    // ========== GET DATABASE VERSION ========== //

    public static int getDatabaseVersion(Context context) {
        int version = DATABASE_FILE_VERSION;
        SQLiteDatabase db = null;
        try {
            File path = context.getDatabasePath(DATABASE_FILE_NAME);
            // open manually to prevent database upgrade or creation
            db = SQLiteDatabase.openDatabase(path.toString(), null, SQLiteDatabase.OPEN_READONLY);
            version = db.getVersion(); // equivalent of PRAGMA user_version
            Log.d(TAG, "getDatabaseVersion(): Database file version %s", version);
        } catch (SQLiteException ex) {
            Log.e(TAG, "getDatabaseVersion(): Database file cannot be opened", ex);
        } finally {
            if (db != null)
                db.close();
        }
        return version;
    }

    // ========== FORCE DATABASE UPGRADE ========== //

    public void forceDatabaseUpgrade() {
        Log.d(TAG, "forceDatabaseUpgrade(): Forcing database upgrade");
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            // read something to prevent from being removed while optimization (I hope)
            Cursor cursor = db.rawQuery("SELECT 1", null);
            cursor.close();
            Log.d(TAG, "forceDatabaseUpgrade(): Database successfully opened for R/W");
        } catch (SQLiteException ex) {
            Log.e(TAG, "forceDatabaseUpgrade(): Failed to open for R/W", ex);
        }
    }

    // ========== GET SINGLETON INSTANCE ========== //

    public static MeasurementsDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (MeasurementsDatabase.class) {
                if (instance == null) {
                    instance = new MeasurementsDatabase(context);
                }
            }
        }
        return instance;
    }

    public static void invalidateInstance(Context context) {
        synchronized (MeasurementsDatabase.class) {
            instance = null;
        }
    }

    // ========== INNER OBJECTS ========== //

    private static class MeasurementsOpenHelper extends SQLiteOpenHelper {
        private static final String INNER_TAG = TAG + "." + MeasurementsOpenHelper.class.getSimpleName();

        MeasurementsOpenHelper(Context context) {
            super(context, DATABASE_FILE_NAME, null, DATABASE_FILE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqliteDatabase) {
            Log.d(INNER_TAG, "onCreate(): Creating db structure");
            List<ITable> tables = new ArrayList<ITable>();
            tables.add(new CellsArchiveTable());
            tables.add(new StatsTable());
            tables.add(new LocationsTable());
            tables.add(new CellsTable());
            tables.add(new MeasurementsTable());

            for (ITable table : tables) {
                String[] queries = table.getCreateQueries();
                for (String query : queries) {
                    sqliteDatabase.execSQL(query);
                }
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqliteDatabase, int oldVersion, int newVersion) {
            Log.d(INNER_TAG, "onUpgrade(): Upgrading db from version %s to %s", oldVersion, newVersion);
            DbMigrationHelper migrationHelper = new DbMigrationHelper(sqliteDatabase);
            migrationHelper.upgrade(oldVersion, newVersion);
        }
    }
}
