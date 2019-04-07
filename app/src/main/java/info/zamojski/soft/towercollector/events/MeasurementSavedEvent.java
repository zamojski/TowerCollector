/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.events;

import info.zamojski.soft.towercollector.collector.ParseResult;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.model.Statistics;

public class MeasurementSavedEvent extends MeasurementProcessedEvent {

    private Measurement measurement;
    private Statistics statistics;

    public MeasurementSavedEvent(Measurement measurement, Statistics statistics) {
        super(ParseResult.Saved);
        this.measurement = measurement;
        this.statistics = statistics;
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    public Statistics getStatistics() {
        return statistics;
    }
}
