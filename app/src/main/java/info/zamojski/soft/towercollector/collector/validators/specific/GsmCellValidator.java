/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.telephony.CellIdentityGsm;

import cz.mroczis.netmonster.core.model.cell.CellGsm;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

public class GsmCellValidator {

    public boolean isValid(CellIdentityGsm cell) {
        boolean valid = isValid(cell.getMcc(), cell.getMnc(), cell.getLac(), cell.getCid());
        if (!valid) {
            Timber.w("isValid(): Invalid CellIdentityGsm %s", cell);
        }
        return valid;
    }

    public boolean isValid(CellGsm cell) {
        int mcc = cell.getNetwork() != null ? StringUtils.toInteger(cell.getNetwork().getMcc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID;
        int mnc = cell.getNetwork() != null ? StringUtils.toInteger(cell.getNetwork().getMnc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID;
        int lac = cell.getLac() != null ? cell.getLac() : Cell.UNKNOWN_CID;
        int cid = cell.getCid() != null ? cell.getCid() : Cell.UNKNOWN_CID;
        boolean valid = isValid(mcc, mnc, lac, cid);
        if (!valid) {
            Timber.w("isValid(): Invalid CellGsm %s", cell);
        }
        return valid;
    }

    private boolean isValid(int mcc, int mnc, int lac, int cid) {
        boolean valid = isCidInRange(cid) && isLacInRange(lac) && isMncInRange(mnc) && isMccInRange(mcc);
        if (!valid) {
            Timber.w("isValid(): Invalid GSM Cell [mcc=%s, mnc=%s, lac=%s, cid=%s]", mcc, mnc, lac, cid);
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
