/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Patterns;

public class NetworkUtils {
    public static String getNetworkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null)
            return "NO CONNECTIVITY";
        NetworkInfo netInfo = manager.getActiveNetworkInfo();
        if (netInfo == null)
            return "NO ACTIVE NETWORK";
        return netInfo.getTypeName().toUpperCase();
    }

    public static boolean isNetworkAvailable(Context context) {
        if (PermissionUtils.hasPermissions(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNetwork = connectivityManager.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                        return true;
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                        return true;
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                        return true;
                }
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
            }
        }
        return false;
    }

    public static boolean isInAirplaneMode(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    public static boolean isValidUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }
}
