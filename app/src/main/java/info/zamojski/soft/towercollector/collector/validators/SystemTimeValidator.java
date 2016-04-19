/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators;

import trikita.log.Log;

public class SystemTimeValidator {

    private static final String TAG = SystemTimeValidator.class.getSimpleName();

    private static final long TwoDayTimeDiff = 2 * 24 * 60 * 60 * 1000;

    public boolean isValid(long systemTime, long gpsTime) {
        long timeDiff = (systemTime - gpsTime);
        Log.d(TAG, "isValid(): System to gps time difference = " + timeDiff + " <= " + TwoDayTimeDiff);
        return Math.abs(timeDiff) <= TwoDayTimeDiff;
    }

}
