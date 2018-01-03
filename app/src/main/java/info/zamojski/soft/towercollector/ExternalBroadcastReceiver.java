/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import org.greenrobot.eventbus.EventBus;

import info.zamojski.soft.towercollector.analytics.IntentSource;
import info.zamojski.soft.towercollector.events.CollectorStartedEvent;
import info.zamojski.soft.towercollector.utils.ApkUtils;
import info.zamojski.soft.towercollector.utils.BackgroundTaskHelper;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import info.zamojski.soft.towercollector.utils.PermissionUtils;
import trikita.log.Log;

public class ExternalBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = ExternalBroadcastReceiver.class.getSimpleName();

    private final String quickBootPowerOnAction = "android.intent.action.QUICKBOOT_POWERON";

    private final String collectorStartAction = "info.zamojski.soft.towercollector.COLLECTOR_START";
    private final String collectorStopAction = "info.zamojski.soft.towercollector.COLLECTOR_STOP";

    private final String uploaderStartAction = "info.zamojski.soft.towercollector.UPLOADER_START";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(collectorStartAction)) {
            startCollectorService(context, IntentSource.Application);
        } else if (action.equals(collectorStopAction)) {
            stopCollectorService(context);
        } else if (action.equals(uploaderStartAction)) {
            startUploaderService(context);
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(quickBootPowerOnAction)) {
            boolean startAtBootEnabled = MyApplication.getPreferencesProvider().getStartCollectorAtBoot();
            if (startAtBootEnabled) {
                startCollectorService(context, IntentSource.System);
            }
        }
    }

    private void startCollectorService(Context context, IntentSource source) {
        if (!canStartBackgroundService(context)) {
            return;
        }
        if (!hasAllCollectorRequiredPermissions(context)) {
            showCollectorPermissionsDenied(context);
            return;
        }
        Log.d("startCollectorService(): Starting service from broadcast");
        Intent intent = getCollectorIntent(context);

        ApkUtils.startServiceSafely(context, intent);
        EventBus.getDefault().post(new CollectorStartedEvent(intent));
        MyApplication.getAnalytics().sendCollectorStarted(source);
    }

    private void stopCollectorService(Context context) {
        Log.d("stopCollectorService(): Stopping service from broadcast");
        context.stopService(getCollectorIntent(context));
    }

    private Intent getCollectorIntent(Context context) {
        return new Intent(context, CollectorService.class);
    }

    private void startUploaderService(Context context) {
        if (!canStartBackgroundService(context))
            return;
        Log.d("startCollectorService(): Starting service from broadcast");
        ApkUtils.startServiceSafely(context, getUploaderIntent(context));
        MyApplication.getAnalytics().sendUploadStarted(IntentSource.Application);
    }

    private Intent getUploaderIntent(Context context) {
        return new Intent(context, UploaderService.class);
    }

    private boolean canStartBackgroundService(Context context) {
        String runningTaskClassName = MyApplication.getBackgroundTaskName();
        if (runningTaskClassName != null) {
            Log.d("canStartBackgroundService(): Another task is running in background: %s", runningTaskClassName);
            BackgroundTaskHelper backgroundTaskHelper = new BackgroundTaskHelper(context);
            backgroundTaskHelper.showTaskRunningMessage(runningTaskClassName);
            return false;
        }
        return true;
    }

    private boolean hasAllCollectorRequiredPermissions(Context context) {
        return PermissionUtils.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE);
    }

    private void showCollectorPermissionsDenied(Context context) {
        Log.d("showCollectorPermissionsDenied(): Cannot start collector due to denied permissions");
        Toast.makeText(context, R.string.permission_collector_denied_intent_message, Toast.LENGTH_LONG).show();
    }
}
