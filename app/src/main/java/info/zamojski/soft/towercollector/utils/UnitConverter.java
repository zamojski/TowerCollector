/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.telephony.NeighboringCellInfo;

import info.zamojski.soft.towercollector.model.Cell;

public class UnitConverter {
    private static final float METERS_TO_FEET_MULTIPLIER = 3.280839895013123f;

    public static float convertMetersToFeet(float meters) {
        return (meters * METERS_TO_FEET_MULTIPLIER);
    }

    public static int convertGsmAsuToDbm(int asu) {
        if (asu == Cell.UNKNOWN_SIGNAL || asu == NeighboringCellInfo.UNKNOWN_RSSI)
            return Cell.UNKNOWN_SIGNAL;
        return 2 * asu - 113;
    }

    // ranges taken from 5.0.0 android/telephony/CellSignalStrength.java#CellSignalStrength.getAsuFromRssiDbm()
    public static int convertGsmDbmToAsu(int dbm) {
        if (dbm == Cell.UNKNOWN_SIGNAL || dbm == NeighboringCellInfo.UNKNOWN_RSSI)
            return Cell.UNKNOWN_SIGNAL;
        if (dbm <= -113)
            return 0;
        int asu = (dbm + 113) / 2;
        if (asu > 31)
            return 31;
        return asu;
    }

    public static int convertLteAsuToDbm(int asu) {
        if (asu == Cell.UNKNOWN_SIGNAL || asu == NeighboringCellInfo.UNKNOWN_RSSI)
            return Cell.UNKNOWN_SIGNAL;
        if (asu <= 0)
            return -140;
        if (asu >= 97)
            return -43;
        return asu - 140;
    }

    // converted from 5.0.0 android/telephony/CellSignalStrengthLte.java#CellSignalStrengthLte.getAsuLevel()
    public static int convertLteDbmToAsu(int dbm) {
        if (dbm == Cell.UNKNOWN_SIGNAL || dbm == NeighboringCellInfo.UNKNOWN_RSSI)
            return Cell.UNKNOWN_SIGNAL;
        if (dbm <= -140)
            return 0;
        if (dbm >= -43)
            return 97;
        return dbm + 140;
    }

    public static int convertCdmaDbmToAsu(int dbm) {
        if (dbm >= -75)
            return 16;
        else if (dbm >= -82)
            return 8;
        else if (dbm >= -90)
            return 4;
        else if (dbm >= -95)
            return 2;
        else if (dbm >= -100)
            return 1;
        return Cell.UNKNOWN_SIGNAL;
    }
}
