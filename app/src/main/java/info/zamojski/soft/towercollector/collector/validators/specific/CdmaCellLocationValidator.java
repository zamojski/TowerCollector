/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.telephony.cdma.CdmaCellLocation;
import android.util.Log;

public class CdmaCellLocationValidator {

    private static final String TAG = CdmaCellLocationValidator.class.getSimpleName();

    public boolean isValid(CdmaCellLocation cell) {
        boolean valid = (isBidInRange(cell.getBaseStationId()) && isNidInRange(cell.getNetworkId())
                && isSidInRange(cell.getSystemId()));
        if (!valid) {
            Log.w(TAG, "isValid(): Invalid CdmaCellLocation [sid=" + cell.getSystemId() + ", nid = " + cell.getNetworkId() + ", bid=" + cell.getBaseStationId() + "]");
            Log.w(TAG, "isValid(): Invalid CdmaCellLocation " + cell);
        }
        return valid;
    }

    private boolean isBidInRange(int bid) {
        return (bid >= 1 && bid <= 65535);
    }

    private boolean isNidInRange(int nid) {
        return (nid >= 1 && nid <= 65535);
    }

    private boolean isSidInRange(int sid) {
        return (sid >= 0 && sid <= 32767);
    }

}
