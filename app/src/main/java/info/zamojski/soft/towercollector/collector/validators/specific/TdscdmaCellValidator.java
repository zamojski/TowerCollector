/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityTdscdma;

import cz.mroczis.netmonster.core.model.cell.CellTdscdma;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

public class TdscdmaCellValidator {

    @TargetApi(Build.VERSION_CODES.Q)
    public boolean isValid(CellIdentityTdscdma cell) {
        boolean valid = isValid(cell.getMccString(), cell.getMncString(), cell.getLac(), cell.getCid(), cell.getCpid());
        if (!valid) {
            Timber.w("isValid(): Invalid CellIdentityTdscdma %s", cell);
        }
        return valid;
    }

    public boolean isValid(CellTdscdma cell) {
        String mccString = cell.getNetwork() != null ? cell.getNetwork().getMcc() : null;
        String mncString = cell.getNetwork() != null ? cell.getNetwork().getMnc() : null;
        int lac = cell.getLac() != null ? cell.getLac() : Cell.UNKNOWN_CID;
        int ci = cell.getCi() != null ? cell.getCi() : Cell.UNKNOWN_CID;
        int cpid = cell.getCpid() != null ? cell.getCpid() : Cell.UNKNOWN_CID;
        boolean valid = isValid(mccString, mncString, lac, ci, cpid);
        if (!valid) {
            Timber.w("isValid(): Invalid CellTdscdma %s", cell);
        }
        return valid;
    }

    private boolean isValid(String mccString, String mncString, int lac, int cid, int cpid) {
        boolean valid = (isCidInRange(cid) && isLacInRange(lac)
                && isMncInRange(mncString) && isMccInRange(mccString)
                && isCpidInRange(cpid));
        if (!valid) {
            Timber.w("isValid(): Invalid TD-SCDMA Cell [mcc=%s, mnc=%s, lac=%s, cid=%s, cpid=%s]", mccString, mncString, lac, cid, cpid);
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
