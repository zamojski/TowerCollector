/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.converters;

import java.lang.reflect.Method;

import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.utils.UnitConverter;

import android.telephony.NeighboringCellInfo;
import android.telephony.SignalStrength;
import android.util.Log;

public class CellLocationSignalConverter {

    private static final String TAG = CellLocationSignalConverter.class.getSimpleName();

    private Method lteDbmMethod;

    // http://www.truiton.com/2014/08/android-onsignalstrengthschanged-lte-strength-measurement/
    public void update(Measurement m, SignalStrength signal) {
        if (signal.isGsm()) {
            int asu = signal.getGsmSignalStrength();// asu
            updateGsm(m, asu);
        } else {
            int dbm = signal.getCdmaDbm();// rssi
            updateCdma(m, dbm);
        }
        // try to convert if lte network
        if (m.getAsu() == Measurement.UNKNOWN_SIGNAL) {
            try {
                Method method = getLteDbmMethod();
                int dbm = ((Integer) method.invoke(signal)).intValue();// rsrp
                updateLte(m, dbm);
            } catch (Exception ex) {
                Log.w(TAG, "update(): Cannot read LTE signal strength: " + signal, ex);
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
        Log.d(TAG, "update(): Clearing signal strength");
        updateGsm(m, Measurement.UNKNOWN_SIGNAL);
    }

    private void updateGsm(Measurement m, int asu) {
        Log.d(TAG, "update(): Updating GSM signal strength = " + asu);
        if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
            asu = Measurement.UNKNOWN_SIGNAL;
        m.setGsmLocationSignal(asu, UnitConverter.convertGsmAsuToDbm(asu));
    }

    private void updateCdma(Measurement m, int dbm) {
        Log.d(TAG, "update(): Updating CDMA signal strength = " + dbm);
        m.setCdmaLocationSignal(UnitConverter.convertCdmaDbmToAsu(dbm), dbm);
    }

    private void updateLte(Measurement m, int dbm) {
        Log.d(TAG, "update(): Updating LTE signal strength = " + dbm);
        if (dbm == NeighboringCellInfo.UNKNOWN_RSSI)
            dbm = Measurement.UNKNOWN_SIGNAL;
        m.setGsmLocationSignal(UnitConverter.convertLteDbmToAsu(dbm), dbm);
    }

    private Method getLteDbmMethod() throws NoSuchMethodException {
        if (lteDbmMethod == null) {
            lteDbmMethod = SignalStrength.class.getMethod("getLteDbm", new Class[0]);
        }
        return lteDbmMethod;
    }
}
