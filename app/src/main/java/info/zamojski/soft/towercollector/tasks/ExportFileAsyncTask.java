/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.tasks;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.MainActivity.InternalMessageHandler;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.enums.FileType;
import info.zamojski.soft.towercollector.files.FileGeneratorResult;
import info.zamojski.soft.towercollector.files.devices.FileTextDevice;
import info.zamojski.soft.towercollector.files.formatters.csv.CsvExportFormatter;
import info.zamojski.soft.towercollector.files.formatters.csv.CsvUploadFormatter;
import info.zamojski.soft.towercollector.files.generators.wrappers.CsvTextGeneratorWrapper;
import info.zamojski.soft.towercollector.files.generators.wrappers.GpxTextGeneratorWrapper;
import info.zamojski.soft.towercollector.files.generators.wrappers.JsonTextGeneratorWrapper;
import info.zamojski.soft.towercollector.files.generators.wrappers.interfaces.IProgressListener;
import info.zamojski.soft.towercollector.files.generators.wrappers.interfaces.IProgressiveTextGeneratorWrapper;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;
import info.zamojski.soft.towercollector.utils.FileUtils;
import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;


import android.widget.Toast;

public class ExportFileAsyncTask extends AsyncTask<Void, Integer, FileGeneratorResult> implements IProgressListener {

    public static final String ABSOLUTE_PATH = "EXPORTED_FILE_ABSOLUTE_PATH";
    private Context context;
    private FileTextDevice device;
    private IProgressiveTextGeneratorWrapper generatorWrapper;

    private Handler handler;

    private ProgressDialog dialog;

    public ExportFileAsyncTask(Context context, Handler handler, String path, FileType fileType) {
        this.context = context;
        this.device = new FileTextDevice(path);
        this.handler = handler;
        this.generatorWrapper = CreateTextGeneratorWrapper(fileType);
    }

    @Override
    protected void onPreExecute() {
        Timber.d("onPreExecute(): Starting export");
        MyApplication.startBackgroundTask(this);
        generatorWrapper.addProgressListener(this);
    }

    @Override
    protected FileGeneratorResult doInBackground(Void... params) {
        Timber.d("doInBackground(): Running export");
        // set thread name for easier bug tracking in GA
        Thread.currentThread().setName(ExportFileAsyncTask.class.getSimpleName() + ".Worker");
        long startTime = System.currentTimeMillis();
        // run generator
        FileGeneratorResult result = generatorWrapper.generate();
        // send stats
        long endTime = System.currentTimeMillis();
        AnalyticsStatistics stats = MeasurementsDatabase.getInstance(context).getAnalyticsStatistics();
        String fileExt = FileUtils.getFileExtension(device.getPath());
        long duration = (endTime - startTime);
        MyApplication.getAnalytics().sendExportFinished(duration, fileExt, stats);
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        int current = progress[0];
        int max = progress[1];
        if (current == 0) {
            // show loading indicator
            dialog = new ProgressDialog(context);
            dialog.setTitle(R.string.export_dialog_progress_title);
            dialog.setMessage(context.getString(R.string.export_dialog_progress_message));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // cancel generation and it will return that task should be cancelled (anyway we cleanup in onCancelled to be sure)
                    generatorWrapper.cancel();
                }
            });
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMax(max);
            dialog.show();
        } else if (current >= max) {
            // hide loading indicator
            if (dialog != null)
                dialog.dismiss();
        } else {
            if (dialog != null)
                dialog.setProgress(current);
        }
    }

    @Override
    protected void onPostExecute(FileGeneratorResult result) {
        Timber.d("onPostExecute(): Showing result: %s", result);
        MyApplication.stopBackgroundTask();
        generatorWrapper.removeProgressListener(this);
        // check result
        switch (result.getResult()) {
            case NoData:
                Toast.makeText(context, R.string.export_toast_no_data, Toast.LENGTH_LONG).show();
                break;
            case Succeeded:
                // show dialog
                Message msg = new Message();
                msg.what = InternalMessageHandler.EXPORT_FINISHED_UI_REFRESH;
                msg.getData().putString(ABSOLUTE_PATH, device.getPath());
                handler.sendMessage(msg);
                break;
            case Cancelled:
                deleteFile();
                Toast.makeText(context, R.string.export_toast_cancelled, Toast.LENGTH_LONG).show();
                break;
            case Failed:
            case Unknown:
            default:
                deleteFile();
                String causeString = "";
                switch (result.getReason()) {
                    case LocationNotExists:
                        causeString = getStringById(R.string.export_toast_failed_cause_directory_not_exists);
                        break;
                    case LocationNotWritable:
                        causeString = getStringById(R.string.export_toast_failed_cause_directory_not_writable);
                        break;
                    case DeviceNotWritable:
                        causeString = getStringById(R.string.export_toast_failed_cause_file_not_writable);
                        break;
                    case Unknown:
                    default:
                        causeString = result.getMessage();
                        if (StringUtils.isNullEmptyOrWhitespace(causeString)) {
                            causeString = getStringById(R.string.export_toast_failed_cause_unknown);
                        }
                        break;
                }
                Toast.makeText(context, context.getString(R.string.export_toast_failed, causeString), Toast.LENGTH_LONG).show();
                break;
        }
        // hide loading indicator
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    protected void onCancelled() {
        Timber.d("onCancelled(): Export cancelled");
        MyApplication.stopBackgroundTask();
        generatorWrapper.removeProgressListener(this);
        // hide loading indicator
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    public void reportProgress(int value, int max) {
        publishProgress(value, max);
    }

    private IProgressiveTextGeneratorWrapper CreateTextGeneratorWrapper(FileType fileType) {
        switch (fileType) {
            case Csv:
                return new CsvTextGeneratorWrapper(context, device, new CsvExportFormatter());
            case CsvOcid:
                return new CsvTextGeneratorWrapper(context, device, new CsvUploadFormatter());
            case Gpx:
                return new GpxTextGeneratorWrapper(context, device);
            case JsonMls:
                return new JsonTextGeneratorWrapper(context, device);
            default:
                throw new UnsupportedOperationException("This type of file is not supported");
        }
    }

    private void deleteFile() {
        // delete file if exists
        device.close();
        File file = new File(device.getPath());
        if (file.exists()) {
            Timber.d("deleteFile(): Deleting exported file");
            if (file.delete()) {
                Timber.d("deleteFile(): Exported file deleted");
            } else {
                Timber.d("deleteFile(): Cannot delete file after export fail");
            }
        }
    }

    private String getStringById(int resId) {
        return context.getString(resId);
    }
}
