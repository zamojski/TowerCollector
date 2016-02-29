/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import java.io.Serializable;

public class Statistics implements Serializable {

    private static final long serialVersionUID = -2342507669967581530L;

    private int locationsToday;
    private int cellsToday;
    private int discoveredCellsToday;

    private int locationsLocal;
    private int cellsLocal;
    private int discoveredCellsLocal;
    private long sinceLocal;

    private int locationsGlobal;
    private int discoveredCellsGlobal;
    private long sinceGlobal;

    public int getLocationsToday() {
        return locationsToday;
    }

    public void setLocationsToday(int locationsToday) {
        this.locationsToday = locationsToday;
    }

    public int getCellsToday() {
        return cellsToday;
    }

    public void setCellsToday(int cellsToday) {
        this.cellsToday = cellsToday;
    }

    public int getDiscoveredCellsToday() {
        return discoveredCellsToday;
    }

    public void setDiscoveredCellsToday(int discoveredCellsToday) {
        this.discoveredCellsToday = discoveredCellsToday;
    }

    public int getLocationsLocal() {
        return locationsLocal;
    }

    public void setLocationsLocal(int locationsLocal) {
        this.locationsLocal = locationsLocal;
    }

    public int getCellsLocal() {
        return cellsLocal;
    }

    public void setCellsLocal(int cellsLocal) {
        this.cellsLocal = cellsLocal;
    }

    public int getDiscoveredCellsLocal() {
        return discoveredCellsLocal;
    }

    public void setDiscoveredCellsLocal(int discoveredCellsLocal) {
        this.discoveredCellsLocal = discoveredCellsLocal;
    }

    public long getSinceLocal() {
        return sinceLocal;
    }

    public void setSinceLocal(long sinceLocal) {
        this.sinceLocal = sinceLocal;
    }

    public int getLocationsGlobal() {
        return locationsGlobal;
    }

    public void setLocationsGlobal(int locationsGlobal) {
        this.locationsGlobal = locationsGlobal;
    }

    public int getDiscoveredCellsGlobal() {
        return discoveredCellsGlobal;
    }

    public void setDiscoveredCellsGlobal(int discoveredCellsGlobal) {
        this.discoveredCellsGlobal = discoveredCellsGlobal;
    }

    public long getSinceGlobal() {
        return sinceGlobal;
    }

    public void setSinceGlobal(long sinceGlobal) {
        this.sinceGlobal = sinceGlobal;
    }

    @Override
    public String toString() {
        return "Statistics [locationsToday=" + locationsToday + ", cellsToday=" + cellsToday + ", discoveredCellsToday=" + discoveredCellsToday + ", locationsLocal=" + locationsLocal + ", cellsLocal=" + cellsLocal + ", discoveredCellsLocal=" + discoveredCellsLocal + ", sinceLocal=" + sinceLocal + ", locationsGlobal=" + locationsGlobal + ", discoveredCellsGlobal=" + discoveredCellsGlobal + ", sinceGlobal=" + sinceGlobal + "]";
    }

}
