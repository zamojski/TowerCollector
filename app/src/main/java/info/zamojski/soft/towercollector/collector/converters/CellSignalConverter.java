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

    public void update(Cell cell, CellInfo cellInfo) {
        if (cellInfo instanceof CellInfoGsm) {
            CellInfoGsm gsmCellInfo = (CellInfoGsm) cellInfo;
            CellSignalStrengthGsm signal = gsmCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            int dbm = signal.getDbm();
            int ta = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? signal.getTimingAdvance() : Cell.UNKNOWN_SIGNAL;
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            cell.setGsmSignalInfo(asu, dbm, ta);
        } else if (cellInfo instanceof CellInfoWcdma) {
            CellInfoWcdma wcdmaCellInfo = (CellInfoWcdma) cellInfo;
            CellSignalStrengthWcdma signal = wcdmaCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            cell.setWcdmaSignalInfo(asu, dbm);
        } else if (cellInfo instanceof CellInfoLte) {
            CellInfoLte lteCellInfo = (CellInfoLte) cellInfo;
            CellSignalStrengthLte signal = lteCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            int ta = signal.getTimingAdvance();
            cell.setLteSignalInfo(asu, dbm, ta);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoNr) {
            CellInfoNr nrCellInfo = (CellInfoNr) cellInfo;
            CellSignalStrengthNr signal = (CellSignalStrengthNr) nrCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == 99) // CellSignalStrengthNr.UNKNOWN_ASU_LEVEL not available
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            cell.setNrSignalInfo(asu, dbm);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoTdscdma) {
            CellInfoTdscdma tdscdmaCellInfo = (CellInfoTdscdma) cellInfo;
            CellSignalStrengthTdscdma signal = tdscdmaCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == 255 || asu == 99) // depending on RSSI (99) or RSCP (255)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            cell.setTdscdmaSignalInfo(asu, dbm);
        } else if (cellInfo instanceof CellInfoCdma) {
            CellInfoCdma cdmaCellInfo = (CellInfoCdma) cellInfo;
            CellSignalStrengthCdma signal = cdmaCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            cell.setCdmaSignalInfo(asu, dbm);
        } else {
            throw new UnsupportedOperationException("Cell signal type not supported `" + cellInfo.getClass().getName() + "`");
        }
    }
}
