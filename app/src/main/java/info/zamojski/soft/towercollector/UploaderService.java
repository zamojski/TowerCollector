/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import info.zamojski.soft.towercollector.enums.UploadResult;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.files.devices.MemoryTextDevice;
import info.zamojski.soft.towercollector.files.formatters.csv.CsvUploadFormatter;
import info.zamojski.soft.towercollector.files.generators.CsvTextGenerator;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.io.network.NetworkHelper;
import info.zamojski.soft.towercollector.io.network.NetworkHelper.ResponseData;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.uploader.UploaderNotificationHelper;
import info.zamojski.soft.towercollector.utils.ApkUtils;
import info.zamojski.soft.towercollector.utils.NetworkUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.acra.ACRA;

import org.greenrobot.eventbus.EventBus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.text.TextUtils;

import trikita.log.Log;

import android.widget.Toast;

public class UploaderService extends Service {

    private static final String TAG = UploaderService.class.getSimpleName();

    public static final String SERVICE_FULL_NAME = UploaderService.class.getCanonicalName();
    public static final String BROADCAST_INTENT_STOP_SERVICE = SERVICE_FULL_NAME + ".UploaderCancel";
    public static final String INTENT_KEY_APIKEY = "INTENT_KEY_APIKEY";
    public static final String INTENT_KEY_RESULT_DESCRIPTION = "INTENT_KEY_RESULT_DESCRIPTION";
    public static final int NOTIFICATION_ID = 'U';
    private static final int MEASUREMENTS_PER_PART = 400;

    private String uploadUrl;

    private HandlerThread handlerThread;
    private Handler handler;

    private NotificationManager notificationManager;
    private UploaderNotificationHelper notificationHelper;

    private String appId;
    private String apiKey;
    private AtomicBoolean isCancelled = new AtomicBoolean(false);

