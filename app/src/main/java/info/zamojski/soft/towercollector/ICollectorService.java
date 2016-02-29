/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import info.zamojski.soft.towercollector.enums.GpsStatus;
import info.zamojski.soft.towercollector.enums.Validity;

interface ICollectorService {
    GpsStatus getGpsStatus();

    float getGpsLastAccuracy();

    Validity isSystemTimeValid();
}
