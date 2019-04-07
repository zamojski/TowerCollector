/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.parsers;

import android.location.Location;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.collector.ParseResult;
import info.zamojski.soft.towercollector.collector.converters.CellLocationConverter;
import info.zamojski.soft.towercollector.collector.converters.CellLocationSignalConverter;
import info.zamojski.soft.towercollector.collector.validators.CellLocationValidator;
import info.zamojski.soft.towercollector.collector.validators.ConditionsValidator;
import info.zamojski.soft.towercollector.collector.validators.LocationValidator;
import info.zamojski.soft.towercollector.collector.validators.SystemTimeValidator;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.events.LegacyMeasurementProcessingEvent;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.MeasurementsCollectedEvent;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.model.Statistics;
import info.zamojski.soft.towercollector.utils.MobileUtils;
import timber.log.Timber;

public class LegacyMeasurementParser extends MeasurementParser {

    private CellLocationValidator cellLocationValidator;

    private CellLocationConverter cellLocationConverter;
    private CellLocationSignalConverter cellSignalConverter;

    public LegacyMeasurementParser(LocationValidator locationValidator, CellLocationValidator cellLocationValidator,
                                   ConditionsValidator conditionsValidator, SystemTimeValidator systemTimeValidator,
                                   CellLocationConverter cellLocationConverter, CellLocationSignalConverter cellSignalConverter,
                                   boolean collectNeighboringCells) {
        super(locationValidator, conditionsValidator, systemTimeValidator, collectNeighboringCells);
        this.cellLocationValidator = cellLocationValidator;
        this.cellLocationConverter = cellLocationConverter;
        this.cellSignalConverter = cellSignalConverter;
    }

    private ParseResult parse(Location location, CellLocation cellLocation, SignalStrength signalStrength,
                              NetworkGroup networkType, String operatorCode, String operatorName, List<NeighboringCellInfo> neighboringCells,
                              long timestamp, int minDistance) {
        // if required accuracy was achieved
        if (!locationValidator.isValid(location)) {
            Timber.d("parse(): Required accuracy not achieved: %s", location.getAccuracy());
            return ParseResult.AccuracyNotAchieved;
        }
        Timber.d("parse(): Required accuracy achieved: %s", location.getAccuracy());
        // get last location
        getAndSetLastLocation();
        // operator name may be unreliable for CDMA
        Timber.d("parse(): Operator name = '%s'", operatorName);
        // get operator codes
        int mcc = Cell.UNKNOWN_CID;
        int mnc = Cell.UNKNOWN_CID;
        if (cellLocation instanceof GsmCellLocation) {
            int[] mccMncPair = MobileUtils.getMccMncPair(operatorCode);
            if (mccMncPair != null) {
                mcc = mccMncPair[0];
                mnc = mccMncPair[1];
            } else {
                Timber.d("parseLocation(): Network operator unknown: %s", operatorCode);
            }
        }
        // validate cell
        if (!cellLocationValidator.isValid(cellLocation, mcc, mnc)) {
            Timber.d("parse(): Cell invalid");
            return ParseResult.NoNetworkSignal;
        }
        // create measurement with basic data
        Measurement measurement = new Measurement();
        measurement.setMeasuredAt(System.currentTimeMillis());
        Cell mainCell = cellLocationConverter.convert(cellLocation, mcc, mnc, networkType);
        measurement.addCell(mainCell);
        // fix time if incorrect
        fixMeasurementTimestamp(measurement, location);
        // if the same cell check distance condition, otherwise accept
        if (lastSavedMeasurement != null && lastSavedLocation != null && !conditionsValidator.isMinDistanceSatisfied(lastSavedLocation, location, minDistance)) {
            List<String> lastMeasurementsCellKeys = new ArrayList<>();
            for (Cell lastCell : lastSavedMeasurement.getCells()) {
                lastMeasurementsCellKeys.add(createCellKey(lastCell));
            }
            boolean mainCellChanged = !lastMeasurementsCellKeys.contains(createCellKey(mainCell));
            if (mainCellChanged) {
                Timber.d("parse(): Distance condition not achieved but cell changed");
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
        // update measurement with signal strength
        if (signalStrength != null) {
            cellSignalConverter.update(mainCell, signalStrength);
        }
        if (collectNeighboringCells) {
            // remove duplicated neighboring cells
            removeDuplicatedNeighbors(neighboringCells, mainCell);
            // process neighboring cells
            for (NeighboringCellInfo neighboringCell : neighboringCells) {
                // validate cell
                if (cellLocationValidator.isValid(neighboringCell, mcc, mnc)) {
                    // set values
                    Cell tempCell = cellLocationConverter.convert(neighboringCell, mcc, mnc, mainCell.getLac(), mainCell.getCid());
                    // update measurement with signal strength
                    cellSignalConverter.update(tempCell, neighboringCell.getRssi());
                    // save
                    measurement.addCell(tempCell);
                    Timber.d("parse(): Neighboring cell valid: %s", neighboringCell);
                } else {
                    Timber.d("parse(): Neighboring cell invalid: %s", neighboringCell);
                }
            }
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

    private void removeDuplicatedNeighbors(List<NeighboringCellInfo> neighboringCells, Cell mainCell) {
        List<NeighboringCellInfo> cellsToRemove = new ArrayList<NeighboringCellInfo>();
        Set<String> uniqueCellKeys = new HashSet<String>();

        uniqueCellKeys.add(createCellKey(mainCell));
        for (NeighboringCellInfo cell : neighboringCells) {
            String key = createCellKey(cell, mainCell);
            if (uniqueCellKeys.contains(key)) {
                Timber.d("removeDuplicatedNeighbors(): Remove duplicated cell: %s", key);
                cellsToRemove.add(cell);
            } else {
                uniqueCellKeys.add(key);
            }
        }

        neighboringCells.removeAll(cellsToRemove);
    }

    private String createCellKey(NeighboringCellInfo neighboringCell, Cell cell) {
        return cell.getMcc() + "_" + cell.getMnc() + "_" + neighboringCell.getLac() + "_" + neighboringCell.getCid();
    }

    private String createCellKey(Cell cell) {
        return cell.getMcc() + "_" + cell.getMnc() + "_" + cell.getLac() + "_" + cell.getCid();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(LegacyMeasurementProcessingEvent event) {
        ParseResult result = parse(event.getLastLocation(), event.getLastCellLocation(), event.getLastSignalStrength(),
                event.getLastNetworkType(), event.getLastOperatorCode(), event.getLastOperatorName(),
                event.getNeighboringCells(), event.getLastLocationObtainedTime(), event.getMinDistance());
        // when saved different event is published
        if (result != ParseResult.Saved) {
            notifyResult(result);
        }
    }
}
