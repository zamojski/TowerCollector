/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.tasks;

import org.acra.ACRA;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.io.network.NetworkHelper;
import info.zamojski.soft.towercollector.io.network.NetworkHelper.ResponseData;
import info.zamojski.soft.towercollector.model.UpdateInfo;
import info.zamojski.soft.towercollector.parsers.update.UpdateFeedParseException;
import info.zamojski.soft.towercollector.parsers.update.UpdateFeedParser;
import info.zamojski.soft.towercollector.updater.UpdaterNotificationHelper;
import info.zamojski.soft.towercollector.utils.StringUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import trikita.log.Log;

public class UpdateCheckAsyncTask extends AsyncTask<String, Void, UpdateInfo> {

    private static final String TAG = UpdateCheckAsyncTask.class.getSimpleName();

    public static final String TASK_FULL_NAME = UpdateCheckAsyncTask.class.getCanonicalName();
    public static final String INTENT_KEY_UPDATE_INFO = UpdateInfo.class.getCanonicalName();

    public static final int NOTIFICATION_ID = 'A';

    private UpdaterNotificationHelper notificationHelper;

    private Context context;
    private int currentVersion;
    private UpdateFeedParser responseParser;

    public UpdateCheckAsyncTask(Context context, int currentVersion) {
        this.context = context;
        this.currentVersion = currentVersion;
        responseParser = new UpdateFeedParser();
    }

    @Override
    protected UpdateInfo doInBackground(String... urls) {
        // get update feed url
        String updateFeedUrl = urls[0];
        // check for updates and download info
        ResponseData response = NetworkHelper.sendGet(updateFeedUrl);
        Log.d(TAG, "doInBackground(): Server response: " + response);
        if (response.getCode() == 200 && !StringUtils.isNullEmptyOrWhitespace(response.getContent())) {
            // parse response
            try {
                UpdateInfo updateInfo = responseParser.parse(response.getContent());
                return updateInfo;
            } catch (UpdateFeedParseException ex) {
                Log.w(TAG, "doInBackground(): Cannot parse update feed response");
                MyApplication.getAnalytics().sendException(ex, Boolean.FALSE);
                ACRA.getErrorReporter().handleSilentException(ex);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(UpdateInfo updateInfo) {
        if (updateInfo == null)
            return;
        // check if new version available
        if (currentVersion < updateInfo.getVersionCode()) {
            // display notification
            displayNewVersionAvailableNotification(updateInfo);
        }
    }

    private void displayNewVersionAvailableNotification(UpdateInfo updateInfo) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationHelper = new UpdaterNotificationHelper(context);
        Notification notification = notificationHelper.createNotification(updateInfo);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
