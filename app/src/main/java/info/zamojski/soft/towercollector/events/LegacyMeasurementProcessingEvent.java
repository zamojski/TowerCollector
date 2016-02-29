/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.events;

import java.util.List;

import info.zamojski.soft.towercollector.enums.NetworkGroup;

import android.location.Location;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.SignalStrength;

public class LegacyMeasurementProcessingEvent {

    private Location lastLocation;
    private long lastLocationObtainedTime;

    private NetworkGroup lastNetworkType;
    private String lastOperatorCode;
    private String lastOperatorName;

    private SignalStrength lastSignalStrength;
    private CellLocation lastCellLocation;
    private List<NeighboringCellInfo> neighboringCells;

    private int minDistance;

    public LegacyMeasurementProcessingEvent(Location lastLocation, long lastLocationObtainedTime,
                                            CellLocation lastCellLocation, SignalStrength lastSignalStrength, NetworkGroup lastNetworkType,
                                            String lastOperatorCode, String lastOperatorName, List<NeighboringCellInfo> neighboringCells,
                                            int minDistance) {
        this.lastLocation = lastLocation;
        this.lastLocationObtainedTime = lastLocationObtainedTime;
        this.lastCellLocation = lastCellLocation;
        this.lastSignalStrength = lastSignalStrength;
        this.lastNetworkType = lastNetworkType;
        this.neighboringCells = neighboringCells;
        this.lastOperatorCode = lastOperatorCode;
        this.lastOperatorName = lastOperatorName;
        this.minDistance = minDistance;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public long getLastLocationObtainedTime() {
        return lastLocationObtainedTime;
    }

    public SignalStrength getLastSignalStrength() {
        return lastSignalStrength;
    }

    public CellLocation getLastCellLocation() {
        return lastCellLocation;
    }

    public List<NeighboringCellInfo> getNeighboringCells() {
        return neighboringCells;
    }

    public NetworkGroup getLastNetworkType() {
        return lastNetworkType;
    }

    public String getLastOperatorCode() {
        return lastOperatorCode;
    }

    public String getLastOperatorName() {
        return lastOperatorName;
    }

    public int getMinDistance() {
        return minDistance;
    }
}
