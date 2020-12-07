/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.config.NotificationConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import info.zamojski.soft.towercollector.analytics.AnalyticsServiceFactory;
import info.zamojski.soft.towercollector.analytics.IAnalyticsReportingService;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.logging.ConsoleLoggingTree;
import info.zamojski.soft.towercollector.logging.FileLoggingTree;
import info.zamojski.soft.towercollector.providers.AppThemeProvider;
import info.zamojski.soft.towercollector.providers.preferences.PreferencesProvider;
import info.zamojski.soft.towercollector.utils.ExceptionUtils;
import info.zamojski.soft.towercollector.utils.HashUtils;
import info.zamojski.soft.towercollector.utils.PermissionUtils;

import android.Manifest;
import android.app.Application;
import android.app.NotificationManager;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import android.os.DeadObjectException;
import android.util.Log;
import android.widget.Toast;

import timber.log.Timber;

public class MyApplication extends Application {

    private static IAnalyticsReportingService analyticsService;
    private static MyApplication application;
    private static PreferencesProvider prefProvider;

    private static Thread.UncaughtExceptionHandler defaultHandler;

    private static int appTheme;
    private static int popupTheme;

    private static String backgroundTaskName = null;

    private static Set<String> handledSilentExceptionHashes = new HashSet<>();

