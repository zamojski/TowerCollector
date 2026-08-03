/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import info.zamojski.soft.towercollector.CollectorService;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.broadcast.ExternalBroadcastReceiver;
import info.zamojski.soft.towercollector.export.ExportWorker;
import info.zamojski.soft.towercollector.uploader.UploaderWorker;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class BackgroundTaskHelper {

    private final Context context;

    public BackgroundTaskHelper(Context context) {
        this.context = context;
    }

    public void showTaskRunningMessage(String taskClassName) {
        showTaskRunningMessage(null, taskClassName);
    }

    public void showTaskRunningMessage(View view, String taskClassName) {
        int messageId;
        View.OnClickListener stopActionListener = null;

        if (taskClassName.equals(CollectorService.class.getName())) {
            messageId = R.string.main_toast_background_task_already_running_collector;
            stopActionListener = v -> ExternalBroadcastReceiver.stopCollectorService(context);
        } else if (taskClassName.equals(UploaderWorker.class.getName())) {
            messageId = R.string.main_toast_background_task_already_running_uploader;
            stopActionListener = v -> ExternalBroadcastReceiver.stopUploaderWorker(context);
        } else if (taskClassName.equals(ExportWorker.class.getName())) {
            messageId = R.string.main_toast_background_task_already_running_export;
            stopActionListener = v -> ExternalBroadcastReceiver.stopExportWorker(context);
        } else {
            messageId = R.string.main_toast_background_task_already_running_unknown;
        }

        String taskName = context.getString(messageId);
        String fullMessage = context.getString(R.string.main_toast_background_task_already_running_common, taskName);

        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, fullMessage, Snackbar.LENGTH_LONG);
            if (stopActionListener != null) {
                snackbar.setAction(R.string.dialog_stop, stopActionListener);
            }
            snackbar.show();
        } else {
            Toast.makeText(context.getApplicationContext(), fullMessage, Toast.LENGTH_SHORT).show();
        }
    }

}