    private UploadResult uploadResult = UploadResult.NotStarted;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("onCreate(): Creating service");
        MyApplication.startBackgroundTask(this);
        // get managers
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationHelper = new UploaderNotificationHelper(this);
        // set upload url
        uploadUrl = getString(R.string.upload_url_opencellid_org);
        // set app code
        appId = ApkUtils.getAppId(getApplication());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d("onStartCommand(): Starting service");
        // get API key (intent or extras may be null if the service is being restarted)
        if (intent == null || intent.getExtras() == null) {
            // we hope API key will be valid
            apiKey = MyApplication.getPreferencesProvider().getApiKey();
        } else {
            apiKey = intent.getExtras().getString(INTENT_KEY_APIKEY);
        }
        // start work on separate thread to eliminate menu lags
        getHandler().post(new UploaderThread());
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("onDestroy(): Destroying service");
        MyApplication.stopBackgroundTask();
        stopForeground(true);
        if (stopRequestBroadcastReceiver != null)
            unregisterReceiver(stopRequestBroadcastReceiver);
        // display stop reason
        Log.d("onDestroy(): Upload result: %s", uploadResult);
        int messageId = R.string.unknown_error;
        int descriptionId = R.string.unknown_error;
        switch (uploadResult) {
            case NoData:
                messageId = R.string.uploader_no_data;
                descriptionId = R.string.uploader_no_data_description;
                break;
            case InvalidApiKey:
                messageId = R.string.uploader_invalid_api_key;
                descriptionId = R.string.uploader_invalid_api_key_description;
                break;
            case InvalidInputData:
                messageId = R.string.uploader_invalid_input_data;
                descriptionId = R.string.uploader_invalid_input_data_description;
                break;
            case Cancelled:
                messageId = R.string.uploader_aborted;
                descriptionId = R.string.uploader_aborted_description;
                break;
            case NotStarted:
            case Succeeded:
                messageId = R.string.uploader_succeeded;
                descriptionId = R.string.uploader_succeeded_description;
                break;
            case PartiallySucceeded:
                messageId = R.string.uploader_partially_succeeded;
                descriptionId = R.string.uploader_partially_succeeded_description;
                break;
            case DeleteFailed:
                messageId = R.string.uploader_delete_failed;
                descriptionId = R.string.uploader_delete_failed_description;
                break;
            case ConnectionError:
                messageId = R.string.uploader_connection_error;
                descriptionId = R.string.uploader_connection_error_description;
                break;
            case ServerInternalError:
                messageId = R.string.uploader_server_internal_error;
                descriptionId = R.string.uploader_server_internal_error_description;
                break;
            case ServerTemporarilyNotAvailable:
                messageId = R.string.uploader_server_temporarily_not_available;
                descriptionId = R.string.uploader_server_temporarily_not_available_description;
                break;
            case ServerNotAvailable:
                messageId = R.string.uploader_server_not_available_error;
                descriptionId = R.string.uploader_server_not_available_error_description;
                break;
            case Failed:
                messageId = R.string.uploader_failed;
                descriptionId = R.string.uploader_failed_description;
                break;
            case Rejected:
                messageId = R.string.uploader_rejected;
                descriptionId = R.string.uploader_rejected_description;
                break;
        }
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
        // update notification according to result
        Notification notification = notificationHelper.updateNotificationFinished(messageId, descriptionId);
        notificationManager.notify(NOTIFICATION_ID, notification);
        super.onDestroy();
    }

    // ========== NOTIFICATIONS ========== //

    private synchronized void updateNotification() {
        Notification notification = notificationHelper.updateNotificationCancelling();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private synchronized void updateNotification(int partNumber, int partsCount) {
        Notification notification = notificationHelper.updateNotificationProgress(partNumber, partsCount);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    // ========== BROADCAST RECEIVERS ========== //

    private BroadcastReceiver stopRequestBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("stopRequestBroadcastReceiver.onReceive(): Received broadcast intent: %s", intent);
            if (intent.getAction().equals(BROADCAST_INTENT_STOP_SERVICE)) {
                // stop worker
                isCancelled.set(true);
                // change notification to canceling
                updateNotification();
            }
        }
    };

    // ========== MISCELLANEOUS ========== //

    private Handler getHandler() {
        if (handlerThread == null) {
            handlerThread = new HandlerThread(getClass().getName());
            handlerThread.start();
        }
        if (handler == null) {
            handler = new Handler(handlerThread.getLooper());
        }
        return handler;
    }

    // ========== INNER OBJECTS ========== //

    private class UploaderThread implements Runnable {

        private final String INNER_TAG = TAG + "." + UploaderThread.class.getSimpleName();

        @Override
        public void run() {
            // register receiver
            registerReceiver(stopRequestBroadcastReceiver, new IntentFilter(BROADCAST_INTENT_STOP_SERVICE));

            // start as foreground service to prevent from killing
            Notification notification = notificationHelper.createNotification();
            startForeground(UploaderService.NOTIFICATION_ID, notification);

            // get number of measurements to upload
            int measurementsCount = MeasurementsDatabase.getInstance(getApplication()).getAllMeasurementsCount();

            // get last measurement row id
            Measurement lastMeasurement = MeasurementsDatabase.getInstance(getApplication()).getLastMeasurement();

            // check if there is anything to upload
            if (measurementsCount == 0 || lastMeasurement == null) {
                Log.d(INNER_TAG, "run(): Cancelling upload due to no data to upload");
                uploadResult = UploadResult.NoData;
                stopSelf();
                return;
            }

            AnalyticsStatistics startStats = MeasurementsDatabase.getInstance(getApplication()).getAnalyticsStatistics();
            long startTime = System.currentTimeMillis();

            // calculate number of upload parts
            int partsCount = 1;
            if (measurementsCount > MEASUREMENTS_PER_PART) {
                partsCount = (int) Math.ceil(1.0 * measurementsCount / MEASUREMENTS_PER_PART);
            }

            // create container for responses
            int succeededParts = 0;

            // for each part start new upload
            for (int i = 1; i <= partsCount; i++) {
                // check if cancelled
                if (isCancelled.get()) {
                    uploadResult = UploadResult.Cancelled;
                    break;
                }
                // notify
                updateNotification(i, partsCount);
                // prepare data starting from oldest
                List<Measurement> measurements = MeasurementsDatabase.getInstance(getApplication()).getOlderMeasurements(lastMeasurement.getTimestamp(), 0, MEASUREMENTS_PER_PART);

                // create generator instance
                MemoryTextDevice device = new MemoryTextDevice();
                CsvTextGenerator<CsvUploadFormatter, MemoryTextDevice> generator = new CsvTextGenerator<>(new CsvUploadFormatter(), device);
                // write measurements
                try {
                    device.open();
                    generator.writeHeader();
                    generator.writeEntryChunk(measurements);
                } catch (IOException ex) {
                    // this should never happen for MemoryTextDevice
                    Log.e(INNER_TAG, "run(): Error while generating file", ex);
                    MyApplication.getAnalytics().sendException(ex, Boolean.TRUE);
                    ACRA.getErrorReporter().handleSilentException(ex);
                    uploadResult = UploadResult.Failed;
                }
                // get content
                String csvContent = device.read();
                device.close();
                //// only for upload testing
                //try {
                //    java.io.File destFile = new java.io.File(info.zamojski.soft.towercollector.utils.FileUtils.getExternalStorageAppDir(), "upload_" + info.zamojski.soft.towercollector.utils.FileUtils.getCurrentDateFilename("csv"));
                //    info.zamojski.soft.towercollector.files.devices.FileTextDevice fileDevice = new info.zamojski.soft.towercollector.files.devices.FileTextDevice(destFile.getPath());
                //    fileDevice.open();
                //    fileDevice.write(csvContent);
                //    fileDevice.close();
                //    Log.d(INNER_TAG, String.format("run(): Uploaded file saved as: %s", fileDevice.getPath()));
                //} catch (info.zamojski.soft.towercollector.files.DeviceOperationException ex) {
                //    Log.d(INNER_TAG, "run(): Uploaded file generation problem", ex);
                //} catch (IOException ex) {
                //    Log.d(INNER_TAG, "run(): Uploaded file generation problem", ex);
                //}
                // send request
                ResponseData response = NetworkHelper.sendPost(uploadUrl, appId, apiKey, csvContent);
                Log.d(INNER_TAG, String.format("run(): Server response: %s", response));
                // check whether it makes sense to continue
                if (response.getCode() == 0) {
                    uploadResult = UploadResult.ConnectionError;
                    break;
                } else if (response.getCode() == 400) {
                    uploadResult = UploadResult.InvalidInputData;
                    break;
                } else if (response.getCode() == 401 || response.getCode() == 403) {
                    uploadResult = UploadResult.InvalidApiKey;
                    break;
                } else if (response.getCode() == 500) {
                    uploadResult = UploadResult.ServerInternalError;
                    break;
                } else if (response.getCode() == 503) {
                    uploadResult = UploadResult.ServerTemporarilyNotAvailable;
                    break;
                } else if (response.getCode() >= 400 && response.getCode() <= 599) {
                    uploadResult = UploadResult.ServerNotAvailable;
                    break;
                } else {
                    // check response
                    String responseContent = response.getContent();
                    if (TextUtils.isEmpty(responseContent)) {
                        uploadResult = UploadResult.Rejected;
                        break;
                    } else {
                        String trimmedResponseContent = responseContent.trim();
                        // validate response content (just for sure)
                        if (trimmedResponseContent.equalsIgnoreCase("Error: Authorization failed. Check your API key.")) {
                            uploadResult = UploadResult.InvalidApiKey;
                            break;
                        } else {
                            if (response.getCode() == 200 && "0,OK".equalsIgnoreCase(trimmedResponseContent)) {
                                Log.d(INNER_TAG, String.format("run(): Uploaded %s measurements", measurements.size()));
                                uploadResult = UploadResult.PartiallySucceeded;
                                succeededParts++;
                            } else {
                                // don't report captive portals
                                if (response.getCode() != 302) {
                                    RuntimeException ex = new UploadFailedException(response);
                                    ACRA.getErrorReporter().handleSilentException(ex);
                                }
                                uploadResult = UploadResult.Failed;
                                break;
                            }
                        }
                    }
                }
                // delete sent measurements
                int j = 0;
                int[] rowIds = new int[measurements.size()];
                for (Measurement m : measurements) {
                    rowIds[j++] = m.getRowId();
                }
                int numberOfDeleted = MeasurementsDatabase.getInstance(getApplication()).deleteMeasurements(rowIds);
                if (numberOfDeleted == 0) {
                    uploadResult = UploadResult.DeleteFailed;
                    break;
                }
                // broadcast part uploaded (if error not encountered earlier)
                EventBus.getDefault().post(new PrintMainWindowEvent());
            }
            // sum up results and update notification
            if (uploadResult == UploadResult.PartiallySucceeded) {
                // we can be sure that everything was ok (because we stop on error)
                uploadResult = UploadResult.Succeeded;
            } else if (uploadResult != UploadResult.DeleteFailed) {
                // can be cancelled or failed after uploading few parts (but not all)
                if (succeededParts > 0) {
                    uploadResult = UploadResult.PartiallySucceeded;
                }
            }

            // send stats only when succeeded
            if (uploadResult == UploadResult.Succeeded || uploadResult == UploadResult.PartiallySucceeded) {
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                String networkType = NetworkUtils.getNetworkType(getApplication());
                AnalyticsStatistics endStats = MeasurementsDatabase.getInstance(getApplication()).getAnalyticsStatistics();
                AnalyticsStatistics stats = new AnalyticsStatistics();
                stats.setLocations(startStats.getLocations() - endStats.getLocations());
                stats.setCells(startStats.getCells() - endStats.getCells());
                stats.setDays(startStats.getDays() - endStats.getDays());
                MyApplication.getAnalytics().sendUploadFinished(duration, networkType, stats);
            }
            // broadcast upload finished and stop service
            EventBus.getDefault().post(new PrintMainWindowEvent());
            stopSelf();
        }

        private class UploadFailedException extends RuntimeException {
            private static final long serialVersionUID = -8046748553665290986L;

            public UploadFailedException(ResponseData data) {
                this(String.valueOf(data));
            }

            public UploadFailedException(String message) {
                super(message);
            }
        }
    }
}
