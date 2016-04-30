/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.events;

import java.util.List;

import info.zamojski.soft.towercollector.model.Measurement;

public class MeasurementsCollectedEvent {

    private List<Measurement> measurements;

    public MeasurementsCollectedEvent(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }
}
