/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityTdscdma;

import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

public class TdscdmaCellIdentityValidator {

    @TargetApi(Build.VERSION_CODES.Q)
    public boolean isValid(CellIdentityTdscdma cell) {
        boolean valid = (isCidInRange(cell.getCid()) && isLacInRange(cell.getLac())
                && isMncInRange(cell.getMncString()) && isMccInRange(cell.getMccString())
                && isCpidInRange(cell.getCpid()));
        if (!valid) {
            Timber.w("isValid(): Invalid CellIdentityTdscdma [mcc=%s, mnc=%s, lac=%s, cid=%s, cpid=%s]", cell.getMccString(), cell.getMncString(), cell.getLac(), cell.getCid(), cell.getCpid());
            Timber.w("isValid(): Invalid CellIdentityTdscdma %s", cell);
        }
        return valid;
    }

    private boolean isCidInRange(int cid) {
        return (cid >= 1 && cid <= 268435455);
    }

    private boolean isLacInRange(int lac) {
        return (lac >= 1 && lac <= 65535);
    }

    private boolean isMncInRange(String mncString) {
        int mnc = StringUtils.toInteger(mncString, Cell.UNKNOWN_CID);
        return (mnc >= 0 && mnc <= 999);
    }

    private boolean isMccInRange(String mccString) {
        int mcc = StringUtils.toInteger(mccString, Cell.UNKNOWN_CID);
        return (mcc >= 100 && mcc <= 999);
    }

    private boolean isCpidInRange(int cpid) {
        return (cpid >= 0 && cpid <= 127);
    }

}
