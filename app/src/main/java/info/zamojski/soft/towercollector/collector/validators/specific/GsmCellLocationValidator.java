/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import info.zamojski.soft.towercollector.model.Measurement;

import android.telephony.NeighboringCellInfo;
import android.telephony.gsm.GsmCellLocation;
import trikita.log.Log;

public class GsmCellLocationValidator {

    private static final String TAG = GsmCellLocationValidator.class.getSimpleName();

    public boolean isValid(GsmCellLocation cell, int mcc, int mnc) {
        boolean valid = isValid(cell.getCid(), cell.getLac(), mnc, mcc, cell.getPsc());
        if (!valid) {
            Log.w("isValid(): Invalid GsmCellLocation [mcc=%s, mnc =%s, lac=%s, cid=%s, psc=%s]", mcc, mnc, cell.getLac(), cell.getCid(), cell.getPsc());
            Log.w("isValid(): Invalid GsmCellLocation %s", cell);
        }
        return valid;
    }

    public boolean isValid(int cid, int lac, int mnc, int mcc, int psc) {
        boolean valid = (isCidOrCiInRange(cid) && isLacOrTacInRange(lac)
                && isMncInRange(mnc) && isMccInRange(mcc)
                && (psc == NeighboringCellInfo.UNKNOWN_CID || psc == Measurement.UNKNOWN_CID || isPscOrPciInRange(psc)));
        return valid;
    }

    private boolean isCidOrCiInRange(int cidOrCi) {
        return (cidOrCi >= 1 && cidOrCi <= 268435455);
    }

    private boolean isLacOrTacInRange(int lacOrTac) {
        return (lacOrTac >= 1 && lacOrTac <= 65535);
    }

    private boolean isMncInRange(int mnc) {
        return (mnc >= 0 && mnc <= 999);
    }

    private boolean isMccInRange(int mcc) {
        return (mcc >= 100 && mcc <= 999);
    }

    private boolean isPscOrPciInRange(int pscOrPci) {
        return (pscOrPci >= 0 && pscOrPci <= 511);
    }

}
