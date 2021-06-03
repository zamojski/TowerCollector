/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import android.content.Context;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;

public class MapMeasurement extends MeasurementBase implements Serializable {

    private static final long serialVersionUID = -1561704184666574202L;
    /**
     * Associated cells.
     */
    private List<MapCell> cells = new ArrayList<>();

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

    public String getDescription(Context context) {
        return super.getDescription(context, getMainCells(), "<br/>");
    }

    public static MapMeasurement fromMeasurement(Measurement m) {
        MapMeasurement mm = new MapMeasurement();
        mm.setLatitude(m.getLatitude());
        mm.setLongitude(m.getLongitude());
        mm.setMeasuredAt(m.getMeasuredAt());
        List<MapCell> cc = mm.getCells();
        for (Cell c : m.getCells()) {
            cc.add(MapCell.fromCell(c));
        }
        return mm;
    }

    @NotNull
    @Override
    public String toString() {
        return "MapMeasurement{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", measuredAt=" + measuredAt +
                ", cells=[" + TextUtils.join(", ", cells) + "]" +
                '}';
    }
}
