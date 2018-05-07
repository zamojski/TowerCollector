/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators;

import android.location.Location;

import info.zamojski.soft.towercollector.model.Measurement;
import timber.log.Timber;

public class ConditionsValidator {


    public boolean isSameCell(Measurement m1, Measurement m2) {
        return (m1.getCid() == m2.getCid() && m1.getLac() == m2.getLac()
                && m1.getMnc() == m2.getMnc() && m1.getMcc() == m2.getMcc()
                && m1.getPsc() == m2.getPsc());
    }

    public boolean isMinDistanceSatisfied(Location previousLocation, Location currentLocation, int minDistance) {
        // approximate match with 10% tolerance
        float distanceDiff = previousLocation.distanceTo(currentLocation);
        int distanceCondition = minDistance;
        // check conditions
        boolean valid = (1.1f * distanceDiff >= distanceCondition);
        if (!valid)
            Timber.d("isMinDistanceSatisfied(): Failed to achieve destination '%.4f >= %d' condition at 10%% approx. match", distanceDiff, distanceCondition);
        return valid;
    }
}
