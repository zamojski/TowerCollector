/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityWcdma;

import cz.mroczis.netmonster.core.model.cell.CellWcdma;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

public class WcdmaCellValidator {

    public boolean isValid(CellIdentityWcdma cell) {
        boolean valid = isValid(cell.getMcc(), cell.getMnc(), cell.getLac(), cell.getCid(), cell.getPsc());
        if (!valid) {
            Timber.w("isValid(): Invalid CellIdentityWcdma %s", cell);
        }
        return valid;
    }

    public boolean isValid(CellIdentityGsm cell) {
        boolean valid = isValid(cell.getMcc(), cell.getMnc(), cell.getLac(), cell.getCid(), cell.getPsc());
        if (!valid) {
            Timber.w("isValid(): Invalid CellIdentityWcdma as Gsm JB_MR1 %s", cell);
        }
        return valid;
    }

    public boolean isValid(CellWcdma cell) {
        int mcc = cell.getNetwork() != null ? StringUtils.toInteger(cell.getNetwork().getMcc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID;
        int mnc = cell.getNetwork() != null ? StringUtils.toInteger(cell.getNetwork().getMnc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID;
        int lac = cell.getLac() != null ? cell.getLac() : Cell.UNKNOWN_CID;
        int cid = cell.getCid() != null ? cell.getCid() : Cell.UNKNOWN_CID;
        int psc = cell.getPsc() != null ? cell.getPsc() : Cell.UNKNOWN_CID;
        boolean valid = isValid(mcc, mnc, lac, cid, psc);
        if (!valid) {
            Timber.w("isValid(): Invalid CellWcdma %s", cell);
        }
        return valid;
    }

    private boolean isValid(int mcc, int mnc, int lac, int cid, int psc) {
        boolean valid = (isCidInRange(cid) && isLacInRange(lac)
                && isMncInRange(mnc) && isMccInRange(mcc)
                && isPscInRange(psc));
        if (!valid) {
            Timber.w("isValid(): Invalid WCDMA Cell [mcc=%s, mnc=%s, lac=%s, cid=%s, psc=%s]", mcc, mnc, lac, cid, psc);
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
