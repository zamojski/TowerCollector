/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.converters;

import android.os.Build;
import android.telephony.NeighboringCellInfo;

import cz.mroczis.netmonster.core.model.cell.CellCdma;
import cz.mroczis.netmonster.core.model.cell.CellGsm;
import cz.mroczis.netmonster.core.model.cell.CellLte;
import cz.mroczis.netmonster.core.model.cell.CellNr;
import cz.mroczis.netmonster.core.model.cell.CellTdscdma;
import cz.mroczis.netmonster.core.model.cell.CellWcdma;
import cz.mroczis.netmonster.core.model.cell.ICell;
import cz.mroczis.netmonster.core.model.signal.SignalCdma;
import cz.mroczis.netmonster.core.model.signal.SignalGsm;
import cz.mroczis.netmonster.core.model.signal.SignalLte;
import cz.mroczis.netmonster.core.model.signal.SignalNr;
import cz.mroczis.netmonster.core.model.signal.SignalTdscdma;
import cz.mroczis.netmonster.core.model.signal.SignalWcdma;
import info.zamojski.soft.towercollector.model.Cell;

public class NetMonsterSignalConverter {

    public void update(Cell cell, ICell netMonsterCell) {
        if (netMonsterCell instanceof CellGsm) {
            CellGsm gsmCell = (CellGsm) netMonsterCell;
            SignalGsm signal = gsmCell.getSignal();
            int asu = fixIntNull(signal.getAsu());
            int dbm = fixIntNull(signal.getDbm());
            int ta = fixIntNull(signal.getTimingAdvance());
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            int rssi = fixIntNull(signal.getRssi());
            int arfcn = fixIntNull(gsmCell.getBand() != null ? gsmCell.getBand().getArfcn() : null, Cell.UNKNOWN_CID);
            cell.setNetMonsterGsmSignal(asu, dbm, ta, rssi, arfcn);
        } else if (netMonsterCell instanceof CellWcdma) {
            CellWcdma wcdmaCell = (CellWcdma) netMonsterCell;
            SignalWcdma signal = wcdmaCell.getSignal();
            int asu = fixIntNull(signal.getRssiAsu());
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = fixIntNull(signal.getDbm());
            int ecNo = fixIntNull(signal.getEcno());
            int arfcn = fixIntNull(wcdmaCell.getBand() != null ? wcdmaCell.getBand().getDownlinkUarfcn() : null, Cell.UNKNOWN_CID);
            cell.setNetMonsterWcdmaSignal(asu, dbm, ecNo, arfcn);
        } else if (netMonsterCell instanceof CellLte) {
            CellLte lteCell = (CellLte) netMonsterCell;
            SignalLte signal = lteCell.getSignal();
            int asu = fixIntNull(signal.getRssiAsu());
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = fixLteRsrpOnSamsung(signal.getDbm());
            int ta = fixIntNull(signal.getTimingAdvance());
            int rsrp = fixDoubleNull(signal.getRsrp());
            int rsrq = fixDoubleNull(signal.getRsrq());
            int rssi = fixIntNull(signal.getRssi());
            int rssnr = fixDoubleNull(signal.getSnr());
            int cqi = fixIntNull(signal.getCqi());
            int arfcn = fixIntNull(lteCell.getBand() != null ? lteCell.getBand().getDownlinkEarfcn() : null, Cell.UNKNOWN_CID);
            cell.setNetMonsterLteSignal(asu, dbm, ta, rsrp, rsrq, rssi, rssnr, cqi, arfcn);
        } else if (netMonsterCell instanceof CellNr) {
            CellNr nrCell = (CellNr) netMonsterCell;
            SignalNr signal = nrCell.getSignal();
            int asu = fixIntNull(signal.getSsRsrpAsu()); // Same ASU as taken by Android from Dbm
            if (asu == 99) // CellSignalStrengthNr.UNKNOWN_ASU_LEVEL not available
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = fixIntNull(signal.getDbm());
            int csiRsrp = fixIntNull(signal.getCsiRsrp());
            int csiRsrq = fixIntNull(signal.getCsiRsrq());
            int csiSinr = fixIntNull(signal.getCsiSinr());
            int ssRsrp = fixIntNull(signal.getSsRsrp());
            int ssRsrq = fixIntNull(signal.getSsRsrq());
            int ssSinr = fixIntNull(signal.getSsSinr());
            int arfcn = fixIntNull(nrCell.getBand() != null ? nrCell.getBand().getDownlinkArfcn() : null, Cell.UNKNOWN_CID);
            cell.setNetMonsterNrSignal(asu, dbm, csiRsrp, csiRsrq, csiSinr, ssRsrp, ssRsrq, ssSinr, arfcn);
        } else if (netMonsterCell instanceof CellTdscdma) {
            CellTdscdma tdscdmaCell = (CellTdscdma) netMonsterCell;
            SignalTdscdma signal = tdscdmaCell.getSignal();
            int asu = fixIntNull(signal.getRscpAsu()); // Same ASU as taken by Android from Dbm
            if (asu == 255 || asu == 99) // depending on RSSI (99) or RSCP (255)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = fixIntNull(signal.getDbm());
            int rscp = fixIntNull(signal.getRscp());
            int arfcn = fixIntNull(tdscdmaCell.getBand() != null ? tdscdmaCell.getBand().getDownlinkUarfcn() : null, Cell.UNKNOWN_CID);
            cell.setNetMonsterTdscdmaSignal(asu, dbm, rscp, arfcn);
        } else if (netMonsterCell instanceof CellCdma) {
            CellCdma cdmaCell = (CellCdma) netMonsterCell;
            SignalCdma signal = cdmaCell.getSignal();
            int asu = getCdmaAsuLevel(signal.getCdmaRssi(), signal.getCdmaEcio());
            if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                asu = Cell.UNKNOWN_SIGNAL;
            int dbm = fixIntNull(signal.getDbm());
            int cdmaDbm = fixIntNull(signal.getCdmaRssi());
            int cdmaEcio = fixDoubleNull(signal.getCdmaEcio());
            int evdoDbm = fixIntNull(signal.getEvdoRssi());
            int evdoEcio = fixDoubleNull(signal.getEvdoEcio());
            int evdoSnr = fixIntNull(signal.getEvdoSnr());
            cell.setNetMonsterCdmaSignal(asu, dbm, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr);
        } else {
            throw new UnsupportedOperationException("Cell type not supported `" + netMonsterCell.getClass().getName() + "`");
        }
    }

