/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityGsm;
import trikita.log.Log;

public class GsmCellIdentityValidator {

    private static final String TAG = GsmCellIdentityValidator.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean isValid(CellIdentityGsm cell) {
        boolean valid = (isCidInRange(cell.getCid()) && isLacInRange(cell.getLac())
                && isMncInRange(cell.getMnc()) && isMccInRange(cell.getMcc()));
        if (!valid) {
            Log.w(TAG, "isValid(): Invalid CellIdentityGsm [mcc=%s, mnc=%s, lac=%s, cid=%s, psc=%s]", cell.getMcc(), cell.getMnc(), cell.getLac(), cell.getCid(), cell.getPsc());
            Log.w(TAG, "isValid(): Invalid CellIdentityGsm %s", cell);
        }
        return valid;
    }

    private boolean isCidInRange(int cid) {
        return (cid >= 1 && cid <= 65535);
    }

    private boolean isLacInRange(int lac) {
        return (lac >= 1 && lac <= 65535);
    }

    private boolean isMncInRange(int mnc) {
        return (mnc >= 0 && mnc <= 999);
    }

    private boolean isMccInRange(int mcc) {
        return (mcc >= 100 && mcc <= 999);
    }

}
