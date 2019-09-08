/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.updater;

import info.zamojski.soft.towercollector.MainActivity;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.model.UpdateInfo;
import info.zamojski.soft.towercollector.tasks.UpdateCheckAsyncTask;
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

public class UpdaterNotificationHelper extends NotificationHelperBase {

    private Context context;
    NotificationCompat.Builder builder;

    public UpdaterNotificationHelper(Context context) {
        this.context = context;
        this.builder = new NotificationCompat.Builder(context, OTHER_NOTIFICATION_CHANNEL_ID);
    }

    public Notification createNotification(NotificationManager notificationManager, UpdateInfo updateInfo) {
        if (isUsingNotificationChannel()) {
            createNotificationChannel(notificationManager);
        }
        // set style
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setColor(context.getResources().getColor(R.color.ic_notification_background_color));
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        // set intent
        PendingIntent mainActivityIntent = createMainActivityResultIntent(updateInfo);
        builder.setContentIntent(mainActivityIntent);
        // set message
        String notificationText = context.getString(R.string.updater_notification_download_options);
        builder.setContentTitle(context.getString(R.string.updater_notification_title));
        builder.setContentText(notificationText);
        builder.setTicker(context.getString(R.string.updater_notification_ticker));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText));
        return builder.build();
    }

    private PendingIntent createMainActivityResultIntent(UpdateInfo updateInfo) {
        Intent intent = new Intent(MyApplication.getApplication(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(UpdateCheckAsyncTask.TASK_FULL_NAME + "_NID_" + UpdateCheckAsyncTask.NOTIFICATION_ID);
        intent.putExtra(UpdateCheckAsyncTask.INTENT_KEY_UPDATE_INFO, updateInfo);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getApplication(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(
                OTHER_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.other_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
    }
}
