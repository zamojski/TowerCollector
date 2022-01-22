/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.parsers;

import android.location.Location;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.mroczis.netmonster.core.model.cell.ICell;
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.collector.ParseResult;
import info.zamojski.soft.towercollector.collector.converters.NetMonsterCellConverter;
import info.zamojski.soft.towercollector.collector.converters.NetMonsterSignalConverter;
import info.zamojski.soft.towercollector.collector.validators.ConditionsValidator;
import info.zamojski.soft.towercollector.collector.validators.LocationValidator;
import info.zamojski.soft.towercollector.collector.validators.NetMonsterCellValidator;
import info.zamojski.soft.towercollector.collector.validators.SystemTimeValidator;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.MeasurementsCollectedEvent;
import info.zamojski.soft.towercollector.events.NetMonsterMeasurementProcessingEvent;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.model.Statistics;
import timber.log.Timber;

public class NetMonsterMeasurementParser extends MeasurementParser {

    private NetMonsterCellValidator cellValidator;

    private NetMonsterCellConverter cellConverter;
    private NetMonsterSignalConverter signalConverter;

    public NetMonsterMeasurementParser(LocationValidator locationValidator, NetMonsterCellValidator cellValidator,
                                       ConditionsValidator conditionsValidator, SystemTimeValidator systemTimeValidator,
                                       NetMonsterCellConverter cellConverter, NetMonsterSignalConverter signalConverter,
                                       boolean collectNeighboringCells) {
        super(locationValidator, conditionsValidator, systemTimeValidator, collectNeighboringCells);
        this.cellValidator = cellValidator;
        this.cellConverter = cellConverter;
        this.signalConverter = signalConverter;
    }

    private ParseResult parse(Location location, List<ICell> cells, long timestamp, int minDistance) {
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
        // remove invalid cells
        removeInvalidCells(cells);
        // if the same cell check distance condition, otherwise accept
        if (lastSavedMeasurement != null && lastSavedLocation != null && !conditionsValidator.isMinDistanceSatisfied(lastSavedLocation, location, minDistance)) {
            List<String> lastMeasurementsCellKeys = new ArrayList<>();
            for (Cell lastCell : lastSavedMeasurement.getCells()) {
                lastMeasurementsCellKeys.add(cellConverter.createCellKey(lastCell));
            }
            int mainCellsChanged = 0;
            for (ICell cell : cells) {
                if (cell.getConnectionStatus() instanceof PrimaryConnection && !lastMeasurementsCellKeys.contains(cellConverter.createCellKey(cell))) {
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
        for (ICell cell : cells) {
            if (!collectNeighboringCells && !(cell.getConnectionStatus() instanceof PrimaryConnection)) {
                // skip neighboring cells
                Timber.d("parse(): Neighboring cell skipped: %s", cell);
                continue;
            }
            // update with cell data
            Cell tempCell = cellConverter.convert(cell);
            // update measurement with signal strength
            signalConverter.update(tempCell, cell);
            // write to database
            Timber.d("parse(): Cell valid: %s", cell);
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

    private void removeDuplicatedCells(List<ICell> cells) {
        List<ICell> cellsToRemove = new ArrayList<>();
        Set<String> uniqueCellKeys = new HashSet<>();

        for (ICell cell : cells) {
            if (cell == null)
                continue;
            String key = cellConverter.createCellKey(cell);
            if (uniqueCellKeys.contains(key)) {
                Timber.d("removeDuplicatedCells(): Remove duplicated cell: %s", key);
                cellsToRemove.add(cell);
            } else {
                uniqueCellKeys.add(key);
            }
        }

        cells.removeAll(cellsToRemove);
    }

    private void removeInvalidCells(List<ICell> cells) {
        List<ICell> cellsToRemove = new ArrayList<>();

        for (ICell cell : cells) {
            if (cell == null)
                continue;
            if (!cellValidator.isValid(cell)) {
                // don't try to create neighboring cells because this may be even more unreliable than on older API
                Timber.d("removeInvalidCells(): Cell invalid: %s", cell);
                cellsToRemove.add(cell);
            }
        }

        cells.removeAll(cellsToRemove);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(NetMonsterMeasurementProcessingEvent event) {
        ParseResult result = parse(event.getLastLocation(), event.getLastCells(),
                System.currentTimeMillis(), event.getMinDistance());
        // when saved different event is published
        if (result != ParseResult.Saved) {
            notifyResult(result);
        }
    }
}
