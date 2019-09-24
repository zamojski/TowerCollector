/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.events;

import info.zamojski.soft.towercollector.enums.GpsStatus;

public class GpsStatusChangedEvent {

    private GpsStatus status;
    private float accuracy;
    private boolean active;

    public GpsStatusChangedEvent() {
        this.active = false;
    }

    public GpsStatusChangedEvent(GpsStatus status, float accuracy) {
        this.status = status;
        this.accuracy = accuracy;
        this.active = true;
    }

    public GpsStatus getStatus() {
        return status;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public boolean isActive() {
        return active;
    }
}
