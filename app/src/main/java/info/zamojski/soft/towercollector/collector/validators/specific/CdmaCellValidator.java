/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.telephony.CellIdentityCdma;

import cz.mroczis.netmonster.core.model.cell.CellCdma;
import info.zamojski.soft.towercollector.model.Cell;
import timber.log.Timber;

public class CdmaCellValidator {

    public boolean isValid(CellIdentityCdma cell) {
        boolean valid = isValid(cell.getBasestationId(), cell.getNetworkId(), cell.getSystemId());
        if (!valid) {
            Timber.w("isValid(): Invalid CellIdentityCdma %s", cell);
        }
        return valid;
    }

    public boolean isValid(CellCdma cell) {
        int nid = cell.getNid() != null ? cell.getNid() : Cell.UNKNOWN_CID;
        int bid = cell.getBid() != null ? cell.getBid() : Cell.UNKNOWN_CID;
        boolean valid = isValid(cell.getSid(), nid, bid);
        if (!valid) {
            Timber.w("isValid(): Invalid CellCdma %s", cell);
        }
        return valid;
    }

    private boolean isValid(int sid, int nid, int bid) {
        boolean valid = (isBidInRange(bid) && isNidInRange(nid)
                && isSidInRange(sid));
        if (!valid) {
            Timber.w("isValid(): Invalid CDMA Cell [sid=%s, nid=%s, bid=%s]", sid, nid, bid);
        }
        return valid;
    }

    private boolean isBidInRange(int bid) {
        return (bid >= 1 && bid <= 65535);
    }

    private boolean isNidInRange(int nid) {
        return (nid >= 1 && nid <= 65535);
    }

    private boolean isSidInRange(int sid) {
        return (sid >= 0 && sid <= 32767);
    }

}
