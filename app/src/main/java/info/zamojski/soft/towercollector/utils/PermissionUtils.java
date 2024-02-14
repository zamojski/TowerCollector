/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

import info.zamojski.soft.towercollector.MyApplication;
import timber.log.Timber;

public class PermissionUtils {
    public static boolean hasPermission(Context context, String permission) {
        return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static void openAppSettings(Context context) {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myAppSettings);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotificationPermissionRequired() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    public static String getAppPermissions() {
        Map<String, Boolean> permissions = getAppPermissionsInternal();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Boolean> permission : permissions.entrySet()) {
            sb.append(permission.getKey()).append("=").append(permission.getValue()).append("; ");
        }
        return sb.toString();
    }

    private static Map<String, Boolean> getAppPermissionsInternal() {
        Map<String, Boolean> permissions = new HashMap<>();
        try {
            String packageName = MyApplication.getApplication().getPackageName();
            PackageInfo packageInfo = MyApplication.getApplication().getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
                permissions.put(packageInfo.requestedPermissions[i], isPermissionGranted(packageInfo.requestedPermissionsFlags[i]));
            }
        } catch (Exception ex) {
            Timber.w(ex, "getAppPermissions(): Failed to get app permissions");
        }
        return permissions;
    }

    private static boolean isPermissionGranted(int requestedPermissionsFlags) {
        return (requestedPermissionsFlags & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0;
    }
}
