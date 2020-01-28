/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.broadcast;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.greenrobot.eventbus.EventBus;

import info.zamojski.soft.towercollector.CollectorService;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.UploaderService;
import info.zamojski.soft.towercollector.analytics.IntentSource;
import info.zamojski.soft.towercollector.events.CollectorStartedEvent;
import info.zamojski.soft.towercollector.utils.BackgroundTaskHelper;
import info.zamojski.soft.towercollector.utils.GpsUtils;
import info.zamojski.soft.towercollector.utils.PermissionUtils;
import timber.log.Timber;

public class ExternalBroadcastReceiver extends BroadcastReceiver {

    private static final String quickBootPowerOnAction = "android.intent.action.QUICKBOOT_POWERON";

    private static final String collectorStartAction = "info.zamojski.soft.towercollector.COLLECTOR_START";
    private static final String collectorStopAction = "info.zamojski.soft.towercollector.COLLECTOR_STOP";

    private static final String uploaderStartAction = "info.zamojski.soft.towercollector.UPLOADER_START";
    private static final String uploaderStopAction = "info.zamojski.soft.towercollector.UPLOADER_STOP";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (collectorStartAction.equals(action)) {
            startCollectorService(context, IntentSource.Application);
        } else if (collectorStopAction.equals(action)) {
            stopCollectorService(context);
        } else if (uploaderStartAction.equals(action)) {
            startUploaderService(context);
        } else if (uploaderStopAction.equals(action)) {
            stopUploaderService(context);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action) || quickBootPowerOnAction.equals(action)) {
            boolean startAtBootEnabled = MyApplication.getPreferencesProvider().getStartCollectorAtBoot();
            if (startAtBootEnabled) {
                startCollectorService(context, IntentSource.System);
            }
        }
    }

    public void startCollectorService(Context context, IntentSource source) {
        if (!canStartBackgroundService(context)) {
            return;
        }
        if (!hasAllCollectorRequiredPermissions(context)) {
            showCollectorPermissionsDenied(context);
            return;
        }
        if (!GpsUtils.isGpsEnabled(context)) {
            showGpsNotAvailable(context);
            return;
        }
        Timber.d("startCollectorService(): Starting service from broadcast");
        Intent intent = getCollectorIntent(context, source);

        ContextCompat.startForegroundService(context, intent);
        EventBus.getDefault().post(new CollectorStartedEvent(intent));
    }

    public void stopCollectorService(Context context) {
        Timber.d("stopCollectorService(): Stopping service from broadcast");
        context.stopService(getCollectorIntent(context));
    }

    private Intent getCollectorIntent(Context context, IntentSource source) {
        Intent intent = getCollectorIntent(context);
        intent.putExtra(CollectorService.INTENT_KEY_START_INTENT_SOURCE, source);
        return intent;
    }

    private Intent getCollectorIntent(Context context) {
        return new Intent(context, CollectorService.class);
    }

    public void startUploaderService(Context context) {
        if (!canStartBackgroundService(context))
            return;
        Timber.d("startCollectorService(): Starting service from broadcast");
        ContextCompat.startForegroundService(context, getUploaderIntent(context));
    }

    public void stopUploaderService(Context context) {
        Timber.d("stopUploaderService(): Stopping service from broadcast");
        // don't use stopService because the worker needs to be stopped first
        Intent stopIntent = new Intent(UploaderService.BROADCAST_INTENT_STOP_SERVICE);
        context.sendBroadcast(stopIntent);
    }

    private Intent getUploaderIntent(Context context) {
        Intent intent = new Intent(context, UploaderService.class);
        intent.putExtra(UploaderService.INTENT_KEY_START_INTENT_SOURCE, IntentSource.Application);
        return intent;
    }

    private boolean canStartBackgroundService(Context context) {
        String runningTaskClassName = MyApplication.getBackgroundTaskName();
        if (runningTaskClassName != null) {
            Timber.d("canStartBackgroundService(): Another task is running in background: %s", runningTaskClassName);
            BackgroundTaskHelper backgroundTaskHelper = new BackgroundTaskHelper(context);
            backgroundTaskHelper.showTaskRunningMessage(runningTaskClassName);
            return false;
        }
        return true;
    }

    private boolean hasAllCollectorRequiredPermissions(Context context) {
        if (GpsUtils.isBackgroundLocationAware()) {
            return PermissionUtils.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.READ_PHONE_STATE);
        } else {
            return PermissionUtils.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE);
        }
    }

    private void showCollectorPermissionsDenied(Context context) {
        Timber.d("showCollectorPermissionsDenied(): Cannot start collector due to denied permissions");
        Toast.makeText(context, R.string.permission_collector_denied_intent_message, Toast.LENGTH_LONG).show();
    }

    private void showGpsNotAvailable(Context context) {
        Timber.d("showGpsNotAvailable(): Cannot start collector because GPS is not available");
        Toast.makeText(context, R.string.collector_gps_unavailable, Toast.LENGTH_LONG).show();
    }
}
