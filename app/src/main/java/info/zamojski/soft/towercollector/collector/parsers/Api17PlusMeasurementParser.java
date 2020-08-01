/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.parsers;

import android.location.Location;
import android.telephony.CellInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.model.Statistics;
import timber.log.Timber;

public class Api17PlusMeasurementParser extends MeasurementParser {

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
            Timber.d("parse(): Required accuracy not achieved: %s", location.getAccuracy());
            return ParseResult.AccuracyNotAchieved;
        }
        Timber.d("parse(): Required accuracy achieved: %s", location.getAccuracy());
        // get last location
        getAndSetLastLocation();
        // create measurement
        Measurement measurement = new Measurement();
        measurement.setMeasuredAt(System.currentTimeMillis());
        // fix time if incorrect
        fixMeasurementTimestamp(measurement, location);
        // remove duplicated cells
        removeDuplicatedCells(cells);
        // if the same cell check distance condition, otherwise accept
        if (lastSavedMeasurement != null && lastSavedLocation != null && !conditionsValidator.isMinDistanceSatisfied(lastSavedLocation, location, minDistance)) {
            List<String> lastMeasurementsCellKeys = new ArrayList<>();
            for (Cell lastCell : lastSavedMeasurement.getCells()) {
                lastMeasurementsCellKeys.add(cellIdentityConverter.createCellKey(lastCell));
            }
            int mainCellsChanged = 0;
            for (CellInfo cell : cells) {
                if (cell.isRegistered() && !lastMeasurementsCellKeys.contains(cellIdentityConverter.createCellKey(cell))) {
                    mainCellsChanged++;
                }
            }
            if (mainCellsChanged > 0) {
                Timber.d("parse(): Distance condition not achieved but %s main cells changed", mainCellsChanged);
            } else {
                Timber.d("parse(): Distance condition not achieved");
                return ParseResult.DistanceNotAchieved;
            }
        }
        // check if location has been obtained recently
        if (!locationValidator.isUpToDate(timestamp, System.currentTimeMillis())) {
            Timber.d("parse(): Location too old");
            return ParseResult.LocationTooOld;
        }
        Timber.d("parse(): Destination and time conditions achieved");
        // update measurement with location
        updateMeasurementWithLocation(measurement, location);
        // loop through all cells
        for (CellInfo cellInfo : cells) {
            if (!cellValidator.isValid(cellInfo)) {
                // don't try to create neighboring cells because this may be even more unreliable than on older API
                Timber.d("parse(): Cell invalid: %s", cellInfo);
                continue;
            }
            if (!collectNeighboringCells && !cellInfo.isRegistered()) {
                // skip neighboring cells
                Timber.d("parse(): Neighboring cell skipped: %s", cellInfo);
                continue;
            }
            // update with cell data
            Cell tempCell = cellIdentityConverter.convert(cellInfo);
            // update measurement with signal strength
            cellSignalConverter.update(tempCell, cellInfo);
            // write to database
            Timber.d("parse(): Cell valid: %s", cellInfo);
            measurement.addCell(tempCell);
        }
        // none of cells are valid
        if (measurement.getCells().isEmpty()) {
            Timber.d("parse(): All cells invalid or skipped");
            return ParseResult.NoNetworkSignal;
        }
        // write to database
        Timber.d("parse(): Measurement: %s", measurement);
        boolean inserted = MeasurementsDatabase.getInstance(MyApplication.getApplication()).insertMeasurement(measurement);
        if (inserted) {
            lastSavedLocation = location;
            lastSavedMeasurement = measurement;
            Timber.d("parse(): Measurement saved");
            // broadcast information to main activity
            Statistics stats = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getMeasurementsStatistics();
            EventBus.getDefault().post(new MeasurementSavedEvent(measurement, stats));
            EventBus.getDefault().post(new MeasurementsCollectedEvent(measurement));
            Timber.d("parse(): Notification updated and measurement broadcasted");
            return ParseResult.Saved;
        } else {
            return ParseResult.SaveFailed;
        }
    }

    private void removeDuplicatedCells(List<CellInfo> cells) {
        List<CellInfo> cellsToRemove = new ArrayList<CellInfo>();
        Set<String> uniqueCellKeys = new HashSet<String>();

        for (CellInfo cell : cells) {
            if (cell == null)
                continue;
            String key = cellIdentityConverter.createCellKey(cell);
            if (uniqueCellKeys.contains(key)) {
                Timber.d("removeDuplicatedCells(): Remove duplicated cell: %s", key);
                cellsToRemove.add(cell);
            } else {
                uniqueCellKeys.add(key);
            }
        }

        cells.removeAll(cellsToRemove);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(Api17PlusMeasurementProcessingEvent event) {
        ParseResult result = parse(event.getLastLocation(), event.getLastCellInfo(),
                System.currentTimeMillis(), event.getMinDistance());
        // when saved different event is published
        if (result != ParseResult.Saved) {
            notifyResult(result);
        }
    }
}
