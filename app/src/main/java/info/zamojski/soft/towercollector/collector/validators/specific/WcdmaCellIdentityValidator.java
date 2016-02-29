/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityWcdma;
import android.util.Log;

public class WcdmaCellIdentityValidator {

    private static final String TAG = WcdmaCellIdentityValidator.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean isValid(CellIdentityWcdma cell) {
        boolean valid = (isCidInRange(cell.getCid()) && isLacInRange(cell.getLac())
                && isMncInRange(cell.getMnc()) && isMccInRange(cell.getMcc())
                && isPscInRange(cell.getPsc()));
        if (!valid) {
            Log.w(TAG, "isValid(): Invalid CellIdentityWcdma [mcc=" + cell.getMcc() + ", mnc=" + cell.getMnc() + ", lac=" + cell.getLac() + ", cid=" + cell.getCid() + ", psc=" + cell.getPsc() + "]");
            Log.w(TAG, "isValid(): Invalid CellIdentityWcdma " + cell);
        }
        return valid;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean isValid(CellIdentityGsm cell) {
        boolean valid = (isCidInRange(cell.getCid()) && isLacInRange(cell.getLac())
                && isMncInRange(cell.getMnc()) && isMccInRange(cell.getMcc())
                && isPscInRange(cell.getPsc()));
        if (!valid) {
            Log.w(TAG, "isValid(): Invalid CellIdentityWcdma as Gsm JB_MR1 [mcc=" + cell.getMcc() + ", mnc=" + cell.getMnc() + ", lac=" + cell.getLac() + ", cid=" + cell.getCid() + ", psc=" + cell.getPsc() + "]");
            Log.w(TAG, "isValid(): Invalid CellIdentityWcdma as Gsm JB_MR1 " + cell);
        }
        return valid;
    }

    private boolean isCidInRange(int cid) {
        return (cid >= 1 && cid <= 268435455);
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

    private boolean isPscInRange(int psc) {
        return (psc >= 0 && psc <= 511);
    }

}
