/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.enums;

public enum MeansOfTransport {

    Fixed(50, 40, 1000, 1000),
    Universal(50, 40, 600, 10000);

    private final int distance;
    private final float accuracy;
    private final int minTime;
    private final int maxTime;

    private MeansOfTransport(int distance, int accuracy, int minTime, int maxTime) {
        this.distance = distance;
        this.accuracy = accuracy;
        this.minTime = minTime;
        this.maxTime = maxTime;
    }

    public int getDistance() {
        return this.distance;
    }

    public float getAccuracy() {
        return this.accuracy;
    }

    public int getMinTime() {
        return this.minTime;
    }

    public int getMaxTime() {
        return this.maxTime;
    }

    public float getMinSpeed() {
        return (this.distance * 1000f / this.maxTime);
    }

    public float getMaxSpeed() {
        return (this.distance * 1000f / this.minTime);
    }
}
