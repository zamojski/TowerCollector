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
import info.zamojski.soft.towercollector.utils.ApkUtils;
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
            startCollectorServiceFromForeground(context, IntentSource.Application);
        } else if (collectorStopAction.equals(action)) {
            stopCollectorService(context);
        } else if (uploaderStartAction.equals(action)) {
            startUploaderService(context, IntentSource.Application);
        } else if (uploaderStopAction.equals(action)) {
            stopUploaderService(context);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action) || quickBootPowerOnAction.equals(action)) {
            boolean startAtBootEnabled = MyApplication.getPreferencesProvider().getStartCollectorAtBoot();
            if (startAtBootEnabled) {
                startCollectorServiceFromBackground(context, IntentSource.System);
            }
        }
    }

    public void startCollectorServiceFromForeground(Context context, IntentSource source) {
        startCollectorServiceInternal(context, source, false);
    }

    public void startCollectorServiceFromBackground(Context context, IntentSource source) {
        startCollectorServiceInternal(context, source, true);
    }

    private void startCollectorServiceInternal(Context context, IntentSource source, boolean isBackground) {
        if (!canStartBackgroundService(context)) {
            return;
        }
        if (!(isBackground ? hasAllCollectorBackgroundPermissions(context) : hasAllCollectorForegroundPermissions(context))) {
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
        ApkUtils.reportShortcutUsage(context, R.string.shortcut_id_collector_toggle);
    }

    public void stopCollectorService(Context context) {
        Timber.d("stopCollectorService(): Stopping service from broadcast");
        context.stopService(getCollectorIntent(context));
        ApkUtils.reportShortcutUsage(context, R.string.shortcut_id_collector_toggle);
    }

    private Intent getCollectorIntent(Context context, IntentSource source) {
        Intent intent = getCollectorIntent(context);
        intent.putExtra(CollectorService.INTENT_KEY_START_INTENT_SOURCE, source);
        return intent;
    }

    private Intent getCollectorIntent(Context context) {
        return new Intent(context, CollectorService.class);
    }

    public void startUploaderService(Context context, IntentSource source) {
        if (!canStartBackgroundService(context))
            return;
        Timber.d("startCollectorService(): Starting service from broadcast");
        ContextCompat.startForegroundService(context, getUploaderIntent(context, source));
        ApkUtils.reportShortcutUsage(context, R.string.shortcut_id_uploader_toggle);
    }

    public void stopUploaderService(Context context) {
        Timber.d("stopUploaderService(): Stopping service from broadcast");
        // don't use stopService because the worker needs to be stopped first
        Intent stopIntent = new Intent(UploaderService.BROADCAST_INTENT_STOP_SERVICE);
        context.sendBroadcast(stopIntent);
        ApkUtils.reportShortcutUsage(context, R.string.shortcut_id_uploader_toggle);
    }

    private Intent getUploaderIntent(Context context, IntentSource source) {
        Intent intent = new Intent(context, UploaderService.class);
        intent.putExtra(UploaderService.INTENT_KEY_START_INTENT_SOURCE, source);
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

    private boolean hasAllCollectorForegroundPermissions(Context context) {
        return PermissionUtils.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE);
    }

    private boolean hasAllCollectorBackgroundPermissions(Context context) {
        return hasAllCollectorForegroundPermissions(context) && (!GpsUtils.isBackgroundLocationAware() || PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION));
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
