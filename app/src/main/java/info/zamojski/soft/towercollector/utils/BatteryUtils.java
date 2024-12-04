/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

public class BatteryUtils {

    public static boolean areBatteryOptimizationsEnabled(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            return am.isBackgroundRestricted();
        }

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return (pm != null && !pm.isIgnoringBatteryOptimizations(context.getPackageName()));
    }

    public static boolean isPowerSaveModeEnabled(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null && pm.isPowerSaveMode()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (pm.getLocationPowerSaveMode() != PowerManager.LOCATION_MODE_NO_CHANGE);
            }
            return true;
        }
        return false;
    }

    public static boolean isAllowBackgroundUsageAware() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE; // TODO: API 35+
    }

    public static boolean hasBatterySaverSettings() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }
}
