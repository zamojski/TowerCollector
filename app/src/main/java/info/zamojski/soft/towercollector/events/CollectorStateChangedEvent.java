/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.events;

public class CollectorStateChangedEvent {

    private final boolean active;

    public CollectorStateChangedEvent(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
