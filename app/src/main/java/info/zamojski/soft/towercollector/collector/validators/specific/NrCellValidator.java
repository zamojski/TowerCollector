/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityNr;

import cz.mroczis.netmonster.core.model.cell.CellNr;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

public class NrCellValidator {

    @TargetApi(Build.VERSION_CODES.Q)
    public boolean isValid(CellIdentityNr cell) {
        boolean valid = isValid(cell.getMccString(), cell.getMncString(), cell.getTac(), cell.getNci(), cell.getPci());
        if (!valid) {
            Timber.w("isValid(): Invalid CellIdentityNr %s", cell);
        }
        return valid;
    }

    public boolean isValid(CellNr cell) {
        String mccString = cell.getNetwork() != null ? cell.getNetwork().getMcc() : null;
        String mncString = cell.getNetwork() != null ? cell.getNetwork().getMnc() : null;
        int tac = cell.getTac() != null ? cell.getTac() : Cell.UNKNOWN_CID;
        long nci = cell.getNci() != null ? cell.getNci() : Cell.UNKNOWN_CID_LONG;
        int pci = cell.getPci() != null ? cell.getPci() : Cell.UNKNOWN_CID;
        boolean valid = isValid(mccString, mncString, tac, nci, pci);
        if (!valid) {
            Timber.w("isValid(): Invalid CellNr %s", cell);
        }
        return valid;
    }

    private boolean isValid(String mccString, String mncString, int tac, long nci, int pci) {
        boolean valid = (isNciInRange(nci) && isTacInRange(tac)
                && isMncInRange(mncString) && isMccInRange(mccString)
                && isPciInRange(pci));
        if (!valid) {
            Timber.w("isValid(): Invalid NR Cell [mcc=%s, mnc=%s, tac=%s, nci=%s, pci=%s]", mccString, mncString, tac, nci, pci);
        }
        return valid;
    }

    private boolean isNciInRange(long nci) {
        return (nci >= 1 && nci <= 68719476735L);
    }

    private boolean isTacInRange(int tac) {
        return (tac >= 1 && tac <= 16777215);
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
