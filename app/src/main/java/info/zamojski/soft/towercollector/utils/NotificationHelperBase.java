/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.app.PendingIntent;
import android.os.Build;

public class NotificationHelperBase {

    protected static final String COLLECTOR_NOTIFICATION_CHANNEL_ID = "collector_notification_channel";
    protected static final String UPLOADER_NOTIFICATION_CHANNEL_ID = "uploader_notification_channel";
    protected static final String EXPORT_NOTIFICATION_CHANNEL_ID = "export_notification_channel";
    protected static final String OTHER_NOTIFICATION_CHANNEL_ID = "other_notification_channel";

    protected boolean isUsingNotificationChannel() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
    }

    protected boolean isUsingNotificationPriority() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
    }

    protected int getImmutablePendingIntentFlags(int flags) {
        return isUsingPendingIntentMutabilityFlag() ? flags | PendingIntent.FLAG_IMMUTABLE : flags;
    }

    private boolean isUsingPendingIntentMutabilityFlag() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
