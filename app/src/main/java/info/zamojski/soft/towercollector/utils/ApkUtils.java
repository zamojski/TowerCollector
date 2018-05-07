/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import org.acra.ACRA;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import timber.log.Timber;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.List;


public class ApkUtils {

    public static final String ANDROID_MARKET_STORE_PACKAGE_NAME = "com.google.market";
    public static final String GOOGLE_PLAY_STORE_PACKAGE_NAME = "com.android.vending";

    public static int getApkVersionCode(Context context) {
        return getApkVersionCode(context, context.getPackageName());
    }

    public static int getApkVersionCode(Context context, String packageName) {
        int currentAppVersion = 1;
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            currentAppVersion = pi.versionCode;
        } catch (PackageManager.NameNotFoundException ex) {
            Timber.e(ex, "getApkVersionCode(): Current version number not found");
            MyApplication.getAnalytics().sendException(ex, Boolean.TRUE);
            ACRA.getErrorReporter().handleSilentException(ex);
        }
        return currentAppVersion;
    }

    public static String getApkVersionName(Context context) {
        String currentAppVersion = "";
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            currentAppVersion = pi.versionName;
        } catch (PackageManager.NameNotFoundException ex) {
            Timber.e(ex, "getApkVersionName(): Current version name not found");
            MyApplication.getAnalytics().sendException(ex, Boolean.TRUE);
            ACRA.getErrorReporter().handleSilentException(ex);
        }
        return currentAppVersion;
    }

    public static String getAppId(Context context) {
        String appVersion = getApkVersionName(context).replace(".", "");
        String fullAppId = context.getString(R.string.app_id, appVersion);
        return fullAppId;
    }

    public static String getInstallationInfo(Context context) {
        String appVersion = getApkVersionName(context);
        String androidVersionName = Build.VERSION.RELEASE;
        int androidVersionCode = Build.VERSION.SDK_INT;
        String deviceName = getDeviceName();
        return context.getString(R.string.preferences_email_content, appVersion, androidVersionName, androidVersionCode, deviceName);
    }

    public static String getDeviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    public static boolean isServiceRunning(String serviceClassName) {
        ActivityManager activityManager = (ActivityManager) MyApplication.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void startServiceSafely(Context context, Intent intent) {
        if (MobileUtils.isApi26VersionCompatible()) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