    // don't use BuildConfig as it sometimes doesn't set DEBUG to true
    private static final boolean EVENTBUS_SUBSCRIBER_CAN_THROW = true;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        // Logging to file is dependent on preferences but this will skip logging of initialization
        initPreferencesProvider();
        initLogger();
        initACRA();
        // Exception handling must be initialized after ACRA to obtain crash details
        initUnhandledExceptionHandler();
        initEventBus();
        initTheme();
        initAnalytics();
    }

    static {
        // Enable VectorDrawable support for API < 21
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private void initUnhandledExceptionHandler() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NotNull Thread thread, @NotNull Throwable ex) {
                Timber.e(ex, "CRASHED");
                if (ExceptionUtils.getRootCause(ex) instanceof SQLiteDatabaseCorruptException) {
                    MeasurementsDatabase.deleteDatabase(getApplication());
                }
                // strange but it happens that app is tested on devices with lower SDK - don't send ACRA reports
                // also ignore errors caused by system failures
                if (isSdkVersionSupported() && !hasSystemDied(ex) && !isAndroid10TelephonyManagerLambdaBug(ex)) {
                    defaultHandler.uncaughtException(thread, ex);
                }
            }
        });
    }

    private boolean isSdkVersionSupported() {
        return Build.VERSION.SDK_INT >= BuildConfig.MIN_SDK_VERSION;
    }

    private boolean hasSystemDied(Throwable ex) {
        return ExceptionUtils.getRootCause(ex) instanceof DeadObjectException;
    }

    private boolean isAndroid10TelephonyManagerLambdaBug(Throwable ex) {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.Q)
            return false;
        String stackTrace = ex.toString();
        Boolean isType = ex instanceof NullPointerException;
        Boolean isTypeString = stackTrace.contains("java.lang.NullPointerException");
        Boolean containsParcelableException = stackTrace.contains("ParcelableException.getCause()");
        Boolean containsTelephonyManager = stackTrace.contains("TelephonyManager");
        Boolean containsLambdaOnError = stackTrace.contains("lambda$onError");
        ACRA.getErrorReporter().putCustomData("isType", isType.toString());
        ACRA.getErrorReporter().putCustomData("isTypeString", isTypeString.toString());
        ACRA.getErrorReporter().putCustomData("containsParcelableException", containsParcelableException.toString());
        ACRA.getErrorReporter().putCustomData("containsTelephonyManager", containsTelephonyManager.toString());
        ACRA.getErrorReporter().putCustomData("containsLambdaOnError", containsLambdaOnError.toString());
        return (isType || isTypeString)
                && containsParcelableException
                && containsTelephonyManager
                && containsLambdaOnError;
    }

    public void initLogger() {
        // Default configuration
        int consoleLogLevel = BuildConfig.DEBUG ? Log.VERBOSE : Log.INFO;
        // File logging based on preferences
        String fileLoggingLevelString = getPreferencesProvider().getFileLoggingLevel();
        if (fileLoggingLevelString.equals(getString(R.string.preferences_file_logging_level_entries_value_disabled))) {
            if (Timber.forest().contains(FileLoggingTree.INSTANCE)) {
                Timber.uproot(FileLoggingTree.INSTANCE);
            }
        } else {
            if (PermissionUtils.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                int fileLogLevel = Log.ERROR;
                if (fileLoggingLevelString.equals(getString(R.string.preferences_file_logging_level_entries_value_debug))) {
                    fileLogLevel = Log.DEBUG;
                } else if (fileLoggingLevelString.equals(getString(R.string.preferences_file_logging_level_entries_value_info))) {
                    fileLogLevel = Log.INFO;
                } else if (fileLoggingLevelString.equals(getString(R.string.preferences_file_logging_level_entries_value_warning))) {
                    fileLogLevel = Log.WARN;
                } else if (fileLoggingLevelString.equals(getString(R.string.preferences_file_logging_level_entries_value_error))) {
                    fileLogLevel = Log.ERROR;
                }
                consoleLogLevel = Math.min(consoleLogLevel, fileLogLevel);
                if (Timber.forest().contains(FileLoggingTree.INSTANCE)) {
                    Timber.uproot(FileLoggingTree.INSTANCE);
                }
                Timber.plant(FileLoggingTree.INSTANCE.setPriority(fileLogLevel));
            } else {
                Toast.makeText(this, R.string.permission_logging_denied_temporarily_message, Toast.LENGTH_LONG).show();
            }
        }
        Timber.plant(ConsoleLoggingTree.INSTANCE.setPriority(consoleLogLevel));
    }

    private void initEventBus() {
        Timber.d("initEventBus(): Initializing EventBus");
        EventBus.builder()
                .throwSubscriberException(EVENTBUS_SUBSCRIBER_CAN_THROW)
                .installDefaultEventBus();
    }

    private void initPreferencesProvider() {
        Timber.d("initProviders(): Initializing preferences");
        prefProvider = new PreferencesProvider(this);
    }

    public void initTheme() {
        Timber.d("initTheme(): Initializing theme");
        String appThemeName = getPreferencesProvider().getAppTheme();
        AppThemeProvider themeProvider = new AppThemeProvider(this);
        appTheme = themeProvider.getAppTheme(appThemeName);
        popupTheme = themeProvider.getPopupTheme(appThemeName);
    }

    private void initAnalytics() {
        Timber.d("initAnalytics(): Initializing analytics");
        analyticsService = new AnalyticsServiceFactory().createInstance();
    }

    private void initACRA() {
        Timber.d("initACRA(): Initializing ACRA");

        CoreConfigurationBuilder configBuilder = new CoreConfigurationBuilder(this);
        // Configure connection
        configBuilder.setBuildConfigClass(BuildConfig.class);
        configBuilder.setEnabled(BuildConfig.ACRA_SEND_REPORTS_IN_DEV_MODE);
        configBuilder.setSendReportsInDevMode(BuildConfig.ACRA_SEND_REPORTS_IN_DEV_MODE);
        configBuilder.setReportFormat(StringFormat.valueOf(BuildConfig.ACRA_REPORT_TYPE));
        configBuilder.setExcludeMatchingSharedPreferencesKeys(getString(R.string.preferences_api_key_key));
        configBuilder.setReportContent(getCustomAcraReportFields());
        // Configure reported content
        HttpSenderConfigurationBuilder httpPluginConfigBuilder = configBuilder.getPluginConfigurationBuilder(HttpSenderConfigurationBuilder.class);
        httpPluginConfigBuilder.setUri(BuildConfig.ACRA_FORM_URI);
        httpPluginConfigBuilder.setBasicAuthLogin(BuildConfig.ACRA_FORM_URI_BASIC_AUTH_LOGIN);
        httpPluginConfigBuilder.setBasicAuthPassword(BuildConfig.ACRA_FORM_URI_BASIC_AUTH_PASSWORD);
        httpPluginConfigBuilder.setHttpMethod(HttpSender.Method.valueOf(BuildConfig.ACRA_HTTP_METHOD));
        httpPluginConfigBuilder.setEnabled(true);
        // Configure interaction method
        NotificationConfigurationBuilder notificationConfigBuilder = configBuilder.getPluginConfigurationBuilder(NotificationConfigurationBuilder.class);
        notificationConfigBuilder.setResChannelName(R.string.error_reporting_notification_channel_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationConfigBuilder.setResChannelImportance(NotificationManager.IMPORTANCE_DEFAULT);
        }
        notificationConfigBuilder.setResIcon(R.drawable.ic_notification);
        notificationConfigBuilder.setResTitle(R.string.error_reporting_notification_title);
        notificationConfigBuilder.setResText(R.string.error_reporting_notification_text);
        notificationConfigBuilder.setResTickerText(R.string.error_reporting_notification_title);
        notificationConfigBuilder.setResSendButtonText(R.string.dialog_send);
        notificationConfigBuilder.setResDiscardButtonText(R.string.dialog_cancel);
        notificationConfigBuilder.setSendOnClick(true);
        notificationConfigBuilder.setResSendWithCommentButtonText(R.string.dialog_send_comment);
        notificationConfigBuilder.setResCommentPrompt(R.string.error_reporting_notification_comment_prompt);
        notificationConfigBuilder.setEnabled(!getPreferencesProvider().getReportErrorsSilently());

        ACRA.init(this, configBuilder);
        ACRA.getErrorReporter().putCustomData("APP_MARKET_NAME", BuildConfig.MARKET_NAME);
    }

    private ReportField[] getCustomAcraReportFields() {
        List<ReportField> customizedFields = new ArrayList<ReportField>(Arrays.asList(ACRAConstants.DEFAULT_REPORT_FIELDS));
        // remove Device ID to make sure it will not be included in report
        customizedFields.remove(ReportField.DEVICE_ID);
        // remove BuildConfig to avoid leakage of configuration data in report
        customizedFields.remove(ReportField.BUILD_CONFIG);
        return customizedFields.toArray(new ReportField[0]);
    }

    public static IAnalyticsReportingService getAnalytics() {
        return analyticsService;
    }

    public static MyApplication getApplication() {
        return application;
    }

    public static int getCurrentAppTheme() {
        return appTheme;
    }

    public static int getCurrentPopupTheme() {
        return popupTheme;
    }

    public static PreferencesProvider getPreferencesProvider() {
        return prefProvider;
    }

    public synchronized static void startBackgroundTask(Object task) {
        backgroundTaskName = task.getClass().getName();
    }

    public synchronized static void stopBackgroundTask() {
        backgroundTaskName = null;
    }

    public synchronized static String getBackgroundTaskName() {
        return backgroundTaskName;
    }

    public synchronized static boolean isBackgroundTaskRunning(Class clazz) {
        return (backgroundTaskName != null && backgroundTaskName.equals(clazz.getName()));
    }

    public synchronized static void handleSilentException(Throwable throwable) {
        String throwableHash = HashUtils.toSha1(throwable.toString());
        if (!handledSilentExceptionHashes.contains(throwableHash)) {
            handledSilentExceptionHashes.add(throwableHash);
            ACRA.getErrorReporter().handleSilentException(throwable);
        }
    }
}
