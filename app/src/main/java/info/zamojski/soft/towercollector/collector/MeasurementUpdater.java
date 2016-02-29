/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.events.Api17PlusMeasurementProcessingEvent;
import info.zamojski.soft.towercollector.events.LegacyMeasurementProcessingEvent;

import android.location.Location;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.SignalStrength;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class MeasurementUpdater {

    private static final String TAG = MeasurementUpdater.class.getSimpleName();

    private final List<NeighboringCellInfo> EMPTY_NEIGHBORING_CELL_LIST = new ArrayList<NeighboringCellInfo>(0);

    private Location lastLocation;
    private long lastLocationObtainedTime;

    private NetworkGroup lastNetworkType;
    private String lastOperatorCode;
    private String lastOperatorName;

    private List<CellInfo> lastCellInfo;

    private SignalStrength lastSignalStrength;
    private CellLocation lastCellLocation;
    private List<NeighboringCellInfo> neighboringCells;

    private int minDistance;

    public synchronized void setLastLocation(Location location, long locationObtainedTime) {
        Log.d(TAG, "setLastLocation(): Location updated: " + location + " obtained at " + locationObtainedTime);
        this.lastLocation = location;
        this.lastLocationObtainedTime = locationObtainedTime;
        notifyIfReadyToProcess();
    }

    public synchronized void setLastCellInfo(List<CellInfo> cellInfo) {
        Log.d(TAG, "setLastCellInfo(): Cell info updated: " + cellInfo);
        this.lastCellInfo = cellInfo;
        this.lastNetworkType = NetworkGroup.Unknown;
        this.lastOperatorName = null;
        notifyIfReadyToProcess();
    }

    public synchronized void setLastCellLocation(CellLocation cellLocation, NetworkGroup networkType,
                                                 String operatorCode, String operatorName, List<NeighboringCellInfo> neighboringCells) {
        Log.d(TAG, "setLastCellLocation(): Cell location updated: " + cellLocation + ", network type: " + networkType + ", operator code: " + operatorCode + ", operator name: " + operatorName);
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
        Log.d(TAG, "setLastSignalStrength(): Signal strength updated: " + signalStrength);
        this.lastSignalStrength = signalStrength;
    }

    public synchronized void setMinDistanceAndInterval(int minDistance, int minInterval) {
        Log.d(TAG, "setMinDistanceAndInterval(): Min distance: " + minDistance + ", interval: " + minInterval + " updated");
        this.minDistance = minDistance;
    }

    private void notifyIfReadyToProcess() {
        if (isApi17PlusCompleted()) {
            Log.d(TAG, "notifyIfReadyToProcess(): Api17Plus collected");
            Api17PlusMeasurementProcessingEvent event = new Api17PlusMeasurementProcessingEvent(lastLocation, lastCellInfo, minDistance);
            EventBus.getDefault().post(event);
        } else if (isLegacyCompleted()) {
            Log.d(TAG, "notifyIfReadyToProcess(): Legacy collected");
            LegacyMeasurementProcessingEvent event = new LegacyMeasurementProcessingEvent(lastLocation, lastLocationObtainedTime,
                    lastCellLocation, lastSignalStrength, lastNetworkType, lastOperatorCode, lastOperatorName,
                    neighboringCells, minDistance);
            EventBus.getDefault().post(event);
        }
    }

    private boolean isLegacyCompleted() {
        return (lastLocation != null && lastCellLocation != null);
    }

    private boolean isApi17PlusCompleted() {
        return (lastLocation != null && lastCellInfo != null && lastCellInfo.size() > 0);
    }

    private boolean isCellLocationEqual(CellLocation cl1, CellLocation cl2) {
        boolean result;
        if (cl1 instanceof GsmCellLocation && cl2 instanceof GsmCellLocation) {
            GsmCellLocation gsm1 = (GsmCellLocation) cl1;
            GsmCellLocation gsm2 = (GsmCellLocation) cl2;
            result = (gsm1.getCid() == gsm2.getCid()
                    && gsm1.getLac() == gsm2.getLac()
                    && gsm1.getPsc() == gsm2.getPsc());
            Log.d(TAG, "isCellLocationEqual(): GSM equals = " + result);
        } else if (cl1 instanceof CdmaCellLocation && cl2 instanceof CdmaCellLocation) {
            CdmaCellLocation cdma1 = (CdmaCellLocation) cl1;
            CdmaCellLocation cdma2 = (CdmaCellLocation) cl2;
            result = (cdma1.getBaseStationId() == cdma2.getBaseStationId()
                    && cdma1.getNetworkId() == cdma2.getNetworkId()
                    && cdma1.getSystemId() == cdma2.getSystemId());
            Log.d(TAG, "isCellLocationEqual(): CDMA equal = " + result);
        } else {
            // different types or nulls
            result = false;
            Log.d(TAG, "isCellLocationEqual(): Different types or nulls");
        }
        return result;
    }
}
