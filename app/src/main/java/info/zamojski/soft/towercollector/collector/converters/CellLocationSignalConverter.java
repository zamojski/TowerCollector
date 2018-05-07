/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.converters;

import java.lang.reflect.Method;

import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.utils.UnitConverter;
import timber.log.Timber;

import android.telephony.NeighboringCellInfo;
import android.telephony.SignalStrength;


public class CellLocationSignalConverter {


    private Method lteDbmMethod;
    private Method lteSignalStrengthMethod;
    private Method wcdmaRscpMethod;
    private Method wcdmaEcioMethod;

    // http://www.truiton.com/2014/08/android-onsignalstrengthschanged-lte-strength-measurement/
    public void update(Measurement m, SignalStrength signal) {
        if (signal.isGsm()) {
            int asu = signal.getGsmSignalStrength();// asu
            updateGsm(m, asu);
        } else {
            int dbm = signal.getCdmaDbm();// rssi
            updateCdma(m, dbm);
        }
        // try to convert if lte network or network being reported as GSM is in fact WCDMA
        if (m.getAsu() == Measurement.UNKNOWN_SIGNAL || m.getAsu() == 0) {
            try {
                Method rscpMethod = getWcdmaRscpMethod();
                int rscp = (Integer) rscpMethod.invoke(signal);// rscp
                Method ecioMethod = getWcdmaEcioMethod();
                int ecio = (Integer) ecioMethod.invoke(signal);// ecio
                int rssi = rscp - ecio;// rssi
                updateGsm(m, rssi);
            } catch (Exception ex) {
                Timber.w(ex, "update(): Cannot read WCDMA signal strength from RSCP/ECIO: %s", signal);
            }
        }
        // try to convert if lte network or network being reported as GSM is in fact LTE
        if (m.getAsu() == Measurement.UNKNOWN_SIGNAL || m.getAsu() == 0) {
            try {
                Method dbmMethod = getLteDbmMethod();
                int dbm = (Integer) dbmMethod.invoke(signal);// rsrp
                updateLte(m, dbm);
            } catch (Exception ex) {
                Timber.w(ex, "update(): Cannot read LTE signal strength from RSRP: %s", signal);
            }
        }
        if (m.getAsu() == Measurement.UNKNOWN_SIGNAL || m.getAsu() == 0) {
            try {
                Method strengthMethod = getLteSignalStrengthMethod();
                int asu = (Integer) strengthMethod.invoke(signal);// asu
                updateLte(m, asu);
            } catch (Exception ex) {
                Timber.w(ex, "update(): Cannot read LTE signal strength from ASU: %s", signal);
            }
        }
    }

    public void update(Measurement m, int asu) {
        if (m.getNetworkType() == NetworkGroup.Gsm || m.getNetworkType() == NetworkGroup.Wcdma) {
            if (asu < 0) {
                // NOTE: some devices return ASU but some RSSI
                asu = UnitConverter.convertGsmDbmToAsu(asu);
            }
            updateGsm(m, asu);
            return;
        }
        // no way to detect lte so clear current
        Timber.d("update(): Clearing signal strength");
        updateGsm(m, Measurement.UNKNOWN_SIGNAL);
    }

    private void updateGsm(Measurement m, int asu) {
        Timber.d("update(): Updating GSM signal strength = %s", asu);
        if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
            asu = Measurement.UNKNOWN_SIGNAL;
        // NOTE: for GSM asu is always positive but RSSI negative
        if (asu >= 0)
            m.setGsmLocationSignal(asu, UnitConverter.convertGsmAsuToDbm(asu));
        else
            m.setGsmLocationSignal(UnitConverter.convertGsmDbmToAsu(asu), asu);
    }

    private void updateCdma(Measurement m, int dbm) {
        Timber.d("update(): Updating CDMA signal strength = %s", dbm);
        m.setCdmaLocationSignal(UnitConverter.convertCdmaDbmToAsu(dbm), dbm);
    }

    private void updateLte(Measurement m, int dbm) {
        Timber.d("update(): Updating LTE signal strength = %s", dbm);
        if (dbm == NeighboringCellInfo.UNKNOWN_RSSI)
            dbm = Measurement.UNKNOWN_SIGNAL;
        // NOTE: for LTE asu is always positive but RSRP negative
        if (dbm >= 0)
            m.setGsmLocationSignal(dbm, UnitConverter.convertLteAsuToDbm(dbm));
        else
            m.setGsmLocationSignal(UnitConverter.convertLteDbmToAsu(dbm), dbm);
    }

    private Method getLteDbmMethod() throws NoSuchMethodException {
        if (lteDbmMethod == null) {
            //getwcdmarscp
            lteDbmMethod = SignalStrength.class.getMethod("getLteDbm", new Class[0]);
        }
        return lteDbmMethod;
    }

    private Method getLteSignalStrengthMethod() throws NoSuchMethodException {
        if (lteSignalStrengthMethod == null) {
            lteSignalStrengthMethod = SignalStrength.class.getMethod("getLteSignalStrength", new Class[0]);
        }
        return lteSignalStrengthMethod;
    }

    public Method getWcdmaRscpMethod() throws NoSuchMethodException {
        if (wcdmaRscpMethod == null) {
            wcdmaRscpMethod = SignalStrength.class.getMethod("getWcdmaRscp", new Class[0]);
        }
        return wcdmaRscpMethod;
    }

    public Method getWcdmaEcioMethod() throws NoSuchMethodException {
        if (wcdmaEcioMethod == null) {
            wcdmaEcioMethod = SignalStrength.class.getMethod("getWcdmaEcio", new Class[0]);
        }
        return wcdmaEcioMethod;
    }
}
