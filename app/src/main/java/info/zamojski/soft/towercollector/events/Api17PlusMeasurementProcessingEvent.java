/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.events;

import java.util.List;

import android.location.Location;
import android.telephony.CellInfo;

public class Api17PlusMeasurementProcessingEvent {

    private Location lastLocation;

    private List<CellInfo> lastCellInfo;

    private int minDistance;

    public Api17PlusMeasurementProcessingEvent(Location lastLocation, List<CellInfo> lastCellInfo, int minDistance) {
        this.lastLocation = lastLocation;
        this.lastCellInfo = lastCellInfo;
        this.minDistance = minDistance;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public List<CellInfo> getLastCellInfo() {
        return lastCellInfo;
    }

    public int getMinDistance() {
        return minDistance;
    }
}
