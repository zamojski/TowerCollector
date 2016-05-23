/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityLte;
import trikita.log.Log;

public class LteCellIdentityValidator {

    private static final String TAG = LteCellIdentityValidator.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean isValid(CellIdentityLte cell) {
        boolean valid = (isCiInRange(cell.getCi()) && isTacInRange(cell.getTac())
                && isMncInRange(cell.getMnc()) && isMccInRange(cell.getMcc())
                && isPciInRange(cell.getPci()));
        if (!valid) {
            Log.w("isValid(): Invalid CellIdentityLte [mcc=%s, mnc=%s, lac=%s, cid=%s, psc=%s]", cell.getMcc(), cell.getMnc(), cell.getTac(), cell.getCi(), cell.getPci());
            Log.w("isValid(): Invalid CellIdentityLte %s", cell);
        }
        return valid;
    }

    private boolean isCiInRange(int ci) {
        return (ci >= 1 && ci <= 268435455);
    }

    private boolean isTacInRange(int tac) {
        return (tac >= 1 && tac <= 65535);
    }

    private boolean isMncInRange(int mnc) {
        return (mnc >= 0 && mnc <= 999);
    }

    private boolean isMccInRange(int mcc) {
        return (mcc >= 100 && mcc <= 999);
    }

    private boolean isPciInRange(int pci) {
        return (pci >= 0 && pci <= 503);
    }

}
