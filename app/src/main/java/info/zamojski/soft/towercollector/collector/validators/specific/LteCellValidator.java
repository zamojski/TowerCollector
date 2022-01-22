/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.telephony.CellIdentityLte;

import cz.mroczis.netmonster.core.model.cell.CellLte;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

public class LteCellValidator {

    public boolean isValid(CellIdentityLte cell) {
        boolean valid = isValid(cell.getMcc(), cell.getMnc(), cell.getTac(), cell.getCi(), cell.getPci());
        if (!valid) {
            Timber.w("isValid(): Invalid CellIdentityLte %s", cell);
        }
        return valid;
    }

    public boolean isValid(CellLte cell) {
        int mcc = cell.getNetwork() != null ? StringUtils.toInteger(cell.getNetwork().getMcc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID;
        int mnc = cell.getNetwork() != null ? StringUtils.toInteger(cell.getNetwork().getMnc(), Cell.UNKNOWN_CID) : Cell.UNKNOWN_CID;
        int tac = cell.getTac() != null ? cell.getTac() : Cell.UNKNOWN_CID;
        int ci = cell.getEci() != null ? cell.getEci() : Cell.UNKNOWN_CID;
        int pci = cell.getPci() != null ? cell.getPci() : Cell.UNKNOWN_CID;
        boolean valid = isValid(mcc, mnc, tac, ci, pci);
        if (!valid) {
            Timber.w("isValid(): Invalid CellLte %s", cell);
        }
        return valid;
    }

    private boolean isValid(int mcc, int mnc, int tac, int ci, int pci) {
        boolean valid = (isCiInRange(ci) && isTacInRange(tac)
                && isMncInRange(mnc) && isMccInRange(mcc)
                && isPciInRange(pci));
        if (!valid) {
            Timber.w("isValid(): Invalid LTE Cell [mcc=%s, mnc=%s, lac=%s, cid=%s, psc=%s]", mcc, mnc, tac, ci, pci);
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
