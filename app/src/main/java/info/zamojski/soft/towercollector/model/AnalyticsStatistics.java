/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import java.io.Serializable;

public class AnalyticsStatistics implements Serializable {

    private static final long serialVersionUID = -7557862466436056665L;
    private int days;
    private int locations;
    private int cells;

    public AnalyticsStatistics() {
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getLocations() {
        return locations;
    }

    public void setLocations(int locations) {
        this.locations = locations;
    }

    public int getCells() {
        return cells;
    }

    public void setCells(int cells) {
        this.cells = cells;
    }

    @Override
    public String toString() {
        return "AnalyticsStatistics [days=" + days + ", locations=" + locations + ", cells=" + cells + "]";
    }
}
