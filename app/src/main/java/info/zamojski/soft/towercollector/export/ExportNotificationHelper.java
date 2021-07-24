/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.export;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.utils.NotificationHelperBase;

public class ExportNotificationHelper extends NotificationHelperBase {

    private Context context;
    private NotificationCompat.Builder builder;

    public ExportNotificationHelper(Context context) {
        this.context = context;
        this.builder = new NotificationCompat.Builder(context, EXPORT_NOTIFICATION_CHANNEL_ID);
    }

    public Notification createNotification(NotificationManager notificationManager) {
        if (isUsingNotificationChannel()) {
            createNotificationChannel(notificationManager);
        }
        String notificationText = context.getString(R.string.export_starting);
        return prepareNotification(notificationText);
    }

    public Notification updateNotificationProgress(int progress, int max) {
        String notificationText = context.getString(R.string.export_notification_progress_info, progress);
        builder.setContentText(notificationText);
        builder.setProgress(max, progress, false);
        return builder.build();
    }

    public Notification updateNotificationCancelling() {
        builder.setWhen(System.currentTimeMillis());
        String notificationText = context.getString(R.string.export_aborting);
        builder.setContentText(notificationText);
        builder.setTicker(notificationText);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText));
        return builder.build();
    }

    private Notification prepareNotification(String notificationText) {
        // set style
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setColor(context.getResources().getColor(R.color.ic_notification_background_color));
        builder.setWhen(System.currentTimeMillis());
        builder.setOnlyAlertOnce(true);
        // set message
        builder.setContentTitle(context.getString(R.string.export_notification_title));
        builder.setContentText(notificationText);
        builder.setTicker(notificationText);
        return builder.build();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(
                EXPORT_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.export_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW); // Android will automatically promote to DEFAULT but in case they change their mind I leave it here
        notificationManager.createNotificationChannel(channel);
    }
}
