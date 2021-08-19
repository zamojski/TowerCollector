/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.export;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import info.zamojski.soft.towercollector.ExportQuickSettingsTileService;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.analytics.IntentSource;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.enums.FileType;
import info.zamojski.soft.towercollector.files.FileGeneratorResult;
import info.zamojski.soft.towercollector.files.formatters.csv.CsvExportFormatter;
import info.zamojski.soft.towercollector.files.formatters.csv.CsvUploadFormatter;
import info.zamojski.soft.towercollector.files.formatters.gpx.GpxExportFormatter;
import info.zamojski.soft.towercollector.files.formatters.json.JsonMozillaExportFormatter;
import info.zamojski.soft.towercollector.files.formatters.kml.KmlExportFormatter;
import info.zamojski.soft.towercollector.files.generators.wrappers.CompositeTextGeneratorWrapper;
import info.zamojski.soft.towercollector.files.generators.wrappers.CsvTextGeneratorWrapper;
import info.zamojski.soft.towercollector.files.generators.wrappers.GpxTextGeneratorWrapper;
import info.zamojski.soft.towercollector.files.generators.wrappers.JsonTextGeneratorWrapper;
import info.zamojski.soft.towercollector.files.generators.wrappers.KmlTextGeneratorWrapper;
import info.zamojski.soft.towercollector.files.generators.wrappers.interfaces.IProgressListener;
import info.zamojski.soft.towercollector.files.generators.wrappers.interfaces.IProgressiveTextGeneratorWrapper;
import info.zamojski.soft.towercollector.io.filesystem.CompressionFormat;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;
import info.zamojski.soft.towercollector.utils.FileUtils;
import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

public class ExportWorker extends Worker implements IProgressListener {

    public static final String SERVICE_FULL_NAME = ExportWorker.class.getCanonicalName();
    public static final String BROADCAST_INTENT_STOP_SERVICE = SERVICE_FULL_NAME + ".ExportStop";
    public static final int NOTIFICATION_ID = 'E';

    public static final String PROGRESS = "PROGRESS";
    public static final String PROGRESS_MAX = "PROGRESS_MAX";
    public static final int PROGRESS_MIN_VALUE = 0;
    public static final int PROGRESS_MAX_VALUE = 100;
    public static final String SELECTED_FILE_TYPES = "SELECTED_FILE_TYPES";
    public static final String INTENT_SOURCE = "INTENT_SOURCE";
    public static final String MESSAGE = "MESSAGE";
    public static final String DIR_PATH = "EXPORTED_DIR_PATH";
    public static final String FILE_PATHS = "EXPORTED_FILE_PATHS";
    public static final String WORKER_TAG = "EXPORT_WORKER";

    private Uri storageUri;
    private CompositeTextGeneratorWrapper generator;
    private IntentSource intentSource;

    private final NotificationManager notificationManager;
    private final ExportNotificationHelper notificationHelper;

