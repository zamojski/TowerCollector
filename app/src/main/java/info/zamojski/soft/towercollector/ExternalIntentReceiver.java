/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import de.greenrobot.event.EventBus;
import info.zamojski.soft.towercollector.analytics.IntentSource;
import info.zamojski.soft.towercollector.events.CollectorStartedEvent;
import info.zamojski.soft.towercollector.utils.BackgroundTaskHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ExternalIntentReceiver extends BroadcastReceiver {

    private static final String TAG = ExternalIntentReceiver.class.getSimpleName();

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
        if (canStartBackgroundService(context)) {
            Log.d(TAG, "startCollectorService(): Starting service from broadcast");
            Intent intent = getCollectorIntent(context);
            context.startService(intent);
            EventBus.getDefault().post(new CollectorStartedEvent(intent));
            MyApplication.getAnalytics().sendCollectorStarted(source);
        }
    }

    private void stopCollectorService(Context context) {
        Log.d(TAG, "stopCollectorService(): Stopping service from broadcast");
        context.stopService(getCollectorIntent(context));
    }

    private Intent getCollectorIntent(Context context) {
        return new Intent(context, CollectorService.class);
    }

    private void startUploaderService(Context context) {
        if (canStartBackgroundService(context)) {
            Log.d(TAG, "startCollectorService(): Starting service from broadcast");
            context.startService(getUploaderIntent(context));
            MyApplication.getAnalytics().sendUploadStarted(IntentSource.Application);
        }
    }

    private Intent getUploaderIntent(Context context) {
        return new Intent(context, UploaderService.class);
    }

    private boolean canStartBackgroundService(Context context) {
        String runningTaskClassName = MyApplication.getBackgroundTaskName();
        if (runningTaskClassName != null) {
            Log.d(TAG, "canStartBackgroundService(): Another task is running in background: " + runningTaskClassName);
            BackgroundTaskHelper backgroundTaskHelper = new BackgroundTaskHelper(context);
            backgroundTaskHelper.showTaskRunningMessage(runningTaskClassName);
            return false;
        }
        return true;
    }
}
