/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.uploader;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.zamojski.soft.towercollector.BuildConfig;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.UploaderQuickSettingsTileService;
import info.zamojski.soft.towercollector.analytics.IntentSource;
import info.zamojski.soft.towercollector.analytics.internal.Label;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.enums.UploadResult;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.files.formatters.csv.CsvUploadFormatter;
import info.zamojski.soft.towercollector.files.formatters.csv.ICsvFormatter;
import info.zamojski.soft.towercollector.files.formatters.json.IJsonFormatter;
import info.zamojski.soft.towercollector.files.formatters.json.JsonMozillaUploadFormatter;
import info.zamojski.soft.towercollector.files.generators.wrappers.interfaces.IProgressListener;
import info.zamojski.soft.towercollector.io.network.IUploadClient;
import info.zamojski.soft.towercollector.io.network.MozillaUploadClient;
import info.zamojski.soft.towercollector.io.network.OcidUploadClient;
import info.zamojski.soft.towercollector.io.network.RequestResult;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.model.Statistics;
import info.zamojski.soft.towercollector.utils.ApkUtils;
import info.zamojski.soft.towercollector.utils.NetworkUtils;
import info.zamojski.soft.towercollector.utils.OpenCellIdUtils;
import timber.log.Timber;

public class UploaderWorker extends Worker implements IProgressListener {

    public static final String SERVICE_FULL_NAME = UploaderWorker.class.getCanonicalName();
    public static final String WORKER_TAG = "UPLOADER_WORKER";

    public static final String INTENT_KEY_UPLOAD_TO_OCID = "upload_to_ocid";
    public static final String INTENT_KEY_UPLOAD_TO_OCID_SHARED = "upload_to_ocid_shared";
    public static final String INTENT_KEY_UPLOAD_TO_MLS = "upload_to_mls";
    public static final String INTENT_KEY_UPLOAD_TRY_REUPLOAD = "try_reupload";
    public static final String INTENT_KEY_START_INTENT_SOURCE = "start_intent_source";
    public static final String PROGRESS = "PROGRESS";
    public static final String PROGRESS_MAX = "PROGRESS_MAX";
    public static final int PROGRESS_MIN_VALUE = 0;
    public static final int PROGRESS_MAX_VALUE = 100;
    public static final String MESSAGE = "MESSAGE";
    public static final int NOTIFICATION_ID = 'U';
    private static final int LOCATIONS_PER_PART = 85;

    private final NotificationManager notificationManager;
    private final UploaderNotificationHelper notificationHelper;

    private final String appId;
    private final String ocidUploadUrl;
    private final String mlsUploadUrl;
    private String ocidApiKey;
    private String mlsApiKey;
    private boolean isOpenCellIdUploadEnabled;
    private boolean isUseSharedOpenCellIdApiKeyEnabled;
    private boolean isMlsUploadEnabled;
    private boolean isReuploadIfUploadFailsEnabled;

    private UploadResult ocidUploadResult = UploadResult.NotStarted;
    private UploadResult mlsUploadResult = UploadResult.NotStarted;

    private IntentSource startIntentSource;

    public UploaderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationHelper = new UploaderNotificationHelper(MyApplication.getApplication());

