/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.parsers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.acra.ACRA;

import de.greenrobot.event.EventBus;

import android.annotation.TargetApi;
import android.location.Location;
import android.os.Build;
import android.telephony.CellInfo;
import trikita.log.Log;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.collector.ParseResult;
import info.zamojski.soft.towercollector.collector.converters.CellIdentityConverter;
import info.zamojski.soft.towercollector.collector.converters.CellSignalConverter;
import info.zamojski.soft.towercollector.collector.validators.CellIdentityValidator;
import info.zamojski.soft.towercollector.collector.validators.ConditionsValidator;
import info.zamojski.soft.towercollector.collector.validators.LocationValidator;
import info.zamojski.soft.towercollector.collector.validators.SystemTimeValidator;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.events.Api17PlusMeasurementProcessingEvent;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.MeasurementsCollectedEvent;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.model.Statistics;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class Api17PlusMeasurementParser extends MeasurementParser {

    private static final String TAG = Api17PlusMeasurementParser.class.getSimpleName();

    private CellIdentityValidator cellValidator;

    private CellIdentityConverter cellIdentityConverter;
    private CellSignalConverter cellSignalConverter;

    public Api17PlusMeasurementParser(LocationValidator locationValidator, CellIdentityValidator cellValidator,
                                      ConditionsValidator conditionsValidator, SystemTimeValidator systemTimeValidator,
                                      CellIdentityConverter cellIdentityConverter, CellSignalConverter cellSignalConverter,
                                      boolean collectNeighboringCells) {
        super(locationValidator, conditionsValidator, systemTimeValidator, collectNeighboringCells);
        this.cellValidator = cellValidator;
        this.cellIdentityConverter = cellIdentityConverter;
        this.cellSignalConverter = cellSignalConverter;
    }

    private ParseResult parse(Location location, List<CellInfo> cells,
                              long timestamp, int minDistance) {
        // if required accuracy was achieved
        if (!locationValidator.isValid(location)) {
            Log.d("parse(): Required accuracy not achieved: %s", location.getAccuracy());
            return ParseResult.AccuracyNotAchieved;
        }
        Log.d("parse(): Required accuracy achieved: %s", location.getAccuracy());
        // get last location
        getAndSetLastLocation();
        // create measurement
        Measurement measurement = new Measurement();
        // fix time if incorrect
        fixMeasurementTimestamp(measurement, location);
        // if the same cell check distance condition, otherwise accept
        if (lastSavedLocation != null && !conditionsValidator.isMinDistanceSatisfied(lastSavedLocation, location, minDistance)) {
            Log.d("parse(): Distance condition not achieved");
            return ParseResult.DistanceNotAchieved;
        }
        // check if location has been obtained recently
        if (!locationValidator.isUpToDate(timestamp, System.currentTimeMillis())) {
            Log.d("parse(): Location too old");
            return ParseResult.LocationTooOld;
        }
        Log.d("parse(): Destination and time conditions achieved");
        // update measurement with location
        updateMeasurementWithLocation(measurement, location);
        // remove duplicated cells
        removeDuplicatedCells(cells);
        // create a list of measurements to save
        List<Measurement> measurementsToSave = new ArrayList<Measurement>();
        // loop through all cells
        for (CellInfo cell : cells) {
            if (!cellValidator.isValid(cell)) {
                // don't try to create neighboring cells because this may be even more unreliable than on older API
                Log.d("parse(): Cell invalid: %s", cell);
                continue;
            }
            if (!collectNeighboringCells && !cell.isRegistered()) {
                // skip neighboring cells
                Log.d("parse(): Neighboring cell skipped: %s", cell);
                continue;
            }
            // copy measurement
            Measurement measurementCopy = new Measurement(measurement);
            // update with cell data
            cellIdentityConverter.update(measurementCopy, cell);
            // update measurement with signal strength
            cellSignalConverter.update(measurementCopy, cell);
            // write to database
            Log.d("parse(): Measurement: %s", measurementCopy);
            measurementsToSave.add(measurementCopy);
        }
        // none of cells are valid
        if (measurementsToSave.isEmpty()) {
            Log.d("parse(): All cells invalid or skipped");
            return ParseResult.NoNetworkSignal;
        }
        // temporary solution to keep compatibility with old API and current views
        Measurement mainMeasurement = findFirstMainMeasurement(measurementsToSave);
        // write to database
        Log.d("parse(): Selected as main: %s", mainMeasurement);
        boolean inserted = MeasurementsDatabase.getInstance(MyApplication.getApplication()).insertMeasurements(measurementsToSave.toArray(new Measurement[measurementsToSave.size()]));
        if (inserted) {
            lastSavedLocation = location;
            lastSavedMeasurement = mainMeasurement;
            Log.d("parse(): Measurement saved");
            // broadcast information to main activity
            Statistics stats = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getMeasurementsStatistics();
            EventBus.getDefault().post(new MeasurementSavedEvent(mainMeasurement, stats));
            EventBus.getDefault().post(new MeasurementsCollectedEvent(measurementsToSave));
            Log.d("parse(): Notification updated and measurement broadcasted");
            return ParseResult.Saved;
        } else {
            Log.e("parse(): Error while saving measurement");
            Exception ex = new Exception("Measurement save failed");
            MyApplication.getAnalytics().sendException(ex, Boolean.FALSE);
            ACRA.getErrorReporter().handleSilentException(ex);
            return ParseResult.SaveFailed;
        }
    }

    private void removeDuplicatedCells(List<CellInfo> cells) {
        List<CellInfo> cellsToRemove = new ArrayList<CellInfo>();
        Set<String> uniqueCellKeys = new HashSet<String>();

        for (CellInfo cell : cells) {
            String key = cellIdentityConverter.createCellKey(cell);
            if (uniqueCellKeys.contains(key)) {
                Log.d("removeDuplicatedCells(): Remove duplicated cell: %s", key);
                cellsToRemove.add(cell);
            } else {
                uniqueCellKeys.add(key);
            }
        }

        cells.removeAll(cellsToRemove);
    }

    private Measurement findFirstMainMeasurement(List<Measurement> measurements) {
        Measurement mainMeasurement = measurements.get(0);
        for (Measurement m : measurements) {
            if (!m.isNeighboring()) {
                mainMeasurement = m;
                break;
            }
        }
        return mainMeasurement;
    }

    public void onEventBackgroundThread(Api17PlusMeasurementProcessingEvent event) {
        ParseResult result = parse(event.getLastLocation(), event.getLastCellInfo(),
                System.currentTimeMillis(), event.getMinDistance());
        // when saved different event is published
        if (result != ParseResult.Saved) {
            notifyResult(result);
        }
    }
}
