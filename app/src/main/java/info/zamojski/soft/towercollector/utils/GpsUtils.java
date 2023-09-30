/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.Manifest;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;

public class GpsUtils {

    public static boolean isPreciseLocationAware() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    public static boolean isBackgroundLocationAware() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public static boolean isBackgroundLocationPermissionHidden() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    public static boolean isGpsEnabled(Context context) {
        if (hasGpsPermissions(context)) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return isEnabled && locationManager.isLocationEnabled();
            }
            return isEnabled;
        }
        // to cover the case when permission denied on API lower than 21
        return false;
    }

    public static boolean hasGpsPermissions(Context context) {
        return PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }
}
