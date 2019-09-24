/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.Manifest;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;

import androidx.core.content.PermissionChecker;

public class GpsUtils {

    public static boolean isGpsEnabled(Context context) {
        if (PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return locationManager.isLocationEnabled();
            } else {
                return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }

        }
        // to cover the case when permission denied on API lower than 21
        return false;
    }
}
