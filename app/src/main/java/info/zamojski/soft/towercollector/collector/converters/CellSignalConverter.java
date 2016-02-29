/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.converters;

import info.zamojski.soft.towercollector.model.Measurement;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CellSignalConverter {

    public void update(Measurement m, CellInfo cellInfo) {
        if (cellInfo instanceof CellInfoGsm) {
            CellInfoGsm gsmCellInfo = (CellInfoGsm) cellInfo;
            CellSignalStrengthGsm signal = gsmCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            int dbm = signal.getDbm();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Measurement.UNKNOWN_SIGNAL;
            m.setGsmSignalInfo(asu, dbm);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && cellInfo instanceof CellInfoWcdma) {
            CellInfoWcdma wcdmaCellInfo = (CellInfoWcdma) cellInfo;
            CellSignalStrengthWcdma signal = wcdmaCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Measurement.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            m.setWcdmaSignalInfo(asu, dbm);
        } else if (cellInfo instanceof CellInfoLte) {
            CellInfoLte lteCellInfo = (CellInfoLte) cellInfo;
            CellSignalStrengthLte signal = lteCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Measurement.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            int ta = signal.getTimingAdvance();
            m.setLteSignalInfo(asu, dbm, ta);
        } else if (cellInfo instanceof CellInfoCdma) {
            CellInfoCdma cdmaCellInfo = (CellInfoCdma) cellInfo;
            CellSignalStrengthCdma signal = cdmaCellInfo.getCellSignalStrength();
            int asu = signal.getAsuLevel();
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Measurement.UNKNOWN_SIGNAL;
            int dbm = signal.getDbm();
            m.setCdmaSignalInfo(asu, dbm);
        } else {
            throw new UnsupportedOperationException("Cell signal type not supported `" + cellInfo.getClass().getName() + "`");
        }
    }
}
