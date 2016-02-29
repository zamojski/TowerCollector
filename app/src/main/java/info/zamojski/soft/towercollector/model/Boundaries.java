/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import java.io.Serializable;

public class Boundaries implements Serializable {

    private static final long serialVersionUID = 3060626143804974908L;

    private double minLat, minLon, maxLat, maxLon;

    public Boundaries(double minLat, double minLon, double maxLat, double maxLon) {
        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMaxLon() {
        return maxLon;
    }

    @Override
    public String toString() {
        return "Boundaries [minLat=" + minLat + ", minLon=" + minLon + ", maxLat=" + maxLat + ", maxLon=" + maxLon + "]";
    }
}
