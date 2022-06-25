/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.List;

import cz.mroczis.netmonster.core.INetMonster;
import cz.mroczis.netmonster.core.factory.NetMonsterFactory;
import cz.mroczis.netmonster.core.model.NetMonsterConfig;
import cz.mroczis.netmonster.core.model.cell.ICell;
import info.zamojski.soft.towercollector.collector.validators.CellIdentityValidator;
import timber.log.Timber;

public class MobileUtils {

    public static int[] getMccMncPair(String operatorCode) {
        // mcc and mnc is concatenated in the networkOperatorString (the first 3 chars is the mcc and the last 2 is the mnc)
        if (!TextUtils.isEmpty(operatorCode) && operatorCode.length() > 3) {
            try {
                int mcc = Integer.parseInt(operatorCode.substring(0, 3));
                int mnc = Integer.parseInt(operatorCode.substring(3));
                return new int[]{mcc, mnc};
            } catch (NumberFormatException ex) {
                Timber.e(ex, "getMccMncPair(): Cannot parse network operator codes: %s", operatorCode);
            }
        }
        return null;
    }

    public static boolean isApi17FullyCompatible(Context context) {
        return isApi17CellInfoAvailable(context);
    }

    private static boolean isApi17CellInfoAvailable(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> cells;
        try {
            cells = telephonyManager.getAllCellInfo();
        } catch (SecurityException ex) {
            Timber.d(ex, "isApi17CellInfoAvailable(): Result = location permission is denied");
            return false;
        }
        if (cells == null || cells.size() == 0) {
            Timber.d("isApi17CellInfoAvailable(): Result = no cell info");
            return false;
        }
        CellIdentityValidator validator = new CellIdentityValidator();
        for (CellInfo cell : cells) {
            if (validator.isValid(cell)) {
                Timber.d("isApi17CellInfoAvailable(): Result = true");
                return true;
            }
        }
        Timber.d("isApi17CellInfoAvailable(): Result = false");
        return false;
    }

    public static boolean isApi29Limited() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public static boolean isApi24MultiSimCompatible() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isNetMonsterCoreApiCompatible(Context context) {
        INetMonster netMonster = getNetMonsterCore(context);
        try {
            List<ICell> cells = netMonster.getCells();
            return !cells.isEmpty();
        } catch (SecurityException ex) {
            Timber.d(ex, "isNetMonsterCoreApiCompatible(): Result = location permission is denied");
            return false;
        }
    }

    public static INetMonster getNetMonsterCore(Context context) {
        return NetMonsterFactory.INSTANCE.get(context, new NetMonsterConfig(true));
    }
}
