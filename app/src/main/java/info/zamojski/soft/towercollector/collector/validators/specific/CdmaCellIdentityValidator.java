/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators.specific;

import android.telephony.CellIdentityCdma;

import timber.log.Timber;

public class CdmaCellIdentityValidator {

    public boolean isValid(CellIdentityCdma cell) {
        boolean valid = (isBidInRange(cell.getBasestationId()) && isNidInRange(cell.getNetworkId())
                && isSidInRange(cell.getSystemId()));
        if (!valid) {
            Timber.w("isValid(): Invalid CellIdentityCdma [sid=%s, nid=%s, bid=%s]", cell.getSystemId(), cell.getNetworkId(), cell.getBasestationId());
            Timber.w("isValid(): Invalid CellIdentityCdma %s", cell);
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
