/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MapMeasurement implements Serializable {

    private static final long serialVersionUID = -1561704184666574202L;

     /**
     * Geographic Latitude.
     */
    private double latitude;
    /**
     * Geographic Longitude.
     */
    private double longitude;
    /**
     * Associated cells.
     */
    private List<MapCell> cells = new ArrayList<>();

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<MapCell> getCells() {
        return cells;
    }

    public void addCell(MapCell cell) {
        this.cells.add(cell);
    }

    public List<MapCell> getMainCells() {
        List<MapCell> mainCells = new ArrayList<>();
        for (MapCell cell : cells) {
            if (!cell.isNeighboring())
                mainCells.add(cell);
        }
        if (mainCells.isEmpty())
            mainCells.add(cells.get(0)); // should never happen
        return mainCells;
    }

    @Override
    public String toString() {
        return "MapMeasurement{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", cells=[" + TextUtils.join(", ", cells) + "]" +
                '}';
    }
}