    private int fixLteRsrpOnSamsung(Integer rsrp) {
        if (rsrp == null)
            return Cell.UNKNOWN_SIGNAL;
        // Hack for Samsung phones which report positive dBm with too large value
        if (rsrp >= -1 && "samsung".equalsIgnoreCase(Build.MANUFACTURER)) {
            rsrp /= -10;
            if (rsrp < -140 || rsrp > -40) {
                rsrp = Cell.UNKNOWN_SIGNAL;
            }
        }
        return rsrp;
    }

    private int fixIntNull(Integer value) {
        return fixIntNull(value, Cell.UNKNOWN_SIGNAL);
    }

    private int fixIntNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value.intValue();
    }

    private int fixDoubleNull(Double value) {
        if (value == null)
            return Cell.UNKNOWN_SIGNAL;
        long roundedValue = Math.round(value);
        if (roundedValue >= Integer.MAX_VALUE || roundedValue <= Integer.MIN_VALUE)
            return Cell.UNKNOWN_SIGNAL;
        return (int) roundedValue;
    }

    /**
     * There is no standard definition of ASU for CDMA; however, Android defines it as the
     * the lesser of the following two results (for 1xRTT): RSSI Range (dBm) and Ec/Io Range (dB)
     * https://android.googlesource.com/platform/frameworks/base/+/master/telephony/java/android/telephony/CellSignalStrengthCdma.java
     */
    public int getCdmaAsuLevel(Integer cdmaDbm, Double cdmaEcio) {
        int cdmaAsuLevel;
        int ecioAsuLevel;
        if (cdmaDbm == null) cdmaAsuLevel = 99;
        else if (cdmaDbm >= -75) cdmaAsuLevel = 16;
        else if (cdmaDbm >= -82) cdmaAsuLevel = 8;
        else if (cdmaDbm >= -90) cdmaAsuLevel = 4;
        else if (cdmaDbm >= -95) cdmaAsuLevel = 2;
        else if (cdmaDbm >= -100) cdmaAsuLevel = 1;
        else cdmaAsuLevel = 99;
        // Ec/Io are in dB*10
        if (cdmaEcio == null) ecioAsuLevel = 99;
        else if (cdmaEcio >= -90) ecioAsuLevel = 16;
        else if (cdmaEcio >= -100) ecioAsuLevel = 8;
        else if (cdmaEcio >= -115) ecioAsuLevel = 4;
        else if (cdmaEcio >= -130) ecioAsuLevel = 2;
        else if (cdmaEcio >= -150) ecioAsuLevel = 1;
        else ecioAsuLevel = 99;
        return Math.min(cdmaAsuLevel, ecioAsuLevel);
    }
}
