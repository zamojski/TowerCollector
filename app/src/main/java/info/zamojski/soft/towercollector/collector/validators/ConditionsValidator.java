/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators;

import android.location.Location;

import info.zamojski.soft.towercollector.model.Cell;
import timber.log.Timber;

public class ConditionsValidator {

    public boolean isSameCell(Cell c1, Cell c2) {
        return (c1.getCid() == c2.getCid() && c1.getLac() == c2.getLac()
                && c1.getMnc() == c2.getMnc() && c1.getMcc() == c2.getMcc()
                && c1.getPsc() == c2.getPsc());
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
