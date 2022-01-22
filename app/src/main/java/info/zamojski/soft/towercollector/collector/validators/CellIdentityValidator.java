/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators;

import android.os.Build;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;

public class CellIdentityValidator extends CellValidatorBase {

    public boolean isValid(CellInfo cellInfo) {
        if (cellInfo instanceof CellInfoGsm) {
            CellInfoGsm gsmCellInfo = (CellInfoGsm) cellInfo;
            // If is compatible with API 17 hack (PSC on GSM) return true
            boolean wcdmaApi17Valid = getWcdmaValidator().isValid(gsmCellInfo.getCellIdentity());
            if (wcdmaApi17Valid)
                return true;
            return getGsmValidator().isValid(gsmCellInfo.getCellIdentity());
        }
        if (cellInfo instanceof CellInfoWcdma) {
            CellInfoWcdma wcdmaCellInfo = (CellInfoWcdma) cellInfo;
            return getWcdmaValidator().isValid(wcdmaCellInfo.getCellIdentity());
        }
        if (cellInfo instanceof CellInfoLte) {
            CellInfoLte lteCellInfo = (CellInfoLte) cellInfo;
            return getLteValidator().isValid(lteCellInfo.getCellIdentity());
        }
        if (cellInfo instanceof CellInfoCdma) {
            CellInfoCdma cdmaCellInfo = (CellInfoCdma) cellInfo;
            return getCdmaValidator().isValid(cdmaCellInfo.getCellIdentity());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoNr) {
            CellInfoNr nrCellInfo = (CellInfoNr) cellInfo;
            return getNrValidator().isValid((CellIdentityNr) nrCellInfo.getCellIdentity());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoTdscdma) {
            CellInfoTdscdma tdscdmaCellInfo = (CellInfoTdscdma) cellInfo;
            return getTdscdmaValidator().isValid(tdscdmaCellInfo.getCellIdentity());
        }
        throw new UnsupportedOperationException("Cell identity type not supported `" + cellInfo.getClass().getName() + "`");
    }
}
