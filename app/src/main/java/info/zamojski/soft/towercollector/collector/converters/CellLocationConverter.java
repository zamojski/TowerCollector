/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.converters;

import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;

import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public class CellLocationConverter {

    public void update(Measurement m, CellLocation cellLocation, int mcc, int mnc, NetworkGroup networkType) {
        if (cellLocation instanceof GsmCellLocation) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
            if (gsmCellLocation.getCid() <= 65535 && gsmCellLocation.getPsc() == NeighboringCellInfo.UNKNOWN_CID) {
                m.setGsmCellLocation(mcc, mnc, gsmCellLocation.getLac(), gsmCellLocation.getCid(), NetworkGroup.Gsm);
            } else {
                // fix invalid network types (unfortunately not possible to distinguish between UMTS and LTE)
                if (networkType == NetworkGroup.Gsm || networkType == NetworkGroup.Cdma)
                    networkType = NetworkGroup.Unknown;
                int psc = gsmCellLocation.getPsc();
                if (psc == NeighboringCellInfo.UNKNOWN_CID || psc == Measurement.UNKNOWN_CID) {
                    psc = Measurement.UNKNOWN_CID;
                } else if (psc >= 504) {
                    // only UMTS networks support larger PSC
                    networkType = NetworkGroup.Wcdma;
                }
                m.setGsmCellLocation(mcc, mnc, gsmCellLocation.getLac(), gsmCellLocation.getCid(), psc, networkType);
            }
        } else if (cellLocation instanceof CdmaCellLocation) {
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
            m.setCdmaCellLocation(cdmaCellLocation.getSystemId(), cdmaCellLocation.getNetworkId(), cdmaCellLocation.getBaseStationId());
        } else {
            throw new UnsupportedOperationException("Cell location type not supported `" + cellLocation.getClass().getName() + "`");
        }
    }

    public void update(Measurement m, NeighboringCellInfo cell, int mcc, int mnc) {
        // no type checking because only GSM and UMTS cells can have neighboring cells
        NetworkGroup networkType = NetworkTypeUtils.getNetworkGroup(cell.getNetworkType());
        if (networkType == NetworkGroup.Gsm) {
            m.setGsmCellLocation(mcc, mnc, cell.getLac(), cell.getCid(), NetworkGroup.Gsm);
        } else {
            int psc = cell.getPsc();
            if (psc == NeighboringCellInfo.UNKNOWN_CID) {
                psc = Measurement.UNKNOWN_CID;
            }
            m.setGsmCellLocation(mcc, mnc, m.getLac(), m.getCid(), psc, NetworkGroup.Wcdma);
        }
    }
}
