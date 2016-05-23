/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators;

import android.location.Location;
import trikita.log.Log;

public class LocationValidator {

    private static final String TAG = LocationValidator.class.getSimpleName();

    public static final long NO_LOCATION_TIME_DIFF = 5 * 60 * 1000;// 5 minutes

    private float minAccuracy;

    public LocationValidator(float minAccuracy) {
        this.minAccuracy = minAccuracy;
    }

    public boolean isValid(Location location) {
        return (isLocationInRange(location) && hasRequiredAccuracy(location));
    }

    public boolean isUpToDate(long gpsTimestamp, long systemTimestamp) {
        long timeDiff = Math.abs(systemTimestamp - gpsTimestamp);
        // check conditions
        boolean valid = timeDiff <= NO_LOCATION_TIME_DIFF;
        if (!valid)
            Log.d("isUpToDate(): Location is outdated by %d milliseconds > %d", timeDiff, NO_LOCATION_TIME_DIFF);
        return valid;
        // return true;
    }

    private boolean isLocationInRange(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        return ((lat >= -90 && lat <= 90 && lat != 0.0) && (lon >= -180 && lon <= 180 && lon != 0.0));
    }

    public boolean hasRequiredAccuracy(Location location) {
        return (location.hasAccuracy() && location.getAccuracy() <= minAccuracy);
    }

}
