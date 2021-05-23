/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.dao.migration.DbMigrationHelper;
import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;
import info.zamojski.soft.towercollector.model.Boundaries;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.MapCell;
import info.zamojski.soft.towercollector.model.MapMeasurement;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.model.Statistics;
import info.zamojski.soft.towercollector.utils.DateUtils;
import info.zamojski.soft.towercollector.utils.HashUtils;
import timber.log.Timber;

public class MeasurementsDatabase {

    public static final String DATABASE_FILE_NAME = "measurements.db";
    public static final int DATABASE_FILE_VERSION = 17;

    private static final int NUM_OF_DELETIONS_PER_ONE_QUERY = 50;

    private final MeasurementsOpenHelper helper;

    private static volatile MeasurementsDatabase instance = null;

    private boolean insertionFailureReported = false;

    private Measurement lastMeasurementCache;
    private Statistics lastStatisticsCache;

    private MeasurementsDatabase(Context context) {
        helper = new MeasurementsOpenHelper(context);
    }

    public boolean insertMeasurement(Measurement measurement) {
        Timber.d("insertMeasurement(): Inserting %s measurement", measurement);
        boolean result = true;
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();
            StringBuilder resultSb = new StringBuilder();
            // calculate hashcode
            String locationHashCode = HashUtils.toSha1(measurement);
            // insert measurement
            {
                ContentValues values = new ContentValues();
                values.put(MeasurementsTable.COLUMN_LOCATION_HASHCODE, locationHashCode);
                values.put(MeasurementsTable.COLUMN_LATITUDE, measurement.getLatitude());
                values.put(MeasurementsTable.COLUMN_LONGITUDE, measurement.getLongitude());
                values.put(MeasurementsTable.COLUMN_GPS_ACCURACY, measurement.getGpsAccuracy());
                values.put(MeasurementsTable.COLUMN_GPS_SPEED, measurement.getGpsSpeed());
                values.put(MeasurementsTable.COLUMN_GPS_BEARING, measurement.getGpsBearing());
                values.put(MeasurementsTable.COLUMN_GPS_ALTITUDE, measurement.getGpsAltitude());
                values.put(MeasurementsTable.COLUMN_MEASURED_AT, measurement.getMeasuredAt());
                long rowId = db.insert(MeasurementsTable.TABLE_NAME, null, values);
                boolean localResult = (rowId != -1);
                Timber.d("insertMeasurement(): Measurement inserted = %s", localResult);
                resultSb.append("\tmeasurement inserted=").append(localResult);
            }
            // don't use value returned by insert, because it sometimes returns wrong value -> query always
            int measurementId = -1;
            {
                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                queryBuilder.setTables(MeasurementsTable.TABLE_NAME);
                String[] columns = new String[]{MeasurementsTable.COLUMN_ROW_ID};
                String selection = MeasurementsTable.COLUMN_LOCATION_HASHCODE + " = ? AND " + MeasurementsTable.COLUMN_MEASURED_AT + " = ?";
                String[] selectionArgs = new String[]{locationHashCode, String.valueOf(measurement.getMeasuredAt())};
                Cursor cursorTotal = queryBuilder.query(db, columns, selection, selectionArgs, null, null, null);
                boolean localResult = false;
                if (cursorTotal.moveToNext()) {
                    measurementId = cursorTotal.getInt(cursorTotal.getColumnIndex(MeasurementsTable.COLUMN_ROW_ID));
                    measurement.setMeasurementId(measurementId);
                    localResult = true;
                }
                cursorTotal.close();
                result = localResult;
                Timber.d("insertMeasurement(): Measurement found = %s", localResult);
                resultSb.append("\tmeasurement found=").append(localResult);
            }
            for (Cell cell : measurement.getCells()) {
                // insert cell
                {
                    ContentValues values = new ContentValues();
                    values.put(CellsTable.COLUMN_MCC, cell.getMcc());
                    values.put(CellsTable.COLUMN_MNC, cell.getMnc());
                    values.put(CellsTable.COLUMN_LAC, cell.getLac());
                    values.put(CellsTable.COLUMN_CID, cell.getCid());
                    values.put(CellsTable.COLUMN_NET_TYPE, cell.getNetworkType().ordinal());
                    values.put(CellsTable.COLUMN_DISCOVERED_AT, measurement.getMeasuredAt());
                    long rowId = db.insert(CellsTable.TABLE_NAME, null, values);
                    boolean localResult = (rowId != -1);
                    Timber.d("insertMeasurement(): Cell inserted = %s", localResult);
                    resultSb.append("\tcell inserted=").append(localResult);
                }
                // don't use value returned by insert, because it sometimes returns wrong value -> query always
                int cellId = -1;
                {
                    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                    queryBuilder.setTables(CellsTable.TABLE_NAME);
                    String[] columns = new String[]{CellsTable.COLUMN_ROW_ID};
                    String selection = CellsTable.COLUMN_CID + " = ? AND " + CellsTable.COLUMN_LAC + " = ? AND " + CellsTable.COLUMN_MNC + " = ? AND " + CellsTable.COLUMN_MCC + " = ? AND " + CellsTable.COLUMN_NET_TYPE + " = ?";
                    String[] selectionArgs = new String[]{String.valueOf(cell.getCid()), String.valueOf(cell.getLac()), String.valueOf(cell.getMnc()), String.valueOf(cell.getMcc()), String.valueOf(cell.getNetworkType().ordinal())};
                    Cursor cursorTotal = queryBuilder.query(db, columns, selection, selectionArgs, null, null, null);
                    boolean localResult = false;
                    if (cursorTotal.moveToNext()) {
                        cellId = cursorTotal.getInt(cursorTotal.getColumnIndex(CellsTable.COLUMN_ROW_ID));
                        cell.setCellId(cellId);
                        localResult = true;
                    }
                    cursorTotal.close();
                    result &= localResult;
                    Timber.d("insertMeasurement(): Cell found = %s", localResult);
                    resultSb.append("\tcell found=").append(localResult);
                }
                // insert cell signal (if previous queries returned correct result)
                if (result) {
                    ContentValues values = new ContentValues();
                    values.put(CellSignalsTable.COLUMN_CELL_ID, cellId);
                    values.put(CellSignalsTable.COLUMN_MEASUREMENT_ID, measurementId);
                    values.put(CellSignalsTable.COLUMN_PSC, cell.getPsc());
                    values.put(CellSignalsTable.COLUMN_NEIGHBORING, cell.isNeighboring());
                    values.put(CellSignalsTable.COLUMN_TA, cell.getTa());
                    values.put(CellSignalsTable.COLUMN_ASU, cell.getAsu());
                    values.put(CellSignalsTable.COLUMN_DBM, cell.getDbm());
                    values.put(CellSignalsTable.COLUMN_RSRP, cell.getRsrp());
                    values.put(CellSignalsTable.COLUMN_RSRQ, cell.getRsrq());
                    values.put(CellSignalsTable.COLUMN_RSSI, cell.getRssi());
                    values.put(CellSignalsTable.COLUMN_RSSNR, cell.getRssnr());
                    values.put(CellSignalsTable.COLUMN_CQI, cell.getCqi());
                    values.put(CellSignalsTable.COLUMN_RSCP, cell.getRscp());
                    values.put(CellSignalsTable.COLUMN_CSI_RSRP, cell.getCsiRsrp());
                    values.put(CellSignalsTable.COLUMN_CSI_RSRQ, cell.getCsiRsrq());
                    values.put(CellSignalsTable.COLUMN_CSI_SINR, cell.getCsiSinr());
                    values.put(CellSignalsTable.COLUMN_SS_RSRP, cell.getSsRsrp());
                    values.put(CellSignalsTable.COLUMN_SS_RSRQ, cell.getSsRsrq());
                    values.put(CellSignalsTable.COLUMN_SS_SINR, cell.getSsSinr());
                    values.put(CellSignalsTable.COLUMN_CDMA_DBM, cell.getCdmaDbm());
                    values.put(CellSignalsTable.COLUMN_CDMA_ECIO, cell.getCdmaEcio());
                    values.put(CellSignalsTable.COLUMN_EVDO_DBM, cell.getEvdoDbm());
                    values.put(CellSignalsTable.COLUMN_EVDO_ECIO, cell.getEvdoEcio());
                    values.put(CellSignalsTable.COLUMN_EVDO_SNR, cell.getEvdoSnr());
                    values.put(CellSignalsTable.COLUMN_EC_NO, cell.getEcNo());
                    values.put(CellSignalsTable.COLUMN_ARFCN, cell.getArfcn());
                    long rowId = db.insert(CellSignalsTable.TABLE_NAME, null, values);
                    boolean localResult = false;
                    if (rowId != -1) {
                        cell.setCellSignalId((int) rowId); // acceptable because we don't use the value anyway
                        localResult = true;
                    }
                    result = localResult;
                    Timber.d("insertMeasurement(): Cell signal inserted = %s", localResult);
                    resultSb.append("\tcell signal inserted=").append(localResult);
                }
                resultSb.append(";\r\n");
            }
            // commit
            if (result) {
                db.setTransactionSuccessful();
                Timber.d("insertMeasurement(): Measurement inserted successfully");
                Timber.d("insertMeasurement(): Insertion report: %s", resultSb.toString());
            } else {
                Timber.d("insertMeasurement(): Measurement not inserted");
                Timber.d("insertMeasurement(): Insertion report: %s", resultSb.toString());
                // report exception because it shouldn't occur (one time per app run)
                if (!insertionFailureReported) {
                    Throwable ex = new MeasurementInsertionFailedException("Measurements not inserted", resultSb.toString());
                    MyApplication.handleSilentException(ex);
                    insertionFailureReported = true;
                }
            }
        } catch (Exception ex) {
            result = false;
            Timber.e(ex, "insertMeasurement(): Error while saving measurement");
            MyApplication.handleSilentException(ex);
        } finally {
            invalidateCache();
            db.endTransaction();
        }
        return result;
    }

    public Measurement getFirstMeasurement() {
        Measurement firstMeasurement = null;
        List<Measurement> measurements = getMeasurements(CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_MEASUREMENT_ID + " = (SELECT tm." + MeasurementsTable.COLUMN_ROW_ID + " FROM " + NotUploadedMeasurementsView.VIEW_NAME + " tm ORDER BY tm." + MeasurementsTable.COLUMN_MEASURED_AT + " ASC, tm." + MeasurementsTable.COLUMN_ROW_ID + " ASC LIMIT 0,1)",
                null,
                null, null,
                NotUploadedMeasurementsView.VIEW_NAME + "." + MeasurementsTable.COLUMN_MEASURED_AT + " ASC, " // from view because not for upload
                        + CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_NEIGHBORING + " ASC, "
                        + CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_ROW_ID + " ASC",
                null, false);
        if (!measurements.isEmpty()) {
            firstMeasurement = measurements.get(0);
        }
        Timber.d("getFirstMeasurement(): %s", firstMeasurement);
        return firstMeasurement;
    }

    public Measurement getLastMeasurement() {
        // Try to get from cache then read from DB (copy to local to avoid null if invalidated in the meantime)
        Measurement lastMeasurementCacheCopy = this.lastMeasurementCache;
        if (lastMeasurementCacheCopy != null) {
            Timber.d("getLastMeasurement(): Value from cache: %s", lastMeasurementCacheCopy);
            return lastMeasurementCacheCopy;
        }
        Measurement lastMeasurement = null;
        List<Measurement> measurements = getMeasurements(CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_MEASUREMENT_ID + " = (SELECT tm." + MeasurementsTable.COLUMN_ROW_ID + " FROM " + NotUploadedMeasurementsView.VIEW_NAME + " tm ORDER BY tm." + MeasurementsTable.COLUMN_MEASURED_AT + " DESC, tm." + MeasurementsTable.COLUMN_ROW_ID + " DESC LIMIT 0,1)",
                null,
                null, null,
                NotUploadedMeasurementsView.VIEW_NAME + "." + MeasurementsTable.COLUMN_MEASURED_AT + " DESC, " // from view because not for upload
                        + CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_NEIGHBORING + " ASC, "
                        + CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_ROW_ID + " DESC",
                null, false);
        if (!measurements.isEmpty()) {
            lastMeasurement = measurements.get(0);
        }
        Timber.d("getLastMeasurement(): Value from DB: %s", lastMeasurement);
        this.lastMeasurementCache = lastMeasurement;
        return lastMeasurement;
    }

    public int getAllLocationsCount(boolean includePartiallyUploaded) {
        Timber.d("getAllLocationsCount(): Getting number of locations, including partially uploaded = %s", includePartiallyUploaded);
        int count = 0;
        final String measurementsCount = "MEASUREMENTS_COUNT";
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(includePartiallyUploaded ? MeasurementsTable.TABLE_NAME : NotUploadedMeasurementsView.VIEW_NAME);
        String[] columns = new String[]{"COUNT(*) AS " + measurementsCount};
        Cursor cursorTotal = queryBuilder.query(db, columns, null, null, null, null, null);
        if (cursorTotal.moveToNext()) {
            count = cursorTotal.getInt(cursorTotal.getColumnIndex(measurementsCount));
        }
        cursorTotal.close();
        return count;
    }

    public Statistics getMeasurementsStatistics() {
        // Try to get from cache then read from DB (copy to local to avoid null if invalidated in the meantime)
        Statistics lastStatisticsCacheCopy = this.lastStatisticsCache;
        if (lastStatisticsCacheCopy != null) {
            Timber.d("getMeasurementsStatistics(): Value from cache: %s", lastStatisticsCacheCopy);
            return lastStatisticsCacheCopy;
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
        final String todayMeasurementsCount = "TODAY_MEASUREMENTS_COUNT";
        final String todayDiscoveredCellsCount = "TODAY_DISCOVERED_CELLS_COUNT";
        final String localCellsCount = "LOCAL_CELLS_COUNT";
        final String localMeasurementsCount = "LOCAL_MEASUREMENTS_COUNT";
        final String localDiscoveredCellsCount = "LOCAL_DISCOVERED_CELLS_COUNT";
        final String localSince = "LOCAL_SINCE";
        final String globalMeasurementsCount = "GLOBAL_MEASUREMENTS_COUNT";
        final String globalDiscoveredCellsCount = "GLOBAL_DISCOVERED_CELLS_COUNT";
        final String globalSince = "GLOBAL_SINCE";
        final String uploadToOcid = "UPLOAD_TO_OCID";
        final String uploadToMls = "UPLOAD_TO_MLS";
        String todayTime = String.valueOf(todayCalendar.getTimeInMillis());
        String[] selectionArgs = new String[]{todayTime, todayTime};
        // get all in one query (raw is the only possible solution)
        // full queries
        String globalStatsQuery = "SELECT " + StatsTable.COLUMN_TOTAL_MEASUREMENTS + " AS " + globalMeasurementsCount + ", " + StatsTable.COLUMN_TOTAL_DISCOVERED_CELLS + " AS " + globalDiscoveredCellsCount + ", " + StatsTable.COLUMN_TOTAL_SINCE + " AS " + globalSince + " FROM " + StatsTable.TABLE_NAME;
        String localMeasurementsAndCellsQuery = "SELECT COUNT(" + MeasurementsTable.COLUMN_ROW_ID + ") AS " + localMeasurementsCount + ", COUNT(DISTINCT " + CellSignalsTable.COLUMN_CELL_ID + ") AS " + localCellsCount + " FROM " + CellSignalsTable.TABLE_NAME + " WHERE " + CellSignalsTable.COLUMN_MEASUREMENT_ID + " IN (SELECT DISTINCT " + MeasurementsTable.COLUMN_ROW_ID + " FROM " + NotUploadedMeasurementsView.VIEW_NAME + ")";
        String localDiscoveredCellsQuery = "SELECT COUNT(" + CellsTable.COLUMN_ROW_ID + ") AS " + localDiscoveredCellsCount + " FROM " + CellsTable.TABLE_NAME + " WHERE " + CellsTable.COLUMN_DISCOVERED_AT + " >= (SELECT MIN(" + MeasurementsTable.COLUMN_MEASURED_AT + ") FROM " + NotUploadedMeasurementsView.VIEW_NAME + ")";
        String localSinceQuery = "SELECT MIN(" + MeasurementsTable.COLUMN_MEASURED_AT + ") AS " + localSince + " FROM " + NotUploadedMeasurementsView.VIEW_NAME;
        String todayMeasurementsAndCellsQuery = "SELECT COUNT(" + MeasurementsTable.COLUMN_ROW_ID + ") AS " + todayMeasurementsCount + ", COUNT(DISTINCT " + CellSignalsTable.COLUMN_CELL_ID + ") AS " + todayCellsCount + " FROM " + CellSignalsTable.TABLE_NAME + " WHERE " + CellSignalsTable.COLUMN_MEASUREMENT_ID + " IN (SELECT DISTINCT " + MeasurementsTable.COLUMN_ROW_ID + " FROM " + NotUploadedMeasurementsView.VIEW_NAME + " WHERE " + MeasurementsTable.COLUMN_MEASURED_AT + " > ?)";
        String todayDiscoveredCellsQuery = "SELECT COUNT(" + CellsTable.COLUMN_ROW_ID + ") AS " + todayDiscoveredCellsCount + " FROM " + CellsTable.TABLE_NAME + " WHERE " + CellsTable.COLUMN_DISCOVERED_AT + " >= (SELECT MIN(" + MeasurementsTable.COLUMN_MEASURED_AT + ") FROM " + NotUploadedMeasurementsView.VIEW_NAME + " WHERE " + MeasurementsTable.COLUMN_MEASURED_AT + " > ?)";
        String uploadToOcidAndMlsQuery = "SELECT SUM(CASE WHEN m." + MeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT + " IS NULL THEN 1 ELSE 0 END) AS " + uploadToOcid + ", "
                + "SUM(CASE WHEN m." + MeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT + " IS NULL THEN 1 ELSE 0 END) AS " + uploadToMls
                + " FROM " + CellSignalsTable.TABLE_NAME + " cs INNER JOIN " + MeasurementsTable.TABLE_NAME + " m ON cs." + CellSignalsTable.COLUMN_MEASUREMENT_ID + " = m." + MeasurementsTable.COLUMN_ROW_ID;

        String query = "SELECT * FROM ((" + globalStatsQuery + ") "
                + "JOIN (" + localMeasurementsAndCellsQuery + ") "
                + "JOIN (" + localDiscoveredCellsQuery + ") "
                + "JOIN (" + localSinceQuery + ") "
                + "JOIN (" + todayMeasurementsAndCellsQuery + ") "
                + "JOIN (" + todayDiscoveredCellsQuery + ") "
                + "JOIN (" + uploadToOcidAndMlsQuery + "))";
        // Timber.d(query);
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor.moveToNext()) {
            stats.setCellsToday(cursor.getInt(cursor.getColumnIndex(todayCellsCount)));
            stats.setLocationsToday(cursor.getInt(cursor.getColumnIndex(todayMeasurementsCount)));
            stats.setDiscoveredCellsToday(cursor.getInt(cursor.getColumnIndex(todayDiscoveredCellsCount)));
            stats.setCellsLocal(cursor.getInt(cursor.getColumnIndex(localCellsCount)));
            stats.setLocationsLocal(cursor.getInt(cursor.getColumnIndex(localMeasurementsCount)));
            stats.setDiscoveredCellsLocal(cursor.getInt(cursor.getColumnIndex(localDiscoveredCellsCount)));
            stats.setSinceLocal(cursor.getLong(cursor.getColumnIndex(localSince)));
            stats.setLocationsGlobal(cursor.getInt(cursor.getColumnIndex(globalMeasurementsCount)));
            stats.setDiscoveredCellsGlobal(cursor.getInt(cursor.getColumnIndex(globalDiscoveredCellsCount)));
            stats.setSinceGlobal(cursor.getLong(cursor.getColumnIndex(globalSince)));
            stats.setToUploadOcid(cursor.getInt(cursor.getColumnIndex(uploadToOcid)));
            stats.setToUploadMls(cursor.getInt(cursor.getColumnIndex(uploadToMls)));
        }
        cursor.close();
        Timber.d("getMeasurementsStatistics(): Value from DB: %s", stats);
        this.lastStatisticsCache = stats;
        return stats;
    }

    public AnalyticsStatistics getAnalyticsStatistics() {
        Timber.d("getAnalyticsStatistics(): Getting analytics stats");
        AnalyticsStatistics stats = new AnalyticsStatistics();
        SQLiteDatabase db = helper.getReadableDatabase();
        // get all in one query (raw is the only possible solution)
        String query = "SELECT * FROM (SELECT COUNT(" + CellSignalsTable.COLUMN_CELL_ID + ") AS TOTAL_CELLS_COUNT FROM " + CellSignalsTable.TABLE_NAME + ") JOIN (SELECT COUNT(*) AS TOTAL_LOCATIONS_COUNT, MIN(" + MeasurementsTable.COLUMN_MEASURED_AT + ") AS TOTAL_DAYS_MIN, MAX(" + MeasurementsTable.COLUMN_MEASURED_AT + ") AS TOTAL_DAYS_MAX FROM " + NotUploadedMeasurementsView.VIEW_NAME + ")";
        // Log.d(query);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToNext()) {
            stats.setCells(cursor.getInt(cursor.getColumnIndex("TOTAL_CELLS_COUNT")));
            stats.setLocations(cursor.getInt(cursor.getColumnIndex("TOTAL_LOCATIONS_COUNT")));
            long minDate = cursor.getLong(cursor.getColumnIndex("TOTAL_DAYS_MIN"));
            long maxDate = cursor.getLong(cursor.getColumnIndex("TOTAL_DAYS_MAX"));
            int diff = (int) DateUtils.getTimeDiff(maxDate, minDate); // safe because return value is in days
            stats.setDays(diff);
        }
        cursor.close();
        Timber.d("getAnalyticsStatistics(): %s", stats);
        return stats;
    }

    public Boundaries getLocationBounds() {
        Timber.d("getLocationBounds(): Getting GPS bounds");
        Boundaries boundaries = null;
        SQLiteDatabase db = helper.getReadableDatabase();
        // get all in one query (raw is the only possible solution)
        String query = "SELECT MIN(" + MeasurementsTable.COLUMN_LATITUDE + ") AS MIN_LAT, MIN(" + MeasurementsTable.COLUMN_LONGITUDE + ") AS MIN_LON, MAX(" + MeasurementsTable.COLUMN_LATITUDE + ") AS MAX_LAT, MAX(" + MeasurementsTable.COLUMN_LONGITUDE + ") AS MAX_LON"
                + " FROM " + NotUploadedMeasurementsView.VIEW_NAME;
        // Log.d(query);
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

    public List<Measurement> getMeasurementsPartIncludingPartiallyUploaded(int offset, int limit) {
        Timber.d("getMeasurementsPartIncludingPartiallyUploaded(): Getting %s measurements skipping first %s", limit, offset);
        return getMeasurements(CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_MEASUREMENT_ID + " IN(SELECT tm." + MeasurementsTable.COLUMN_ROW_ID + " FROM " + MeasurementsTable.TABLE_NAME + " tm ORDER BY tm." + MeasurementsTable.COLUMN_MEASURED_AT + " ASC, tm." + MeasurementsTable.COLUMN_ROW_ID + " ASC LIMIT " + String.valueOf(offset) + ", " + String.valueOf(limit) + ")",
                null, null, null, null, null, true);
    }

    public List<Measurement> getMeasurementsPart(int offset, int limit) {
        Timber.d("getMeasurementsPart(): Getting %s measurements skipping first %s", limit, offset);
        return getMeasurements(CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_MEASUREMENT_ID + " IN(SELECT tm." + MeasurementsTable.COLUMN_ROW_ID + " FROM " + NotUploadedMeasurementsView.VIEW_NAME + " tm ORDER BY tm." + MeasurementsTable.COLUMN_MEASURED_AT + " ASC, tm." + MeasurementsTable.COLUMN_ROW_ID + " ASC LIMIT " + String.valueOf(offset) + ", " + String.valueOf(limit) + ")",
                null, null, null, null, null, false);
    }

    private List<Measurement> getMeasurements(String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit, boolean includePartiallyUploaded) {
        Timber.d("getMeasurements(): Getting selected measurements");
        final String MEASUREMENT_ROW_ID = "measurement_" + MeasurementsTable.COLUMN_ROW_ID;
        final String CELL_ROW_ID = "cell_" + CellsTable.COLUMN_ROW_ID;
        final String CELL_SIGNAL_ROW_ID = "cell_signal_" + CellSignalsTable.COLUMN_ROW_ID;
        final String MEASUREMENTS_TABLE = includePartiallyUploaded ? MeasurementsTable.TABLE_NAME : NotUploadedMeasurementsView.VIEW_NAME;
        Map<Integer, Measurement> tempMeasurements = new HashMap<>();
        List<Measurement> measurementList = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MEASUREMENTS_TABLE
                + " INNER JOIN " + CellSignalsTable.TABLE_NAME + " ON (" + MEASUREMENTS_TABLE + "." + MeasurementsTable.COLUMN_ROW_ID + " = " + CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_MEASUREMENT_ID + ")"
                + " INNER JOIN " + CellsTable.TABLE_NAME + " ON (" + CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_CELL_ID + " = " + CellsTable.TABLE_NAME + "." + CellsTable.COLUMN_ROW_ID + ")");
        String[] returnedColumns = {
                MEASUREMENTS_TABLE + "." + MeasurementsTable.COLUMN_ROW_ID + " AS " + MEASUREMENT_ROW_ID,
                CellsTable.TABLE_NAME + "." + CellsTable.COLUMN_ROW_ID + " AS " + CELL_ROW_ID,
                CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_ROW_ID + " AS " + CELL_SIGNAL_ROW_ID,
                CellSignalsTable.COLUMN_PSC,
                CellSignalsTable.COLUMN_NEIGHBORING,
                CellSignalsTable.COLUMN_TA,
                CellSignalsTable.COLUMN_ASU,
                CellSignalsTable.COLUMN_DBM,
                CellSignalsTable.COLUMN_RSRP,
                CellSignalsTable.COLUMN_RSRQ,
                CellSignalsTable.COLUMN_RSSI,
                CellSignalsTable.COLUMN_RSSNR,
                CellSignalsTable.COLUMN_CQI,
                CellSignalsTable.COLUMN_RSCP,
                CellSignalsTable.COLUMN_CSI_RSRP,
                CellSignalsTable.COLUMN_CSI_RSRQ,
                CellSignalsTable.COLUMN_CSI_SINR,
                CellSignalsTable.COLUMN_SS_RSRP,
                CellSignalsTable.COLUMN_SS_RSRQ,
                CellSignalsTable.COLUMN_SS_SINR,
                CellSignalsTable.COLUMN_CDMA_DBM,
                CellSignalsTable.COLUMN_CDMA_ECIO,
                CellSignalsTable.COLUMN_EVDO_DBM,
                CellSignalsTable.COLUMN_EVDO_ECIO,
                CellSignalsTable.COLUMN_EVDO_SNR,
                CellSignalsTable.COLUMN_EC_NO,
                CellSignalsTable.COLUMN_ARFCN,
                MeasurementsTable.COLUMN_MEASURED_AT,
                MeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT,
                MeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT,
                MeasurementsTable.COLUMN_LATITUDE,
                MeasurementsTable.COLUMN_LONGITUDE,
                MeasurementsTable.COLUMN_GPS_ACCURACY,
                MeasurementsTable.COLUMN_GPS_SPEED,
                MeasurementsTable.COLUMN_GPS_BEARING,
                MeasurementsTable.COLUMN_GPS_ALTITUDE,
                CellsTable.COLUMN_CID,
                CellsTable.COLUMN_LAC,
                CellsTable.COLUMN_MNC,
                CellsTable.COLUMN_MCC,
                CellsTable.COLUMN_NET_TYPE
        };
        // Timber.d(queryBuilder.buildQuery(returnedColumns, selection, selectionArgs, groupBy, having, sortOrder, limit));
        Cursor cursor = queryBuilder.query(db, returnedColumns, selection, selectionArgs, groupBy, having, sortOrder, limit);
        int measurementIdColumnIndex = cursor.getColumnIndex(MEASUREMENT_ROW_ID);
        int cellIdColumnIndex = cursor.getColumnIndex(CELL_ROW_ID);
        int cellSignalIdColumnIndex = cursor.getColumnIndex(CELL_SIGNAL_ROW_ID);
        int mccColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_MCC);
        int mncColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_MNC);
        int lacColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_LAC);
        int cidColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_CID);
        int netTypeColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_NET_TYPE);
        int pscColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_PSC);
        int neighboringColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_NEIGHBORING);
        int taColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_TA);
        int asuColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_ASU);
        int dbmColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_DBM);
        int rsrpColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_RSRP);
        int rsrqColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_RSRQ);
        int rssiColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_RSSI);
        int rssnrColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_RSSNR);
        int cqiColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_CQI);
        int rscpColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_RSCP);
        int csiRsrpColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_CSI_RSRP);
        int csiRsrqColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_CSI_RSRQ);
        int csiSinrColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_CSI_SINR);
        int ssRsrpColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_SS_RSRP);
        int ssRsrqColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_SS_RSRQ);
        int ssSinrColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_SS_SINR);
        int cdmaDbmColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_CDMA_DBM);
        int cdmaEcioColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_CDMA_ECIO);
        int evdoDbmColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_EVDO_DBM);
        int evdoEcioColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_EVDO_ECIO);
        int evdoSnrColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_EVDO_SNR);
        int ecNoColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_EC_NO);
        int arfcnColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_ARFCN);
        int latitudeColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_LATITUDE);
        int longitudeColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_LONGITUDE);
        int gpsAccuracyColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_GPS_ACCURACY);
        int gpsSpeedColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_GPS_SPEED);
        int gpsBearingColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_GPS_BEARING);
        int gpsAltitudeColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_GPS_ALTITUDE);
        int timestampColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_MEASURED_AT);
        int uploadedToOcidAtColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT);
        int uploadedToMlsAtColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT);
        while (cursor.moveToNext()) {
            Measurement measurement;
            int measurementId = cursor.getInt(measurementIdColumnIndex);
            if (tempMeasurements.containsKey(measurementId)) {
                measurement = tempMeasurements.get(measurementId);
            } else {
                measurement = new Measurement();
                measurement.setMeasurementId(measurementId);
                measurement.setLatitude(cursor.getDouble(latitudeColumnIndex));
                measurement.setLongitude(cursor.getDouble(longitudeColumnIndex));
                measurement.setGpsAccuracy(cursor.getFloat(gpsAccuracyColumnIndex));
                measurement.setGpsSpeed(cursor.getFloat(gpsSpeedColumnIndex));
                measurement.setGpsBearing(cursor.getFloat(gpsBearingColumnIndex));
                measurement.setGpsAltitude(cursor.getDouble(gpsAltitudeColumnIndex));
                measurement.setMeasuredAt(cursor.getLong(timestampColumnIndex));
                if (!cursor.isNull(uploadedToOcidAtColumnIndex))
                    measurement.setUploadedToOcidAt(cursor.getLong(uploadedToOcidAtColumnIndex));
                if (!cursor.isNull(uploadedToMlsAtColumnIndex))
                    measurement.setUploadedToMlsAt(cursor.getLong(uploadedToMlsAtColumnIndex));

                tempMeasurements.put(measurementId, measurement);
                measurementList.add(measurement);
            }
            Cell cell = new Cell();
            cell.setCellId(cursor.getInt(cellIdColumnIndex));
            cell.setCellSignalId(cursor.getInt(cellSignalIdColumnIndex));
            cell.setMcc(cursor.getInt(mccColumnIndex));
            cell.setMnc(cursor.getInt(mncColumnIndex));
            cell.setLac(cursor.getInt(lacColumnIndex));
            cell.setCid(cursor.getLong(cidColumnIndex));
            cell.setNetworkType(NetworkGroup.fromValue(cursor.getInt(netTypeColumnIndex)));
            cell.setNeighboring(cursor.getInt(neighboringColumnIndex) == 1);
            cell.setPsc(cursor.getInt(pscColumnIndex));
            cell.setTa(cursor.getInt(taColumnIndex));
            cell.setAsu(cursor.getInt(asuColumnIndex));
            cell.setDbm(cursor.getInt(dbmColumnIndex));
            cell.setRsrp(cursor.getInt(rsrpColumnIndex));
            cell.setRsrq(cursor.getInt(rsrqColumnIndex));
            cell.setRssi(cursor.getInt(rssiColumnIndex));
            cell.setRssnr(cursor.getInt(rssnrColumnIndex));
            cell.setCqi(cursor.getInt(cqiColumnIndex));
            cell.setRscp(cursor.getInt(rscpColumnIndex));
            cell.setCsiRsrp(cursor.getInt(csiRsrpColumnIndex));
            cell.setCsiRsrq(cursor.getInt(csiRsrqColumnIndex));
            cell.setCsiSinr(cursor.getInt(csiSinrColumnIndex));
            cell.setSsRsrp(cursor.getInt(ssRsrpColumnIndex));
            cell.setSsRsrq(cursor.getInt(ssRsrqColumnIndex));
            cell.setSsSinr(cursor.getInt(ssSinrColumnIndex));
            cell.setCdmaDbm(cursor.getInt(cdmaDbmColumnIndex));
            cell.setCdmaEcio(cursor.getInt(cdmaEcioColumnIndex));
            cell.setEvdoDbm(cursor.getInt(evdoDbmColumnIndex));
            cell.setEvdoEcio(cursor.getInt(evdoEcioColumnIndex));
            cell.setEvdoSnr(cursor.getInt(evdoSnrColumnIndex));
            cell.setEcNo(cursor.getInt(ecNoColumnIndex));
            cell.setArfcn(cursor.getInt(arfcnColumnIndex));
            measurement.addCell(cell);
        }
        cursor.close();
        return measurementList;
    }

    public List<MapMeasurement> getMeasurementsInArea(Boundaries boundaries) {
        Timber.d("getMeasurementsInArea(): Getting measurements from area lat<%s, %s>, lon<%s, %s>", boundaries.getMinLat(), boundaries.getMaxLat(), boundaries.getMinLon(), boundaries.getMaxLon());
        final String MEASUREMENT_ROW_ID = "measurement_" + MeasurementsTable.COLUMN_ROW_ID;
        Map<Integer, MapMeasurement> tempMeasurements = new HashMap<>();
        List<MapMeasurement> measurementList = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(NotUploadedMeasurementsView.VIEW_NAME
                + " INNER JOIN " + CellSignalsTable.TABLE_NAME + " ON (" + NotUploadedMeasurementsView.VIEW_NAME + "." + MeasurementsTable.COLUMN_ROW_ID + " = " + CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_MEASUREMENT_ID + ")"
                + " INNER JOIN " + CellsTable.TABLE_NAME + " ON (" + CellSignalsTable.TABLE_NAME + "." + CellSignalsTable.COLUMN_CELL_ID + " = " + CellsTable.TABLE_NAME + "." + CellsTable.COLUMN_ROW_ID + ")");
        String[] returnedColumns = {
                NotUploadedMeasurementsView.VIEW_NAME + "." + MeasurementsTable.COLUMN_ROW_ID + " AS " + MEASUREMENT_ROW_ID,
                CellSignalsTable.COLUMN_NEIGHBORING,
                MeasurementsTable.COLUMN_LATITUDE,
                MeasurementsTable.COLUMN_LONGITUDE,
                MeasurementsTable.COLUMN_MEASURED_AT,
                CellsTable.COLUMN_CID,
                CellsTable.COLUMN_LAC,
                CellsTable.COLUMN_MNC,
                CellsTable.COLUMN_MCC,
                CellsTable.COLUMN_NET_TYPE
        };
        // latitude / latitude can pass north or south pole / date line and between would fail
        String selection = MeasurementsTable.COLUMN_LATITUDE + " > ?"
                + " AND " + MeasurementsTable.COLUMN_LATITUDE + " < ?"
                + " AND " + MeasurementsTable.COLUMN_LONGITUDE + " > ?"
                + " AND " + MeasurementsTable.COLUMN_LONGITUDE + " < ?";
        String[] selectionArgs = new String[]{
                String.valueOf(boundaries.getMinLat()), String.valueOf(boundaries.getMaxLat()), String.valueOf(boundaries.getMinLon()), String.valueOf(boundaries.getMaxLon())
        };
        Cursor cursor = queryBuilder.query(db, returnedColumns, selection, selectionArgs, null, null, null, null);
        int measurementIdColumnIndex = cursor.getColumnIndex(MEASUREMENT_ROW_ID);
        int mccColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_MCC);
        int mncColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_MNC);
        int lacColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_LAC);
        int cidColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_CID);
        int netTypeColumnIndex = cursor.getColumnIndex(CellsTable.COLUMN_NET_TYPE);
        int neighboringColumnIndex = cursor.getColumnIndex(CellSignalsTable.COLUMN_NEIGHBORING);
        int latitudeColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_LATITUDE);
        int longitudeColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_LONGITUDE);
        int measuredAtColumnIndex = cursor.getColumnIndex(MeasurementsTable.COLUMN_MEASURED_AT);
        while (cursor.moveToNext()) {
            MapMeasurement measurement;
            int measurementId = cursor.getInt(measurementIdColumnIndex);
            if (tempMeasurements.containsKey(measurementId)) {
                measurement = tempMeasurements.get(measurementId);
            } else {
                measurement = new MapMeasurement();
                measurement.setLatitude(cursor.getDouble(latitudeColumnIndex));
                measurement.setLongitude(cursor.getDouble(longitudeColumnIndex));
                measurement.setMeasuredAt(cursor.getLong(measuredAtColumnIndex));

                tempMeasurements.put(measurementId, measurement);
                measurementList.add(measurement);
            }
            MapCell cell = new MapCell();
            cell.setMcc(cursor.getInt(mccColumnIndex));
            cell.setMnc(cursor.getInt(mncColumnIndex));
            cell.setLac(cursor.getInt(lacColumnIndex));
            cell.setCid(cursor.getLong(cidColumnIndex));
            cell.setNetworkType(NetworkGroup.fromValue(cursor.getInt(netTypeColumnIndex)));
            cell.setNeighboring(cursor.getInt(neighboringColumnIndex) == 1);
            measurement.addCell(cell);
        }
        cursor.close();
        return measurementList;
    }

    public int deleteAllMeasurements() {
        Timber.d("deleteAllMeasurements(): Deleting all measurements");
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        int deletedCellSignals = 0;
        try {
            deletedCellSignals = db.delete(CellSignalsTable.TABLE_NAME, "1", null);
            int deletedMeasurements = db.delete(MeasurementsTable.TABLE_NAME, "1", null);
            db.setTransactionSuccessful();
            Timber.d("deleteAllMeasurements(): Deleted %s cell signals, %s measurements", deletedCellSignals, deletedMeasurements);
        } finally {
            invalidateCache();
            db.endTransaction();
        }
        return deletedCellSignals;
    }

    public int markAsUploaded(int[] measurementIds, Long uploadedToOcidAt, Long uploadedToMlsAt) {
        if (measurementIds == null || measurementIds.length == 0) {
            Timber.d("markAsUploaded(): Nothing to mark");
            return 0;
        }
        Timber.d("markAsUploaded(): Marking %s measurements as uploaded to OCID = %s, MLS = %s", measurementIds.length, uploadedToOcidAt, uploadedToMlsAt);
        // in transaction
        int updated = 0;
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            // move to temporary uploaded
            int numOfMoves = (int) Math.ceil(1.0 * measurementIds.length / NUM_OF_DELETIONS_PER_ONE_QUERY);
            for (int i = 0; i < numOfMoves; i++) {
                // calculate range
                int lower = i * NUM_OF_DELETIONS_PER_ONE_QUERY;
                int upper = (i + 1) * NUM_OF_DELETIONS_PER_ONE_QUERY;
                // last
                if (i + 1 == numOfMoves && upper != measurementIds.length)
                    upper = measurementIds.length;
                // construct query
                String[] whereArgs = new String[upper - lower];
                StringBuilder whereClauseBuilder = new StringBuilder(MeasurementsTable.COLUMN_ROW_ID + " IN (");
                for (int j = 0; j < (upper - lower); j++) {
                    if (j == 0) {
                        whereClauseBuilder.append("?");
                    } else {
                        whereClauseBuilder.append(", ?");
                    }
                    whereArgs[j] = String.valueOf(measurementIds[lower + j]);
                }
                whereClauseBuilder.append(")");

                ContentValues cv = new ContentValues();
                if (uploadedToOcidAt != null)
                    cv.put(MeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT, uploadedToOcidAt);
                if (uploadedToMlsAt != null)
                    cv.put(MeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT, uploadedToMlsAt);
                // mark measurements
                updated += db.update(MeasurementsTable.TABLE_NAME, cv, whereClauseBuilder.toString(), whereArgs);
            }
            db.setTransactionSuccessful();
            Timber.d("markAsUploaded(): Marked successfully");
        } finally {
            invalidateCache();
            db.endTransaction();
        }
        return updated;
    }

    public int clearOlderUploadedPartiallyAndUploadedFully() {
        Timber.d("clearOlderUploadedPartiallyAndUploadedFully(): Executing in transaction");
        // in transaction
        int deletedCellSignals = 0;
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            // without transaction because SQLite doesn't support transaction nesting
            final long days = 30;
            final long dayInMillis = 24 * 60 * 60 * 1000;
            long minTimeToKeep = System.currentTimeMillis() - days * dayInMillis;
            Timber.d("clearOlderUploadedPartiallyAndUploadedFully(): Deleting uploaded measurements older than %s", minTimeToKeep);
            // delete cell signals
            deletedCellSignals = db.delete(CellSignalsTable.TABLE_NAME, CellSignalsTable.COLUMN_MEASUREMENT_ID +
                            " IN (SELECT DISTINCT " + MeasurementsTable.COLUMN_ROW_ID + " FROM " + MeasurementsTable.TABLE_NAME + " WHERE " +
                            MeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT + " < ? OR " + MeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT + " < ?" +
                            " OR (" + MeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT + " IS NOT NULL AND " + MeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT + " IS NOT NULL))",
                    new String[]{String.valueOf(minTimeToKeep), String.valueOf(minTimeToKeep)});
            // delete measurements
            int deletedMeasurements = db.delete(MeasurementsTable.TABLE_NAME, MeasurementsTable.COLUMN_ROW_ID + " NOT IN (SELECT DISTINCT " + CellSignalsTable.COLUMN_MEASUREMENT_ID + " FROM " + CellSignalsTable.TABLE_NAME + ")", null);
            Timber.d("clearOlderUploadedPartiallyAndUploadedFully(): Deleted %s cell signals, %s orphaned measurements of uploaded data", deletedCellSignals, deletedMeasurements);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return deletedCellSignals;
    }

    public int clearAllData() {
        Timber.d("clearAllData(): Clearing all data");
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        int deletedCellSignals = 0;
        try {
            deletedCellSignals = db.delete(CellSignalsTable.TABLE_NAME, "1", null);
            int deletedMeasurements = db.delete(MeasurementsTable.TABLE_NAME, "1", null);
            int deletedCells = db.delete(CellsTable.TABLE_NAME, "1", null);
            ContentValues cv = new ContentValues();
            cv.put(StatsTable.COLUMN_TOTAL_MEASUREMENTS, 0);
            cv.put(StatsTable.COLUMN_TOTAL_DISCOVERED_CELLS, 0);
            cv.put(StatsTable.COLUMN_TOTAL_SINCE, System.currentTimeMillis());
            int cleanedStats = db.update(StatsTable.TABLE_NAME, cv, null, null);
            db.setTransactionSuccessful();
            Timber.d("clearAllData(): Deleted %s cell signals, %s measurements, %s cells, cleaned %s stats", deletedCellSignals, deletedMeasurements, deletedCells, cleanedStats);
        } finally {
            invalidateCache();
            db.endTransaction();
        }
        return deletedCellSignals;
    }

    public String quickDump() {
        StringBuilder sb = new StringBuilder(10000);
        SQLiteDatabase db = helper.getReadableDatabase();
        {
            sb.append("--CELLS TABLE ")
                    .append(CellsTable.COLUMN_ROW_ID)
                    .append(",").append(CellsTable.COLUMN_DISCOVERED_AT)
                    .append("\r\n");
            String query = "SELECT "
                    + CellsTable.COLUMN_ROW_ID
                    + "," + CellsTable.COLUMN_DISCOVERED_AT
                    + " FROM " + CellsTable.TABLE_NAME;
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                int rowId = cursor.getInt(0);
                long discoveredAt = cursor.getLong(1);
                sb.append(rowId)
                        .append(",").append(discoveredAt)
                        .append("\r\n");
            }
            cursor.close();
        }
        {
            sb.append("--MEASUREMENTS TABLE ")
                    .append(MeasurementsTable.COLUMN_ROW_ID)
                    .append(",").append(MeasurementsTable.COLUMN_MEASURED_AT)
                    .append(",").append(MeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT)
                    .append(",").append(MeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT)
                    .append("\r\n");
            String query = "SELECT "
                    + MeasurementsTable.COLUMN_ROW_ID
                    + "," + MeasurementsTable.COLUMN_MEASURED_AT
                    + "," + MeasurementsTable.COLUMN_UPLOADED_TO_OCID_AT
                    + "," + MeasurementsTable.COLUMN_UPLOADED_TO_MLS_AT
                    + " FROM " + MeasurementsTable.TABLE_NAME;
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                int rowId = cursor.getInt(0);
                long measuredAt = cursor.getLong(1);
                long uploadedToOcidAt = cursor.getLong(2);
                long uploadedToMlsAt = cursor.getLong(3);
                sb.append(rowId)
                        .append(",").append(measuredAt)
                        .append(",").append(uploadedToOcidAt)
                        .append(",").append(uploadedToMlsAt)
                        .append("\r\n");
            }
            cursor.close();
        }
        {
            sb.append("--CELL_SIGNALS TABLE ")
                    .append(CellSignalsTable.COLUMN_ROW_ID)
                    .append(",").append(CellSignalsTable.COLUMN_CELL_ID)
                    .append(",").append(CellSignalsTable.COLUMN_MEASUREMENT_ID)
                    .append(",").append(CellSignalsTable.COLUMN_NEIGHBORING)
                    .append("\r\n");
            String query = "SELECT "
                    + CellSignalsTable.COLUMN_ROW_ID
                    + "," + CellSignalsTable.COLUMN_CELL_ID
                    + "," + CellSignalsTable.COLUMN_MEASUREMENT_ID
                    + "," + CellSignalsTable.COLUMN_NEIGHBORING
                    + " FROM " + CellSignalsTable.TABLE_NAME;
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                int rowId = cursor.getInt(0);
                int cellId = cursor.getInt(1);
                int measurementId = cursor.getInt(2);
                int isNeighboring = cursor.getInt(3);
                sb.append(rowId)
                        .append(",").append(cellId)
                        .append(",").append(measurementId)
                        .append(",").append(isNeighboring)
                        .append("\r\n");
            }
            cursor.close();
        }
        {
            sb.append("--STATS TABLE ")
                    .append(StatsTable.COLUMN_ROW_ID)
                    .append(",").append(StatsTable.COLUMN_TOTAL_MEASUREMENTS)
                    .append(",").append(StatsTable.COLUMN_TOTAL_DISCOVERED_CELLS)
                    .append(",").append(StatsTable.COLUMN_TOTAL_SINCE)
                    .append("\r\n");
            String query = "SELECT "
                    + StatsTable.COLUMN_ROW_ID
                    + "," + StatsTable.COLUMN_TOTAL_MEASUREMENTS
                    + "," + StatsTable.COLUMN_TOTAL_DISCOVERED_CELLS
                    + "," + StatsTable.COLUMN_TOTAL_SINCE
                    + " FROM " + StatsTable.TABLE_NAME;
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                int rowId = cursor.getInt(0);
                int totalMeasurements = cursor.getInt(1);
                int discoveredCells = cursor.getInt(2);
                long totalSince = cursor.getLong(3);
                sb.append(rowId)
                        .append(",").append(totalMeasurements)
                        .append(",").append(discoveredCells)
                        .append(",").append(totalSince)
                        .append("\r\n");
            }
            cursor.close();
        }

        return sb.toString();
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
            if (path.exists()) {
                // open manually to prevent database upgrade or creation
                db = SQLiteDatabase.openDatabase(path.toString(), null, SQLiteDatabase.OPEN_READONLY);
                version = db.getVersion(); // equivalent of PRAGMA user_version
                Timber.d("getDatabaseVersion(): Database file version %s", version);
            } else {
                Timber.d("getDatabaseVersion(): Database file missing");
            }
        } catch (SQLiteException ex) {
            Timber.e(ex, "getDatabaseVersion(): Database file cannot be opened");
        } finally {
            if (db != null)
                db.close();
        }
        return version;
    }

    // ========== FORCE DATABASE UPGRADE ========== //

    public void forceDatabaseUpgrade() {
        Timber.d("forceDatabaseUpgrade(): Forcing database upgrade");
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            // read something to prevent from being removed while optimization (I hope)
            Cursor cursor = db.rawQuery("SELECT 1", null);
            cursor.close();
            db.close();
            Timber.d("forceDatabaseUpgrade(): Database successfully opened for R/W");
        } catch (SQLiteException ex) {
            Timber.e(ex, "forceDatabaseUpgrade(): Failed to open for R/W");
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

    public static void invalidateInstance() {
        synchronized (MeasurementsDatabase.class) {
            instance = null;
        }
    }

    public static void deleteDatabase(Context context) {
        Timber.d("Deleting corrupted database");
        synchronized (MeasurementsDatabase.class) {
            invalidateInstance();
            boolean deleted = context.deleteDatabase(DATABASE_FILE_NAME);
            Timber.w("deleteDatabase(): Corrupted database deleted = %s", deleted);
        }
    }

    public boolean hasValidSchema() {
        Timber.d("hasValidSchema(): Checking if database has valid schema");
        SQLiteDatabase db = helper.getReadableDatabase();
        int count = -1;
        // check only tables as most of the reported bugs don't have them
        String query = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name IN ('" + MeasurementsTable.TABLE_NAME + "'" +
                ", '" + CellsTable.TABLE_NAME + "', '" + CellSignalsTable.TABLE_NAME + "', '" + StatsTable.TABLE_NAME + "')";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count == 4; // number of tables
    }

    // ========== INNER OBJECTS ========== //

    private static class MeasurementsOpenHelper extends SQLiteOpenHelper {
        private static final String INNER_TAG = MeasurementsDatabase.class.getSimpleName() + "." + MeasurementsOpenHelper.class.getSimpleName();

        private Context context;

        MeasurementsOpenHelper(Context context) {
            super(context, DATABASE_FILE_NAME, null, DATABASE_FILE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase sqliteDatabase) {
            Timber.tag(INNER_TAG).d("onCreate(): Creating db structure");
            List<ITable> tables = new ArrayList<ITable>();
            tables.add(new StatsTable());
            tables.add(new MeasurementsTable());
            tables.add(new CellsTable());
            tables.add(new CellSignalsTable());
            tables.add(new NotUploadedMeasurementsView());

            try {
                createSchema(sqliteDatabase, tables);
            } catch (SQLiteException ex) {
                boolean dbDeleted = context.deleteDatabase(MeasurementsDatabase.DATABASE_FILE_NAME);
                Timber.tag(INNER_TAG).e("Failed to create schema, database deleted = " + dbDeleted + ", retrying");
                createSchema(sqliteDatabase, tables);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqliteDatabase, int oldVersion, int newVersion) {
            Timber.tag(INNER_TAG).i("onUpgrade(): Upgrading db from version %s to %s", oldVersion, newVersion);
            DbMigrationHelper migrationHelper = new DbMigrationHelper(sqliteDatabase);
            migrationHelper.upgrade(oldVersion, newVersion);
        }

        private void createSchema(SQLiteDatabase sqliteDatabase, List<ITable> tables) {
            for (ITable table : tables) {
                String[] queries = table.getCreateQueries();
                for (String query : queries) {
                    sqliteDatabase.execSQL(query);
                }
            }
        }
    }
}
