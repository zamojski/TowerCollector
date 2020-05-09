/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.converters;

import info.zamojski.soft.towercollector.model.Cell;

import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;

public class CellSignalConverter {

    private static final boolean isApi26 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    private static final boolean isApi29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

    public void update(Cell cell, CellInfo cellInfo) {
        if (cellInfo instanceof CellInfoGsm) {
            CellInfoGsm gsmCellInfo = (CellInfoGsm) cellInfo;
            CellSignalStrengthGsm signal = gsmCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            int dbm = signal.getDbm();
            int ta = isApi26 ? signal.getTimingAdvance() : Cell.UNKNOWN_SIGNAL;
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            // TODO add RSSI to GSM on Android R
            cell.setGsmSignalInfo(asu, dbm, ta);
        } else if (cellInfo instanceof CellInfoWcdma) {
            CellInfoWcdma wcdmaCellInfo = (CellInfoWcdma) cellInfo;
            CellSignalStrengthWcdma signal = wcdmaCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            // TODO add EC/NO to WCDMA on Android R
            cell.setWcdmaSignalInfo(asu, dbm);
        } else if (cellInfo instanceof CellInfoLte) {
            CellInfoLte lteCellInfo = (CellInfoLte) cellInfo;
            CellSignalStrengthLte signal = lteCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            int ta = signal.getTimingAdvance();
            int rsrp = isApi26 ? signal.getRsrp() : Cell.UNKNOWN_SIGNAL;
            int rsrq = isApi26 ? signal.getRsrq() : Cell.UNKNOWN_SIGNAL;
            int rssi = isApi29 ? signal.getRssi() : Cell.UNKNOWN_SIGNAL;
            int rssnr = isApi26 ? signal.getRssnr() : Cell.UNKNOWN_SIGNAL;
            int cqi = isApi26 ? signal.getCqi() : Cell.UNKNOWN_SIGNAL;
            cell.setLteSignalInfo(asu, dbm, ta, rsrp, rsrq, rssi, rssnr, cqi);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoNr) {
            CellInfoNr nrCellInfo = (CellInfoNr) cellInfo;
            CellSignalStrengthNr signal = (CellSignalStrengthNr) nrCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == 99) // CellSignalStrengthNr.UNKNOWN_ASU_LEVEL not available
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            int csiRsrp = signal.getCsiRsrp();
            int csiRsrq = signal.getCsiRsrq();
            int csiSinr = signal.getCsiSinr();
            int ssRsrp = signal.getSsRsrp();
            int ssRsrq = signal.getSsRsrq();
            int ssSinr = signal.getSsSinr();
            cell.setNrSignalInfo(asu, dbm, csiRsrp, csiRsrq, csiSinr, ssRsrp, ssRsrq, ssSinr);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoTdscdma) {
            CellInfoTdscdma tdscdmaCellInfo = (CellInfoTdscdma) cellInfo;
            CellSignalStrengthTdscdma signal = tdscdmaCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == 255 || asu == 99) // depending on RSSI (99) or RSCP (255)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            int rscp = signal.getRscp();
            cell.setTdscdmaSignalInfo(asu, dbm, rscp);
        } else if (cellInfo instanceof CellInfoCdma) {
            CellInfoCdma cdmaCellInfo = (CellInfoCdma) cellInfo;
            CellSignalStrengthCdma signal = cdmaCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            int cdmaDbm = signal.getCdmaDbm();
            int cdmaEcio = signal.getCdmaEcio();
            int evdoDbm = signal.getEvdoDbm();
            int evdoEcio = signal.getEvdoEcio();
            int evdoSnr = signal.getEvdoSnr();
            cell.setCdmaSignalInfo(asu, dbm, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr);
        } else {
            throw new UnsupportedOperationException("Cell signal type not supported `" + cellInfo.getClass().getName() + "`");
        }
    }
}
