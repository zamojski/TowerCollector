/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import info.zamojski.soft.towercollector.enums.UploadResult;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.files.devices.MemoryTextDevice;
import info.zamojski.soft.towercollector.files.formatters.csv.CsvUploadFormatter;
import info.zamojski.soft.towercollector.files.formatters.json.JsonMozillaFormatter;
import info.zamojski.soft.towercollector.files.generators.CsvTextGenerator;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.files.generators.JsonTextGenerator;
import info.zamojski.soft.towercollector.io.network.IUploadClient;
import info.zamojski.soft.towercollector.io.network.MozillaUploadClient;
import info.zamojski.soft.towercollector.io.network.OcidUploadClient;
import info.zamojski.soft.towercollector.io.network.RequestResult;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.providers.preferences.PreferencesProvider;
import info.zamojski.soft.towercollector.uploader.UploaderNotificationHelper;
import info.zamojski.soft.towercollector.utils.ApkUtils;
import info.zamojski.soft.towercollector.utils.NetworkUtils;
import timber.log.Timber;

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

import android.widget.Toast;

public class UploaderService extends Service {

    public static final String SERVICE_FULL_NAME = UploaderService.class.getCanonicalName();
    public static final String BROADCAST_INTENT_STOP_SERVICE = SERVICE_FULL_NAME + ".UploaderCancel";
    public static final String INTENT_KEY_UPLOAD_TO_OCID = "upload_to_ocid";
    public static final String INTENT_KEY_UPLOAD_TO_MLS = "upload_to_mls";
    public static final String INTENT_KEY_UPLOAD_TRY_REUPLOAD = "try_reupload";
    public static final String INTENT_KEY_RESULT_DESCRIPTION = "result_description";
    public static final int NOTIFICATION_ID = 'U';
    private static final int MEASUREMENTS_PER_PART = 500;

    private HandlerThread handlerThread;
    private Handler handler;

    private NotificationManager notificationManager;
    private UploaderNotificationHelper notificationHelper;

    private String appId;
    private String ocidUploadUrl;
    private String mlsUploadUrl;
    private String ocidApiKey;
    private String mlsApiKey;
    private boolean isOpenCellIdUploadEnabled;
    private boolean isMlsUploadEnabled;
    private boolean isReuploadIfUploadFailsEnabled;
    private AtomicBoolean isCancelled = new AtomicBoolean(false);

