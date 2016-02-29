/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.events;

import info.zamojski.soft.towercollector.collector.ParseResult;

public class MeasurementProcessedEvent {

    private ParseResult result;

    public MeasurementProcessedEvent(ParseResult result) {
        this.result = result;
    }

    public ParseResult getResult() {
        return result;
    }

}
