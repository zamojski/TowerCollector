/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityNr;

import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

public class NrCellIdentityValidator {

    @TargetApi(Build.VERSION_CODES.Q)
    public boolean isValid(CellIdentityNr cell) {
        boolean valid = (isNciInRange(cell.getNci()) && isTacInRange(cell.getTac())
                && isMncInRange(cell.getMncString()) && isMccInRange(cell.getMccString())
                && isPciInRange(cell.getPci()));
        if (!valid) {
            Timber.w("isValid(): Invalid CellIdentityNr [mcc=%s, mnc=%s, tac=%s, nci=%s, pci=%s]", cell.getMccString(), cell.getMncString(), cell.getTac(), cell.getNci(), cell.getPci());
            Timber.w("isValid(): Invalid CellIdentityNr %s", cell);
        }
        return valid;
    }

    private boolean isNciInRange(long nci) {
        return (nci >= 1 && nci <= 68719476735L);
    }

    private boolean isTacInRange(int tac) {
        return (tac >= 1 && tac <= 65535);
    }

    private boolean isMncInRange(String mncString) {
        int mnc = StringUtils.toInteger(mncString, Cell.UNKNOWN_CID);
        return (mnc >= 0 && mnc <= 999);
    }

    private boolean isMccInRange(String mccString) {
        int mcc = StringUtils.toInteger(mccString, Cell.UNKNOWN_CID);
        return (mcc >= 100 && mcc <= 999);
    }

    private boolean isPciInRange(int pci) {
        return (pci >= 0 && pci <= 1007);
    }

}