    private UploadResult ocidUploadResult = UploadResult.NotStarted;
    private UploadResult mlsUploadResult = UploadResult.NotStarted;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate(): Creating service");
        MyApplication.startBackgroundTask(this);
        // get managers
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationHelper = new UploaderNotificationHelper(this);
        // set upload url
        ocidUploadUrl = getString(R.string.upload_url_opencellid_org);
        mlsUploadUrl = getString(R.string.upload_url_mls);
        // set app code
        appId = ApkUtils.getAppId(getApplication());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Timber.d("onStartCommand(): Starting service");
        // get passed configuration (intent or extras may be null if the service is being restarted)
        PreferencesProvider preferencesProvider = MyApplication.getPreferencesProvider();
        isOpenCellIdUploadEnabled = preferencesProvider.isOpenCellIdUploadEnabled();
        isMlsUploadEnabled = preferencesProvider.isMlsUploadEnabled();
        isReuploadIfUploadFailsEnabled = preferencesProvider.isReuploadIfUploadFailsEnabled();
        if (intent != null) {
            isOpenCellIdUploadEnabled = intent.getBooleanExtra(INTENT_KEY_UPLOAD_TO_OCID, isOpenCellIdUploadEnabled);
            isMlsUploadEnabled = intent.getBooleanExtra(INTENT_KEY_UPLOAD_TO_MLS, isMlsUploadEnabled);
            isReuploadIfUploadFailsEnabled = intent.getBooleanExtra(INTENT_KEY_UPLOAD_TRY_REUPLOAD, isReuploadIfUploadFailsEnabled);
        }
        // we hope API key will be valid
        ocidApiKey = preferencesProvider.getApiKey();
        mlsApiKey = BuildConfig.MLS_API_KEY;
        // start work on separate thread to eliminate lags
        getHandler().post(new UploaderThread());
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy(): Destroying service");
        MyApplication.stopBackgroundTask();
        stopForeground(true);
        if (stopRequestBroadcastReceiver != null)
            unregisterReceiver(stopRequestBroadcastReceiver);
        // display stop reason
        Timber.d("onDestroy(): Upload result OCID: %s, MLS: %s", ocidUploadResult, mlsUploadResult);
        String ocidMessage = getString(getMessage(ocidUploadResult));
        String ocidDescription = getString(getDescription(ocidUploadResult));
        String mlsMessage = getString(getMessage(mlsUploadResult));
        String mlsDescription = getString(getDescription(mlsUploadResult));
        String message = getString(R.string.uploader_result_message, ocidMessage, mlsMessage);
        String description = getString(R.string.uploader_result_message, ocidDescription, mlsDescription);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        // update notification according to result
        Notification notification = notificationHelper.updateNotificationFinished(message, description);
        notificationManager.notify(NOTIFICATION_ID, notification);
        super.onDestroy();
    }

    private int getMessage(UploadResult uploadResult) {
        switch (uploadResult) {
            case NotStarted:
                return R.string.uploader_disabled;
            case NoData:
                return R.string.uploader_no_data;
            case InvalidApiKey:
                return R.string.uploader_invalid_api_key;
            case InvalidData:
                return R.string.uploader_invalid_input_data;
            case Cancelled:
                return R.string.uploader_aborted;
            case Success:
                return R.string.uploader_success;
            case PartiallySucceeded:
                return R.string.uploader_partially_succeeded;
            case DeleteFailed:
                return R.string.uploader_delete_failed;
            case ConnectionError:
                return R.string.uploader_connection_error;
            case ServerError:
                return R.string.uploader_server_error;
            case Failure:
                return R.string.uploader_failure;
            case PermissionDenied:
                return R.string.permission_denied;
            default:
                return R.string.unknown_error;
        }
    }

    private int getDescription(UploadResult uploadResult) {
        switch (uploadResult) {
            case NotStarted:
                return R.string.uploader_disabled_description;
            case NoData:
                return R.string.uploader_no_data_description;
            case InvalidApiKey:
                return R.string.uploader_invalid_api_key_description;
            case InvalidData:
                return R.string.uploader_invalid_input_data_description;
            case Cancelled:
                return R.string.uploader_aborted_description;
            case Success:
                return R.string.uploader_success_description;
            case PartiallySucceeded:
                return R.string.uploader_partially_succeeded_description;
            case DeleteFailed:
                return R.string.uploader_delete_failed_description;
            case ConnectionError:
                return R.string.uploader_connection_error_description;
            case ServerError:
                return R.string.uploader_server_error_description;
            case Failure:
                return R.string.uploader_failure_description;
            case PermissionDenied:
                return R.string.permission_uploader_denied_message;
            default:
                return R.string.unknown_error;
        }
    }

    // ========== NOTIFICATIONS ========== //

    private synchronized void updateNotification() {
        Notification notification = notificationHelper.updateNotificationCancelling();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private synchronized void updateNotification(int progress) {
        Notification notification = notificationHelper.updateNotificationProgress(progress);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    // ========== BROADCAST RECEIVERS ========== //

    private BroadcastReceiver stopRequestBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("stopRequestBroadcastReceiver.onReceive(): Received broadcast intent: %s", intent);
            if (BROADCAST_INTENT_STOP_SERVICE.equals(intent.getAction())) {
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

        private final String INNER_TAG = UploaderService.class.getSimpleName() + "." + UploaderThread.class.getSimpleName();

        @Override
        public void run() {
            // register receiver
            registerReceiver(stopRequestBroadcastReceiver, new IntentFilter(BROADCAST_INTENT_STOP_SERVICE));

            // start as foreground service to prevent from killing
            Notification notification = notificationHelper.createNotification(notificationManager);
            startForeground(UploaderService.NOTIFICATION_ID, notification);

            // get number of measurements to upload
            int measurementsCount = MeasurementsDatabase.getInstance(getApplication()).getAllMeasurementsCount();
            int temporaryMeasurementsCount = MeasurementsDatabase.getInstance(getApplication()).getAllMeasurementsCountFor(isOpenCellIdUploadEnabled, isMlsUploadEnabled);

            // check if there is anything to upload
            if (measurementsCount == 0 && temporaryMeasurementsCount == 0) {
                Timber.tag(INNER_TAG).d("run(): Cancelling upload due to no data to upload");
                ocidUploadResult = UploadResult.NoData;
                mlsUploadResult = UploadResult.NoData;
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
            int temporaryPartsCount = (int) Math.ceil(1.0 * temporaryMeasurementsCount / MEASUREMENTS_PER_PART);

            int succeededParts = upload(partsCount, temporaryPartsCount);

            // sum up results and update notification for ocid
            if (ocidUploadResult == UploadResult.PartiallySucceeded) {
                // we can be sure that everything was ok (because we stop on error)
                ocidUploadResult = UploadResult.Success;
            } else if (ocidUploadResult != UploadResult.DeleteFailed) {
                // can be cancelled or failed after uploading few parts (but not all)
                if (succeededParts > 0) {
                    ocidUploadResult = UploadResult.PartiallySucceeded;
                }
            }
            // sum up results and update notification for mls
            if (mlsUploadResult == UploadResult.PartiallySucceeded) {
                // we can be sure that everything was ok (because we stop on error)
                mlsUploadResult = UploadResult.Success;
            } else if (mlsUploadResult != UploadResult.DeleteFailed) {
                // can be cancelled or failed after uploading few parts (but not all)
                if (succeededParts > 0) {
                    mlsUploadResult = UploadResult.PartiallySucceeded;
                }
            }

            // send stats only when succeeded
            if (ocidUploadResult == UploadResult.Success || ocidUploadResult == UploadResult.PartiallySucceeded
                    || mlsUploadResult == UploadResult.Success || mlsUploadResult == UploadResult.PartiallySucceeded) {
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                String networkType = NetworkUtils.getNetworkType(getApplication());
                AnalyticsStatistics endStats = MeasurementsDatabase.getInstance(getApplication()).getAnalyticsStatistics();
                AnalyticsStatistics stats = new AnalyticsStatistics();
                stats.setLocations(startStats.getLocations() - endStats.getLocations());
                stats.setCells(startStats.getCells() - endStats.getCells());
                stats.setDays(startStats.getDays() - endStats.getDays());
                if (isOpenCellIdUploadEnabled)
                    MyApplication.getAnalytics().sendUploadFinished(duration, networkType, stats, true);
                if (isMlsUploadEnabled)
                    MyApplication.getAnalytics().sendUploadFinished(duration, networkType, stats, false);
            }
            // broadcast upload finished and stop service
            EventBus.getDefault().post(new PrintMainWindowEvent());
            stopSelf();
        }

        private int upload(int partsCount, int temporaryPartsCount) {//TODO use param
            int succeededParts = 0;
            int totalPartsCount = partsCount + temporaryPartsCount;
            boolean continueOcidUpload = isOpenCellIdUploadEnabled;
            boolean continueMlsUpload = isMlsUploadEnabled;
            // for each part start new upload
            for (int i = 0; i < totalPartsCount; i++) {
                // check if cancelled
                if (isCancelled.get()) {
                    ocidUploadResult = UploadResult.Cancelled;
                    mlsUploadResult = UploadResult.Cancelled;
                    break;
                }
                // notify
                int progress = (int) (1.0 * i / totalPartsCount);
                updateNotification(progress);
                // prepare data starting from oldest
                List<Measurement> measurements;
                if (i < partsCount)
                    measurements = MeasurementsDatabase.getInstance(getApplication()).getOlderMeasurements(0, MEASUREMENTS_PER_PART);
                else
                    measurements = MeasurementsDatabase.getInstance(getApplication()).getTemporaryMeasurements(0, MEASUREMENTS_PER_PART);

                Timber.d("upload(): Continue upload to OCID = %s, MLS = %s", continueOcidUpload, continueMlsUpload);

                if (continueOcidUpload) {
                    ocidUploadResult = uploadToOcid(measurements);
                }
                if (continueMlsUpload) {
                    mlsUploadResult = uploadToMls(measurements);
                }

                if (ocidUploadResult == UploadResult.PartiallySucceeded || mlsUploadResult == UploadResult.PartiallySucceeded)
                    succeededParts++;

                continueOcidUpload &= (ocidUploadResult == UploadResult.PartiallySucceeded
                        || ocidUploadResult == UploadResult.ConnectionError
                        || ocidUploadResult == UploadResult.Failure
                        || ocidUploadResult == UploadResult.InvalidData
                        || ocidUploadResult == UploadResult.ServerError);
                continueMlsUpload &= (mlsUploadResult == UploadResult.PartiallySucceeded
                        || mlsUploadResult == UploadResult.ConnectionError
                        || mlsUploadResult == UploadResult.Failure
                        || mlsUploadResult == UploadResult.InvalidData
                        || mlsUploadResult == UploadResult.ServerError);

                boolean ocidSuccessful = (ocidUploadResult == UploadResult.PartiallySucceeded);
                boolean mlsSuccessful = (mlsUploadResult == UploadResult.PartiallySucceeded);

                if (isReuploadIfUploadFailsEnabled) {
                    // all enabled succeeded
                    if ((ocidSuccessful || !isOpenCellIdUploadEnabled) && (mlsSuccessful || !isMlsUploadEnabled)) {
                        Timber.d("upload(): Deleting measurements because OCID enabled = %s and successful = %s, MLS enabled = %s and successful = %s", isOpenCellIdUploadEnabled, ocidSuccessful, isMlsUploadEnabled, mlsSuccessful);
                        // delete sent measurements
                        int[] rowIds = getRowIds(measurements);
                        int numberOfDeleted = MeasurementsDatabase.getInstance(getApplication()).deleteMeasurements(rowIds);
                        if (numberOfDeleted == 0) {
                            ocidUploadResult = UploadResult.DeleteFailed;
                            mlsUploadResult = UploadResult.DeleteFailed;
                            break;
                        }
                    } else if (ocidSuccessful && isMlsUploadEnabled) {
                        Timber.d("upload(): Moving measurements to MLS temporary");
                        // keep for mls
                        int[] rowIds = getRowIds(measurements);//TODO what if already temporary???
                        int numberOfDeleted = MeasurementsDatabase.getInstance(getApplication()).moveToTemporary(rowIds, System.currentTimeMillis(), null);
                        if (numberOfDeleted == 0) {
                            ocidUploadResult = UploadResult.DeleteFailed;
                            break;
                        }
                    } else if (mlsSuccessful && isOpenCellIdUploadEnabled) {
                        Timber.d("upload(): Moving measurements to OCID temporary");
                        // keep for ocid
                        int[] rowIds = getRowIds(measurements);
                        int numberOfDeleted = MeasurementsDatabase.getInstance(getApplication()).moveToTemporary(rowIds, null, System.currentTimeMillis());
                        if (numberOfDeleted == 0) {
                            mlsUploadResult = UploadResult.DeleteFailed;
                            break;
                        }
                    } else {
                        Timber.d("upload(): Skipping delete because all uploads failed");
                        // all uploads failed
                        // this means measurements were not uploaded
                    }
                } else if ((isOpenCellIdUploadEnabled && ocidSuccessful) || (isMlsUploadEnabled && mlsSuccessful)) {
                    Timber.d("upload(): Deleting measurements because OCID enabled = %s and successful = %s, MLS enabled = %s and successful = %s", isOpenCellIdUploadEnabled, ocidSuccessful, isMlsUploadEnabled, mlsSuccessful);
                    // delete sent measurements
                    int[] rowIds = getRowIds(measurements);
                    int numberOfDeleted = MeasurementsDatabase.getInstance(getApplication()).deleteMeasurements(rowIds);
                    if (numberOfDeleted == 0) {
                        ocidUploadResult = UploadResult.DeleteFailed;
                        mlsUploadResult = UploadResult.DeleteFailed;
                        break;
                    }
                }
                // broadcast part uploaded (if error not encountered earlier)
                EventBus.getDefault().post(new PrintMainWindowEvent());

                if (!continueOcidUpload && !continueMlsUpload)
                    break;
            }
            return succeededParts;
        }

        private int[] getRowIds(List<Measurement> measurements) {
            int j = 0;
            int[] rowIds = new int[measurements.size()];
            for (Measurement m : measurements) {
                rowIds[j++] = m.getRowId();
            }
            return rowIds;
        }

        private UploadResult uploadToOcid(List<Measurement> measurements) {
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
                Timber.tag(INNER_TAG).e(ex, "uploadToOcid(): Error while generating file");
                MyApplication.getAnalytics().sendException(ex, Boolean.TRUE);
                ACRA.getErrorReporter().handleSilentException(ex);
                return UploadResult.Failure;
            }
            // get content
            String csvContent = device.read();
            device.close();
            // send request
            try {
                IUploadClient client = new OcidUploadClient(ocidUploadUrl, appId, ocidApiKey);
                RequestResult response = client.uploadMeasurements(csvContent);
                Timber.tag(INNER_TAG).d("uploadToOcid(): Server response: %s", response);
                // check whether it makes sense to continue
                if (response == RequestResult.ConfigurationError) {
                    return UploadResult.InvalidData;
                } else if (response == RequestResult.ServerError) {
                    return UploadResult.ServerError;
                } else if (response == RequestResult.ConnectionError) {
                    return UploadResult.ConnectionError;
                } else if (response == RequestResult.Failure) {
                    return UploadResult.Failure;
                } else if (response == RequestResult.InvalidApiKey) {
                    return UploadResult.InvalidApiKey;
                } else if (response == RequestResult.Success) {
                    Timber.tag(INNER_TAG).d("uploadToOcid(): Uploaded %s measurements", measurements.size());
                    return UploadResult.PartiallySucceeded;
                } else {
                    throw new UnsupportedOperationException(String.format("Unsupported upload result %s", response));
                }
            } catch (SecurityException ex) {
                Timber.tag(INNER_TAG).e(ex, "uploadToOcid(): Internet permission is denied");
                return UploadResult.PermissionDenied;
            }
        }

        private UploadResult uploadToMls(List<Measurement> measurements) {//TODO
            // create generator instance
            MemoryTextDevice device = new MemoryTextDevice();
            JsonTextGenerator<JsonMozillaFormatter, MemoryTextDevice> generator = new JsonTextGenerator<>(new JsonMozillaFormatter(), device);
            // write measurements
            try {
                device.open();
                generator.writeEntries(measurements);
            } catch (IOException ex) {
                // this should never happen for MemoryTextDevice
                Timber.tag(INNER_TAG).e(ex, "uploadToMls(): Error while generating file");
                MyApplication.getAnalytics().sendException(ex, Boolean.TRUE);
                ACRA.getErrorReporter().handleSilentException(ex);
                return UploadResult.Failure;
            }
            // get content
            String jsonContent = device.read();
            device.close();
            // send request
            try {
                IUploadClient client = new MozillaUploadClient(mlsUploadUrl, mlsApiKey);
                RequestResult response = client.uploadMeasurements(jsonContent);
                Timber.tag(INNER_TAG).d("uploadToMls(): Server response: %s", response);
                // check whether it makes sense to continue
                if (response == RequestResult.ConfigurationError) {
                    return UploadResult.InvalidData;
                } else if (response == RequestResult.ServerError) {
                    return UploadResult.ServerError;
                } else if (response == RequestResult.ConnectionError) {
                    return UploadResult.ConnectionError;
                } else if (response == RequestResult.Failure) {
                    return UploadResult.Failure;
                } else if (response == RequestResult.Success) {
                    Timber.tag(INNER_TAG).d("uploadToMls(): Uploaded %s measurements", measurements.size());
                    return UploadResult.PartiallySucceeded;
                } else {
                    throw new UnsupportedOperationException(String.format("Unsupported upload result %s", response));
                }
            } catch (SecurityException ex) {
                Timber.tag(INNER_TAG).e(ex, "uploadToMls(): Internet permission is denied");
                return UploadResult.PermissionDenied;
            }
        }
    }
}
