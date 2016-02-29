/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.validators;

import info.zamojski.soft.towercollector.collector.validators.specific.CdmaCellLocationValidator;
import info.zamojski.soft.towercollector.collector.validators.specific.GsmCellLocationValidator;
import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;

import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public class CellLocationValidator {

    private GsmCellLocationValidator gsmValidator;
    private CdmaCellLocationValidator cdmaValidator;

    public boolean isValid(CellLocation cellLocation, int mcc, int mnc) {
        if (cellLocation instanceof GsmCellLocation) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
            return getGsmValidator().isValid(gsmCellLocation, mcc, mnc);
        }
        if (cellLocation instanceof CdmaCellLocation) {
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
            return getCdmaValidator().isValid(cdmaCellLocation);
        }
        throw new UnsupportedOperationException("Cell location type not supported `" + cellLocation.getClass().getName() + "`");
    }

    public boolean isValid(NeighboringCellInfo neighboringCell, Measurement measurement) {
        NetworkGroup netType = NetworkTypeUtils.getNetworkGroup(neighboringCell.getNetworkType());
        if (netType == NetworkGroup.Gsm) {
            return getGsmValidator().isValid(neighboringCell.getCid(), neighboringCell.getLac(), measurement.getMnc(), measurement.getMcc(), NeighboringCellInfo.UNKNOWN_CID);
        } else if (netType == NetworkGroup.Wcdma) {
            // NOTE: Maybe some phones return full set for WCDMA and then it should be valid
            return getGsmValidator().isValid(neighboringCell.getCid(), neighboringCell.getLac(), measurement.getMnc(), measurement.getMcc(), neighboringCell.getPsc());
        }
        return false;
    }

    private GsmCellLocationValidator getGsmValidator() {
        if (gsmValidator == null) {
            gsmValidator = new GsmCellLocationValidator();
        }
        return gsmValidator;
    }

    private CdmaCellLocationValidator getCdmaValidator() {
        if (cdmaValidator == null) {
            cdmaValidator = new CdmaCellLocationValidator();
        }
        return cdmaValidator;
    }

}
