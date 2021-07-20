/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import info.zamojski.soft.towercollector.CollectorService;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.UploaderService;
import info.zamojski.soft.towercollector.export.ExportWorker;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class BackgroundTaskHelper {

    private final Context context;

    public BackgroundTaskHelper(Context context) {
        this.context = context;
    }

    public void showTaskRunningMessage(String taskClassName) {
        int messageId = View.NO_ID;
        if (taskClassName.equals(CollectorService.class.getName())) {
            messageId = R.string.main_toast_background_task_already_running_collector;
        } else if (taskClassName.equals(UploaderService.class.getName())) {
            messageId = R.string.main_toast_background_task_already_running_uploader;
        } else if (taskClassName.equals(ExportWorker.class.getName())) {
            messageId = R.string.main_toast_background_task_already_running_export;
        } else {
            messageId = R.string.main_toast_background_task_already_running_unknown;
        }
        String message = context.getString(messageId);
        Toast.makeText(context.getApplicationContext(), context.getString(R.string.main_toast_background_task_already_running_common, message), Toast.LENGTH_SHORT).show();
    }

}