        // set upload url
        ocidUploadUrl = getStringById(R.string.upload_url_opencellid_org);
        mlsUploadUrl = getStringById(R.string.upload_url_mls);
        // set app code
        appId = ApkUtils.getAppId(MyApplication.getApplication());
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Notification notification = notificationHelper.createNotification(notificationManager);
            ForegroundInfo foregroundInfo = new ForegroundInfo(NOTIFICATION_ID, notification);
            setForegroundAsync(foregroundInfo);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                UploaderQuickSettingsTileService.requestTileUpdate(true);
            }

            Timber.d("doWork(): Starting upload");
            MyApplication.startBackgroundTask(this);

            // get passed configuration
            isOpenCellIdUploadEnabled = getInputData().getBoolean(INTENT_KEY_UPLOAD_TO_OCID, MyApplication.getPreferencesProvider().isOpenCellIdUploadEnabled());
            isUseSharedOpenCellIdApiKeyEnabled = getInputData().getBoolean(INTENT_KEY_UPLOAD_TO_OCID_SHARED, MyApplication.getPreferencesProvider().isUseSharedOpenCellIdApiKeyEnabled());
            isMlsUploadEnabled = getInputData().getBoolean(INTENT_KEY_UPLOAD_TO_MLS, MyApplication.getPreferencesProvider().isMlsUploadEnabled());
            isReuploadIfUploadFailsEnabled = getInputData().getBoolean(INTENT_KEY_UPLOAD_TRY_REUPLOAD, MyApplication.getPreferencesProvider().isReuploadIfUploadFailsEnabled());
            startIntentSource = IntentSource.valueOf(getInputData().getString(INTENT_KEY_START_INTENT_SOURCE));
            // we hope API key will be valid
            ocidApiKey = isUseSharedOpenCellIdApiKeyEnabled ? OpenCellIdUtils.getSharedApiKey() : OpenCellIdUtils.getApiKey();
            mlsApiKey = BuildConfig.MLS_API_KEY;

            // get number of locations to upload
            int locationsCount = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getAllLocationsCount(true);

            // check if there is anything to upload
            if (locationsCount == 0) {
                Timber.d("doWork(): Cancelling upload due to no data to upload");
                ocidUploadResult = UploadResult.NoData;
                mlsUploadResult = UploadResult.NoData;
                String summary = getStringById(R.string.uploader_result_message, getStringById(getMessage(ocidUploadResult)), getStringById(getDescription(ocidUploadResult)));
                return Result.failure(getMessageData(summary));
            }

            AnalyticsStatistics startStats = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getAnalyticsStatistics();
            long startTime = System.currentTimeMillis();

            // calculate number of upload parts
            int partsCount = 1;
            if (locationsCount > LOCATIONS_PER_PART) {
                partsCount = (int) Math.ceil(1.0 * locationsCount / LOCATIONS_PER_PART);
            }

            int[] succeededParts = upload(partsCount);

            // sum up results and update notification for ocid
            if (ocidUploadResult == UploadResult.PartiallySucceeded) {
                // we can be sure that everything was ok (because we stop on error)
                ocidUploadResult = UploadResult.Success;
            } else if (ocidUploadResult != UploadResult.DeleteFailed && ocidUploadResult != UploadResult.NotStarted) {
                // can be cancelled or failed after uploading few parts (but not all)
                if (succeededParts[0] > 0) {
                    ocidUploadResult = UploadResult.PartiallySucceeded;
                }
            }
            // sum up results and update notification for mls
            if (mlsUploadResult == UploadResult.PartiallySucceeded) {
                // we can be sure that everything was ok (because we stop on error)
                mlsUploadResult = UploadResult.Success;
            } else if (mlsUploadResult != UploadResult.DeleteFailed && mlsUploadResult != UploadResult.NotStarted) {
                // can be cancelled or failed after uploading few parts (but not all)
                if (succeededParts[1] > 0 && mlsUploadResult != UploadResult.LimitExceeded) {
                    mlsUploadResult = UploadResult.PartiallySucceeded;
                }
            }

            // send stats only when succeeded
            if (ocidUploadResult == UploadResult.Success || ocidUploadResult == UploadResult.PartiallySucceeded
                    || mlsUploadResult == UploadResult.Success || mlsUploadResult == UploadResult.PartiallySucceeded) {
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                String networkType = NetworkUtils.getNetworkType(MyApplication.getApplication());
                AnalyticsStatistics endStats = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getAnalyticsStatistics();
                AnalyticsStatistics stats = new AnalyticsStatistics();
                stats.setLocations(startStats.getLocations() - endStats.getLocations());
                stats.setCells(startStats.getCells() - endStats.getCells());
                stats.setDays(startStats.getDays() - endStats.getDays());
                if (isOpenCellIdUploadEnabled)
                    MyApplication.getAnalytics().sendUploadFinished(startIntentSource, networkType, duration, stats, OpenCellIdUtils.isApiKeyShared(ocidApiKey) ? Label.UploadOcidShared : Label.UploadOcid);
                if (isMlsUploadEnabled)
                    MyApplication.getAnalytics().sendUploadFinished(startIntentSource, networkType, duration, stats, Label.UploadMls);
            }
            String ocidMessage = getStringById(getMessage(ocidUploadResult));
            String ocidDescription = getStringById(getDescription(ocidUploadResult));
            String ocidSummary = getStringById(R.string.uploader_result_message, ocidMessage, ocidDescription);
            String mlsMessage = getStringById(getMessage(mlsUploadResult));
            String mlsDescription = getStringById(getDescription(mlsUploadResult));
            String mlsSummary = getStringById(R.string.uploader_result_message, mlsMessage, mlsDescription);
            String message = getStringById(R.string.uploader_result_message_summary, ocidSummary, mlsSummary);
            return Result.success(getMessageData(message));
        } catch (Exception ex) {
            Timber.e(ex, "doWork(): Uploader failed");
            return Result.failure(getMessageData(ex.getMessage()));
        } finally {
            MyApplication.stopBackgroundTask();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                UploaderQuickSettingsTileService.requestTileUpdate(false);
            }
            EventBus.getDefault().post(new PrintMainWindowEvent());
        }
    }

    @Override
    public void onStopped() {
        Timber.d("onStopped(): Uploader cancelled");
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

    private String getStringById(@StringRes int resId, Object... params) {
        return MyApplication.getApplication().getString(resId, params);
    }

    private String getStringById(@StringRes int resId) {
        return MyApplication.getApplication().getString(resId);
    }

    private Data getMessageData(String message) {
        return new Data.Builder()
                .putString(MESSAGE, message)
                .build();
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
            case LimitExceeded:
                return R.string.uploader_limit_exceeded;
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
            case LimitExceeded:
                return R.string.uploader_limit_exceeded_description;
            default:
                return R.string.unknown_error;
        }
    }

    private int[] upload(int partsCount) {
        int ocidSucceededParts = 0, mlsSucceededParts = 0;
        boolean continueOcidUpload = isOpenCellIdUploadEnabled;
        boolean continueMlsUpload = isMlsUploadEnabled;
        Statistics stats = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getMeasurementsStatistics();
        int numberToUploadOcid = stats.getToUploadOcid();
        int numberToUploadMls = stats.getToUploadMls();
        // for each part start new upload
        for (int i = 0; i < partsCount; i++) {
            // check if cancelled
            if (isStopped()) {
                ocidUploadResult = UploadResult.Cancelled;
                mlsUploadResult = UploadResult.Cancelled;
                break;
            }
            // notify
            int progress = (int) (100.0 * i / partsCount);
            reportProgress(progress, PROGRESS_MAX_VALUE);
            // prepare data starting from oldest
            List<Measurement> measurements = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getMeasurementsPartIncludingPartiallyUploaded(i * LOCATIONS_PER_PART, LOCATIONS_PER_PART);

            Timber.d("upload(): Continue upload to OCID = %s, MLS = %s", continueOcidUpload, continueMlsUpload);

            Map<UploadTarget, List<Measurement>> groupedMeasurements = groupByUploaded(measurements);
            if (continueOcidUpload) {
                List<Measurement> ocidMeasurements = groupedMeasurements.get(UploadTarget.Ocid);
                ocidUploadResult = uploadToOcid(ocidMeasurements);
                numberToUploadOcid -= ocidMeasurements.size();
            }
            if (continueMlsUpload) {
                List<Measurement> mlsMeasurements = groupedMeasurements.get(UploadTarget.Mls);
                mlsUploadResult = uploadToMls(mlsMeasurements);
                numberToUploadMls -= mlsMeasurements.size();
            }

            if (ocidUploadResult == UploadResult.PartiallySucceeded)
                ocidSucceededParts++;
            if (mlsUploadResult == UploadResult.PartiallySucceeded)
                mlsSucceededParts++;

            continueOcidUpload &= ocidUploadResult == UploadResult.PartiallySucceeded || numberToUploadOcid > 0;
            continueMlsUpload &= mlsUploadResult == UploadResult.PartiallySucceeded || numberToUploadMls > 0;

            boolean ocidSuccessful = (ocidUploadResult == UploadResult.PartiallySucceeded);
            boolean mlsSuccessful = (mlsUploadResult == UploadResult.PartiallySucceeded);

            if (isReuploadIfUploadFailsEnabled) {
                // all enabled succeeded
                if ((ocidSuccessful || !isOpenCellIdUploadEnabled) && (mlsSuccessful || !isMlsUploadEnabled)) {
                    Timber.d("upload(): Deleting measurements because OCID enabled = %s and successful = %s, MLS enabled = %s and successful = %s", isOpenCellIdUploadEnabled, ocidSuccessful, isMlsUploadEnabled, mlsSuccessful);
                    // delete sent measurements
                    int[] rowIds = getMeasurementIds(measurements);
                    int numberOfDeleted = MeasurementsDatabase.getInstance(MyApplication.getApplication()).markAsUploaded(rowIds, System.currentTimeMillis(), System.currentTimeMillis());
                    if (numberOfDeleted == 0) {
                        ocidUploadResult = UploadResult.DeleteFailed;
                        mlsUploadResult = UploadResult.DeleteFailed;
                        break;
                    }
                } else if (ocidSuccessful && isMlsUploadEnabled) {
                    Timber.d("upload(): Marking measurements as uploaded to OCID");
                    // keep for mls
                    int[] rowIds = getMeasurementIds(groupedMeasurements.get(UploadTarget.Ocid));
                    int numberOfDeleted = MeasurementsDatabase.getInstance(MyApplication.getApplication()).markAsUploaded(rowIds, System.currentTimeMillis(), null);
                    if (numberOfDeleted == 0) {
                        ocidUploadResult = UploadResult.DeleteFailed;
                        break;
                    }
                } else if (mlsSuccessful && isOpenCellIdUploadEnabled) {
                    Timber.d("upload(): Marking measurements as uploaded to MLS");
                    // keep for ocid
                    int[] rowIds = getMeasurementIds(groupedMeasurements.get(UploadTarget.Mls));
                    int numberOfDeleted = MeasurementsDatabase.getInstance(MyApplication.getApplication()).markAsUploaded(rowIds, null, System.currentTimeMillis());
                    if (numberOfDeleted == 0) {
                        mlsUploadResult = UploadResult.DeleteFailed;
                        break;
                    }
                } else {
                    Timber.d("upload(): Skipping delete because all uploads failed");
                    // all uploads failed - measurements were not uploaded
                }
            } else if ((isOpenCellIdUploadEnabled && ocidSuccessful) || (isMlsUploadEnabled && mlsSuccessful)) {
                Timber.d("upload(): Deleting measurements because OCID enabled = %s and successful = %s, MLS enabled = %s and successful = %s", isOpenCellIdUploadEnabled, ocidSuccessful, isMlsUploadEnabled, mlsSuccessful);
                // delete sent measurements
                int[] rowIds = getMeasurementIds(measurements);
                int numberOfDeleted = MeasurementsDatabase.getInstance(MyApplication.getApplication()).markAsUploaded(rowIds, System.currentTimeMillis(), System.currentTimeMillis());
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

        // clean anyway because it doesn't hurt
        MeasurementsDatabase.getInstance(MyApplication.getApplication()).clearOlderUploadedPartiallyAndUploadedFully();

        return new int[]{ocidSucceededParts, mlsSucceededParts};
    }

    private Map<UploadTarget, List<Measurement>> groupByUploaded(List<Measurement> measurements) {
        Map<UploadTarget, List<Measurement>> filtered = new HashMap<>();
        filtered.put(UploadTarget.Ocid, new ArrayList<Measurement>());
        filtered.put(UploadTarget.Mls, new ArrayList<Measurement>());
        if (measurements != null) {
            for (Measurement m : measurements) {
                if (m.getUploadedToOcidAt() == null) {
                    filtered.get(UploadTarget.Ocid).add(m);
                }
                if (m.getUploadedToMlsAt() == null) {
                    filtered.get(UploadTarget.Mls).add(m);
                }
            }
        }
        return filtered;
    }

    private int[] getMeasurementIds(List<Measurement> measurements) {
        int j = 0;
        int[] rowIds = new int[measurements.size()];
        for (Measurement m : measurements) {
            rowIds[j++] = m.getMeasurementId();
        }
        return rowIds;
    }

    private UploadResult uploadToOcid(List<Measurement> measurements) {
        if (measurements.isEmpty())
            return UploadResult.NoData;
        StringBuilder memoryFile = new StringBuilder();
        ICsvFormatter formatter = new CsvUploadFormatter();
        // write measurements
        try {
            memoryFile.append(formatter.formatHeader());
            for (Measurement m : measurements) {
                memoryFile.append(formatter.formatEntry(m));
            }
        } catch (Exception ex) {
            // this should never happen for in-memory writes
            Timber.e(ex, "uploadToOcid(): Error while generating file");
            MyApplication.handleSilentException(ex);
            return UploadResult.Failure;
        }
        // get content
        String csvContent = memoryFile.toString();
        // send request
        try {
            IUploadClient client = new OcidUploadClient(ocidUploadUrl, appId, ocidApiKey);
            RequestResult response = client.uploadMeasurements(csvContent);
            Timber.d("uploadToOcid(): Server response: %s", response);
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
                Timber.d("uploadToOcid(): Uploaded %s measurements", measurements.size());
                return UploadResult.PartiallySucceeded;
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported upload result %s", response));
            }
        } catch (SecurityException ex) {
            Timber.e(ex, "uploadToOcid(): Internet permission is denied");
            return UploadResult.PermissionDenied;
        }
    }

    private UploadResult uploadToMls(List<Measurement> measurements) {
        if (measurements.isEmpty())
            return UploadResult.NoData;
        StringBuilder memoryFile = new StringBuilder();
        IJsonFormatter formatter = new JsonMozillaUploadFormatter();
        // write measurements
        try {
            memoryFile.append(formatter.formatList(measurements));
        } catch (Exception ex) {
            // this should never happen for in-memory writes
            Timber.e(ex, "uploadToMls(): Error while generating file");
            MyApplication.handleSilentException(ex);
            return UploadResult.Failure;
        }
        // get content
        String jsonContent = memoryFile.toString();
        // send request
        try {
            IUploadClient client = new MozillaUploadClient(mlsUploadUrl, mlsApiKey);
            RequestResult response = client.uploadMeasurements(jsonContent);
            Timber.d("uploadToMls(): Server response: %s", response);
            // check whether it makes sense to continue
            if (response == RequestResult.ConfigurationError) {
                return UploadResult.InvalidData;
            } else if (response == RequestResult.ServerError) {
                return UploadResult.ServerError;
            } else if (response == RequestResult.ConnectionError) {
                return UploadResult.ConnectionError;
            } else if (response == RequestResult.LimitExceeded) {
                return UploadResult.LimitExceeded;
            } else if (response == RequestResult.Failure) {
                return UploadResult.Failure;
            } else if (response == RequestResult.Success) {
                Timber.d("uploadToMls(): Uploaded %s measurements", measurements.size());
                return UploadResult.PartiallySucceeded;
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported upload result %s", response));
            }
        } catch (SecurityException ex) {
            Timber.e(ex, "uploadToMls(): Internet permission is denied");
            return UploadResult.PermissionDenied;
        }
    }

    private enum UploadTarget {
        Ocid,
        Mls
    }
}