    public ExportWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationHelper = new ExportNotificationHelper(MyApplication.getApplication());
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Notification notification = notificationHelper.createNotification(notificationManager);
            ForegroundInfo foregroundInfo = new ForegroundInfo(NOTIFICATION_ID, notification);
            setForegroundAsync(foregroundInfo);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ExportQuickSettingsTileService.requestTileUpdate(true);
            }

            List<FileType> fileTypes = Arrays.asList(FileType.valuesOf(getInputData().getStringArray(SELECTED_FILE_TYPES)));
            storageUri = MyApplication.getPreferencesProvider().getStorageUri();
            intentSource = IntentSource.valueOf(getInputData().getString(INTENT_SOURCE));
            CreateGenerators(fileTypes);

            Timber.d("doWork(): Starting export");
            MyApplication.startBackgroundTask(this);
            generator.addProgressListener(this);

            Timber.d("doWork(): Running export");
            long startTime = System.currentTimeMillis();
            // run generator
            FileGeneratorResult result = generator.generate();
            // send stats
            long endTime = System.currentTimeMillis();
            AnalyticsStatistics stats = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getAnalyticsStatistics();
            long duration = (endTime - startTime);
            MyApplication.getAnalytics().sendExportFinishedTotal(duration, generator.getSubGenerators().size(), stats);

            Timber.d("doWork(): Showing result: %s", result);
            MyApplication.stopBackgroundTask();
            generator.removeProgressListener(this);

            // check result
            switch (result.getResult()) {
                case NoData:
                    return Result.failure(getMessageData(getStringById(R.string.export_toast_no_data)));
                case Succeeded:
                    Data resultData = new Data.Builder()
                            .putString(DIR_PATH, storageUri.getPath())
                            .putStringArray(FILE_PATHS, getGeneratedFiles())
                            .build();
                    return Result.success(resultData);
                case Cancelled:
                    deleteFile();
                    return Result.failure(getMessageData(getStringById(R.string.export_toast_cancelled))); // this message is not delivered if the Worker is cancelled
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
                    return Result.failure(getMessageData(getStringById(R.string.export_toast_failed, causeString)));
            }
        } catch (Exception ex) {
            Timber.e(ex, "doWork(): Export failed");
            return Result.failure();
        } finally {
            MyApplication.stopBackgroundTask();
            generator.removeProgressListener(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ExportQuickSettingsTileService.requestTileUpdate(false);
            }
        }
    }

    @Override
    public void onStopped() {
        Timber.d("onStopped(): Export cancelled");
        Notification notification = notificationHelper.updateNotificationCancelling();
        notificationManager.notify(NOTIFICATION_ID, notification);
        generator.cancel();
        notificationManager.cancel(NOTIFICATION_ID);
        super.onStopped();
    }

    @Override
    public void reportProgress(int value, int max) {
        if (isStopped())
            return;
        setProgressAsync(new Data.Builder()
                .putInt(PROGRESS, value)
                .putInt(PROGRESS_MAX, max)
                .build());
        Notification notification = notificationHelper.updateNotificationProgress(value, max);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void CreateGenerators(List<FileType> fileTypes) {
        boolean compressFiles = fileTypes.contains(FileType.Compress);
        fileTypes.remove(FileType.Compress); // not a separate format
        List<IProgressiveTextGeneratorWrapper> subGenerators = new ArrayList<>();
        Date currentDateTime = new Date();
        CompressionFormat compressionFormat = getCompressionFormat(compressFiles);
        String compressedExtension = getCompressedExtension(compressionFormat);
        for (FileType fileType : fileTypes) {
            switch (fileType) {
                case Csv: {
                    String fileName = FileUtils.getCurrentDateFileName(currentDateTime, "", "csv");
                    subGenerators.add(new CsvTextGeneratorWrapper(storageUri, fileName, compressedExtension, compressionFormat, new CsvExportFormatter()));
                    break;
                }
                case CsvOcid: {
                    String fileName = FileUtils.getCurrentDateFileName(currentDateTime, "-ocid", "csv");
                    subGenerators.add(new CsvTextGeneratorWrapper(storageUri, fileName, compressedExtension, compressionFormat, new CsvUploadFormatter()));
                }
                break;
                case Gpx: {
                    String fileName = FileUtils.getCurrentDateFileName(currentDateTime, "", "gpx");
                    subGenerators.add(new GpxTextGeneratorWrapper(storageUri, fileName, compressedExtension, compressionFormat, new GpxExportFormatter()));
                }
                break;
                case JsonMls: {
                    String fileName = FileUtils.getCurrentDateFileName(currentDateTime, "-mls", "json");
                    subGenerators.add(new JsonTextGeneratorWrapper(storageUri, fileName, compressedExtension, compressionFormat, new JsonMozillaExportFormatter()));
                }
                break;
                case Kml: {
                    String fileName = FileUtils.getCurrentDateFileName(currentDateTime, "", "kml");
                    subGenerators.add(new KmlTextGeneratorWrapper(storageUri, fileName, compressedExtension, compressionFormat, new KmlExportFormatter()));
                }
                break;
                case Kmz: {
                    String fileName = FileUtils.getCurrentDateFileName(currentDateTime, "", "kmz");
                    subGenerators.add(new KmlTextGeneratorWrapper(storageUri, fileName, null, CompressionFormat.Zip, new KmlExportFormatter()));
                }
                break;
                default:
                    throw new UnsupportedOperationException("This file type " + fileType + " is not supported");
            }
        }
        generator = new CompositeTextGeneratorWrapper(subGenerators, intentSource);
    }

    private CompressionFormat getCompressionFormat(boolean compressFiles) {
        if (compressFiles) {
            String compressionFormat = MyApplication.getPreferencesProvider().getExportCompressionFormat();
            if (getStringById(R.string.preferences_export_compression_format_entries_value_zip).equals(compressionFormat)) {
                return CompressionFormat.Zip;
            } else if (getStringById(R.string.preferences_export_compression_format_entries_value_gzip).equals(compressionFormat)) {
                return CompressionFormat.GZip;
            }
        }
        return CompressionFormat.None;
    }

    private String getCompressedExtension(CompressionFormat compressionFormat) {
        switch (compressionFormat) {
            case Zip:
                return "zip";
            case GZip:
                return "gz";
            default:
                return null; // no compression
        }
    }

    private void deleteFile() {
        for (IProgressiveTextGeneratorWrapper subGenerator : generator.getSubGenerators()) {
            // delete file if exists
            Uri fullPath = subGenerator.getFullPath();
            if (fullPath != null) {
                DocumentFile file = DocumentFile.fromSingleUri(MyApplication.getApplication(), fullPath);
                if (file.exists()) {
                    Timber.d("deleteFile(): Deleting exported file");
                    if (file.delete()) {
                        Timber.d("deleteFile(): Exported file deleted");
                    } else {
                        Timber.d("deleteFile(): Cannot delete file after export failure");
                    }
                }
            }
        }
    }

    private String[] getGeneratedFiles() {
        ArrayList<String> result = new ArrayList<>();
        for (IProgressiveTextGeneratorWrapper subGenerator : generator.getSubGenerators()) {
            Uri fullPath = subGenerator.getFullPath();
            if (fullPath != null && DocumentFile.fromSingleUri(MyApplication.getApplication(), fullPath).exists()) {
                result.add(fullPath.toString());
            }
        }
        return result.toArray(new String[0]);
    }

    private String getStringById(@StringRes int resId, Object... params) {
        return MyApplication.getApplication().getString(resId, params);
    }

    private Data getMessageData(String message) {
        return new Data.Builder()
                .putString(MESSAGE, message)
                .build();
    }
}
