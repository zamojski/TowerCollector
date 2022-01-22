/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.events;

import android.location.Location;

import java.util.List;

import cz.mroczis.netmonster.core.model.cell.ICell;

public class NetMonsterMeasurementProcessingEvent {

    private Location lastLocation;

    private List<ICell> lastCells;

    private int minDistance;

    public NetMonsterMeasurementProcessingEvent(Location lastLocation, List<ICell> lastCells, int minDistance) {
        this.lastLocation = lastLocation;
        this.lastCells = lastCells;
        this.minDistance = minDistance;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public List<ICell> getLastCells() {
        return lastCells;
    }

    public int getMinDistance() {
        return minDistance;
    }
}
