/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import org.acra.ACRA;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

public class ApkUtils {
    private static final String TAG = ApkUtils.class.getSimpleName();

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
            Log.e(TAG, "getApkVersionCode(): Current version number not found", ex);
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
            Log.e(TAG, "getApkVersionName(): Current version name not found", ex);
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

    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException ex) {
            return false;
        }
        return true;
    }

    public static String getDeviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }
}
