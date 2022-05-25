/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.uploader;

import info.zamojski.soft.towercollector.MainActivity;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.broadcast.ExternalBroadcastReceiver;
import info.zamojski.soft.towercollector.utils.NotificationHelperBase;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class UploaderNotificationHelper extends NotificationHelperBase {

    private final Context context;
    private final NotificationCompat.Builder builder;

    public UploaderNotificationHelper(Context context) {
        this.context = context;
        this.builder = new NotificationCompat.Builder(context, UPLOADER_NOTIFICATION_CHANNEL_ID);
    }

    public Notification createNotification(NotificationManager notificationManager) {
        if (isUsingNotificationChannel()) {
            createNotificationChannel(notificationManager);
        }
        String notificationText = context.getString(R.string.uploader_starting);
        return prepareNotification(notificationText);
    }

    public Notification updateNotificationProgress(int progress, int max) {
        String notificationText = context.getString(R.string.uploader_notification_progress_info, progress);
        builder.setContentText(notificationText);
        builder.setProgress(max, progress, false);
        return builder.build();
    }

    private Notification prepareNotification(String notificationText) {
        // set style
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setColor(context.getResources().getColor(R.color.ic_notification_background_color));
        builder.setWhen(System.currentTimeMillis());
        builder.setOnlyAlertOnce(true);
        // set intent
        PendingIntent mainActivityIntent = createOpenMainActivityIntent();
        builder.setContentIntent(mainActivityIntent);
        // set message
        builder.setContentTitle(context.getString(R.string.uploader_notification_title));
        builder.setContentText(notificationText);
        builder.setTicker(notificationText);
        // set action
        PendingIntent cancelUploaderIntent = createCancelUploaderIntent();
        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(R.drawable.menu_stop, context.getString(R.string.dialog_cancel), cancelUploaderIntent).build();
        builder.addAction(stopAction);
        return builder.build();
    }

    private PendingIntent createOpenMainActivityIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(UploaderWorker.SERVICE_FULL_NAME + "_NID_" + UploaderWorker.NOTIFICATION_ID);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return pendingIntent;
    }

    private PendingIntent createCancelUploaderIntent() {
        Intent intent = new Intent(ExternalBroadcastReceiver.UploaderStopAction);
        intent.setPackage(MyApplication.getApplication().getPackageName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pendingIntent;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(
                UPLOADER_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.uploader_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW); // Android will automatically promote to DEFAULT but in case they change their mind I leave it here
        notificationManager.createNotificationChannel(channel);
    }
}
