/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector;

import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;

import cz.mroczis.netmonster.core.model.cell.ICell;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.events.Api17PlusMeasurementProcessingEvent;
import info.zamojski.soft.towercollector.events.LegacyMeasurementProcessingEvent;
import info.zamojski.soft.towercollector.events.NetMonsterMeasurementProcessingEvent;
import timber.log.Timber;

import android.location.Location;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.SignalStrength;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public class MeasurementUpdater {


    private final List<NeighboringCellInfo> EMPTY_NEIGHBORING_CELL_LIST = new ArrayList<NeighboringCellInfo>(0);

    private Location lastLocation;
    private long lastLocationObtainedTime;

    private NetworkGroup lastNetworkType;
    private String lastOperatorCode;
    private String lastOperatorName;

    private List<CellInfo> lastCellInfo;

    private List<ICell> netMonsterCells;

    private SignalStrength lastSignalStrength;
    private CellLocation lastCellLocation;
    private List<NeighboringCellInfo> neighboringCells;

    private int minDistance;
    private long lastEventTime;

    public synchronized void setLastLocation(Location location, long locationObtainedTime) {
        Timber.d("setLastLocation(): Location updated: %s obtained at %s", location, locationObtainedTime);
        this.lastLocation = location;
        this.lastLocationObtainedTime = locationObtainedTime;
        notifyIfReadyToProcess();
    }

    public synchronized void setLastCellInfo(List<CellInfo> cellInfo) {
        Timber.d("setLastCellInfo(): Cell info updated: %s ", cellInfo);
        this.lastCellInfo = cellInfo;
        notifyIfReadyToProcess();
    }

    public void setLastNetMonsterCell(List<ICell> cells) {
        Timber.d("setLastNetMonsterCell(): Cell updated: %s ", cells);
        this.netMonsterCells = cells;
        notifyIfReadyToProcess();
    }

    public synchronized void setLastCellLocation(CellLocation cellLocation, NetworkGroup networkType,
                                                 String operatorCode, String operatorName, List<NeighboringCellInfo> neighboringCells) {
        Timber.d("setLastCellLocation(): Cell location updated: %s, network type: %s, operator code: %s, operator name: %s", cellLocation, networkType, operatorCode, operatorName);
        // check if any changes
        boolean cellChanged = (!isCellLocationEqual(lastCellLocation, cellLocation)
                || lastNetworkType != networkType
                || !lastOperatorCode.equals(operatorCode)
                || !lastOperatorName.equals(operatorName));
        // update last cell
        this.lastCellLocation = cellLocation;
        this.lastNetworkType = networkType;
        this.lastOperatorCode = operatorCode;
        this.lastOperatorName = operatorName;
        this.neighboringCells = neighboringCells;
        if (this.neighboringCells == null) {
            this.neighboringCells = EMPTY_NEIGHBORING_CELL_LIST;
        }
        if (cellChanged) {
            notifyIfReadyToProcess();
        }
    }

    public synchronized void setLastSignalStrength(SignalStrength signalStrength) {
        Timber.d("setLastSignalStrength(): Signal strength updated: %s", signalStrength);
        this.lastSignalStrength = signalStrength;
    }

    public synchronized void setMinDistanceAndInterval(int minDistance, int minInterval) {
        Timber.d("setMinDistanceAndInterval(): Min distance: %s, interval: %s updated", minDistance, minInterval);
        this.minDistance = minDistance;
    }

    private void notifyIfReadyToProcess() {
        // check frequency throttling
        int throttlingSeconds = MyApplication.getPreferencesProvider().getCollectorFrequencyThrottling();
        long currentTime = System.currentTimeMillis();
        
        if (throttlingSeconds > 0 && lastEventTime > 0) {
            long timeSinceLastEvent = (currentTime - lastEventTime) / 1000; // convert to seconds
            if (timeSinceLastEvent < throttlingSeconds) {
                Timber.d("notifyIfReadyToProcess(): Throttled - last event was %d seconds ago, throttling is %d seconds", timeSinceLastEvent, throttlingSeconds);
                return;
            }
        }
        
        if (isNetMonsterCompleted()) {
            Timber.d("notifyIfReadyToProcess(): NetMonster collected");
            NetMonsterMeasurementProcessingEvent event = new NetMonsterMeasurementProcessingEvent(lastLocation, new ArrayList<>(netMonsterCells), minDistance);
            EventBus.getDefault().post(event);
            lastEventTime = currentTime;
        } else if (isApi17PlusCompleted()) {
            Timber.d("notifyIfReadyToProcess(): Api17Plus collected");
            Api17PlusMeasurementProcessingEvent event = new Api17PlusMeasurementProcessingEvent(lastLocation, new ArrayList<>(lastCellInfo), minDistance);
            EventBus.getDefault().post(event);
            lastEventTime = currentTime;
        } else if (isLegacyCompleted()) {
            Timber.d("notifyIfReadyToProcess(): Legacy collected");
            LegacyMeasurementProcessingEvent event = new LegacyMeasurementProcessingEvent(lastLocation, lastLocationObtainedTime,
                    lastCellLocation, lastSignalStrength, lastNetworkType, lastOperatorCode, lastOperatorName,
                    new ArrayList<>(neighboringCells), minDistance);
            EventBus.getDefault().post(event);
            lastEventTime = currentTime;
        }
    }

    private boolean isLegacyCompleted() {
        return (lastLocation != null && lastCellLocation != null);
    }

    private boolean isApi17PlusCompleted() {
        return (lastLocation != null && lastCellInfo != null && !lastCellInfo.isEmpty());
    }

    private boolean isNetMonsterCompleted() {
        return (lastLocation != null && netMonsterCells != null && !netMonsterCells.isEmpty());
    }

    private boolean isCellLocationEqual(CellLocation cl1, CellLocation cl2) {
        boolean result;
        if (cl1 instanceof GsmCellLocation && cl2 instanceof GsmCellLocation) {
            GsmCellLocation gsm1 = (GsmCellLocation) cl1;
            GsmCellLocation gsm2 = (GsmCellLocation) cl2;
            result = (gsm1.getCid() == gsm2.getCid()
                    && gsm1.getLac() == gsm2.getLac()
                    && gsm1.getPsc() == gsm2.getPsc());
            Timber.d("isCellLocationEqual(): GSM equals = %s", result);
        } else if (cl1 instanceof CdmaCellLocation && cl2 instanceof CdmaCellLocation) {
            CdmaCellLocation cdma1 = (CdmaCellLocation) cl1;
            CdmaCellLocation cdma2 = (CdmaCellLocation) cl2;
            result = (cdma1.getBaseStationId() == cdma2.getBaseStationId()
                    && cdma1.getNetworkId() == cdma2.getNetworkId()
                    && cdma1.getSystemId() == cdma2.getSystemId());
            Timber.d("isCellLocationEqual(): CDMA equal = %s", result);
        } else {
            // different types or nulls
            result = false;
            Timber.d("isCellLocationEqual(): Different types or nulls");
        }
        return result;
    }
}
