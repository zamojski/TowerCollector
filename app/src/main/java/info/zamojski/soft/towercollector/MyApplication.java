/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.DialogConfigurationBuilder;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.config.LimiterConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import info.zamojski.soft.towercollector.analytics.AnalyticsServiceFactory;
import info.zamojski.soft.towercollector.analytics.IAnalyticsReportingService;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.dev.DatabaseOperations;
import info.zamojski.soft.towercollector.logging.ConsoleLoggingTree;
import info.zamojski.soft.towercollector.logging.FileLoggingTree;
import info.zamojski.soft.towercollector.providers.AppThemeProvider;
import info.zamojski.soft.towercollector.providers.preferences.PreferencesProvider;
import info.zamojski.soft.towercollector.utils.ExceptionUtils;
import info.zamojski.soft.towercollector.utils.HashUtils;

import android.app.Application;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import android.os.DeadObjectException;
import android.util.Log;
import android.widget.Toast;

import info.zamojski.soft.towercollector.utils.PermissionUtils;
import info.zamojski.soft.towercollector.utils.StorageUtils;
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
                if (isSdkVersionSupported() && !hasSystemDied(ex) && !isAndroid10TelephonyManagerLambdaBug(ex) && !hasFileSystemInBadStage(ex)) {
                    if (isDatabaseCorrupted(ex)) {
                        String dbString = DatabaseOperations.getDatabaseBaseString(getApplication());
                        ACRA.getErrorReporter().putCustomData("DB", dbString);
                        if (!MeasurementsDatabase.getInstance(getApplication()).hasValidSchema()) {
                            MeasurementsDatabase.deleteDatabase(getApplication());
                        }
                    }
                    // include list of permissions at the moment of crash
                    ACRA.getErrorReporter().putCustomData("CRASH_PERMISSIONS", PermissionUtils.getAppPermissions());
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
        String stackTrace = getFullStackTrace(ex);
        boolean isType = ex instanceof NullPointerException;
        boolean isTypeString = stackTrace.contains("java.lang.NullPointerException");
        boolean containsParcelableException = stackTrace.contains("ParcelableException.getCause()");
        boolean containsTelephonyManager = stackTrace.contains("TelephonyManager");
        boolean containsLambdaOnError = stackTrace.contains("lambda$onError");
        return (isType || isTypeString)
                && containsParcelableException
                && containsTelephonyManager
                && containsLambdaOnError;
    }

    private boolean isDatabaseCorrupted(Throwable ex) {
        String stackTrace = getFullStackTrace(ex);
        boolean isCorrupted = stackTrace.contains("SQLITE_IOERR_SHORT_READ");
        return isCorrupted;
    }

    private boolean hasFileSystemInBadStage(Throwable ex) {
        boolean isType = ex instanceof IllegalStateException;
        boolean isInBadState = ex.getMessage() != null && ex.getMessage().contains("The file system on the device is in a bad state.");
        return isType && isInBadState;
    }

    private String getFullStackTrace(Throwable ex) {
        try {
            ByteArrayOutputStream stackTraceStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(stackTraceStream);
            ex.printStackTrace(printStream);
            printStream.close();
            return stackTraceStream.toString();
        } catch (Throwable ignore) {
            return ex.toString();
        }
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
            Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
            if (StorageUtils.canWriteStorageUri(storageUri)) {
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
        // enable for diagnostics only ACRA.DEV_LOGGING = true;
        ACRA.init(this, new CoreConfigurationBuilder()
                // Configure connection
                .withBuildConfigClass(BuildConfig.class)
                .withSendReportsInDevMode(BuildConfig.ACRA_SEND_REPORTS_IN_DEV_MODE)
                .withReportFormat(StringFormat.valueOf(BuildConfig.ACRA_REPORT_TYPE))
                .withExcludeMatchingSharedPreferencesKeys(getString(R.string.preferences_opencellid_api_key_key))
                .withReportContent(getCustomAcraReportFields())
                .withLogcatArguments("-t", "250", "-v", "time")
                .withPluginConfigurations(
                    // Configure reported content
                    new HttpSenderConfigurationBuilder()
                        .withUri(BuildConfig.ACRA_FORM_URI)
                        .withBasicAuthLogin(BuildConfig.ACRA_FORM_URI_BASIC_AUTH_LOGIN)
                        .withBasicAuthPassword(BuildConfig.ACRA_FORM_URI_BASIC_AUTH_PASSWORD)
                        .withHttpMethod(HttpSender.Method.valueOf(BuildConfig.ACRA_HTTP_METHOD))
                        .withEnabled(true)
                        .build(),
                    // Configure interaction method
                    new DialogConfigurationBuilder()
                        .withText(getString(R.string.error_reporting_notification_text))
                        .withTitle(getString(R.string.error_reporting_notification_title))
                        .withPositiveButtonText(getString(R.string.dialog_send))
                        .withNegativeButtonText(getString(R.string.dialog_cancel))
                        .withCommentPrompt(getString(R.string.error_reporting_notification_comment_prompt))
                        .withEmailPrompt(getString(R.string.error_reporting_notification_email_prompt))
                        .withResIcon(R.drawable.ic_notification)
                        .withResTheme(R.style.LightAppTheme)
                        .withEnabled(!getPreferencesProvider().getReportErrorsSilently())
                        .build(),
                    // Configure limits for one device
                    new LimiterConfigurationBuilder()
                        .withStacktraceLimit(2)
                        .withExceptionClassLimit(3)
                        .withOverallLimit(5)
                        .withPeriod(2)
                        .withPeriodUnit(TimeUnit.DAYS)
                        .withResetLimitsOnAppUpdate(true)
                        .withEnabled(true)
                        .build()
                )
        );
        ACRA.getErrorReporter().putCustomData("APP_MARKET_NAME", BuildConfig.MARKET_NAME);
        // include list of permissions at startup
        ACRA.getErrorReporter().putCustomData("STARTUP_PERMISSIONS", PermissionUtils.getAppPermissions());
    }

    private ReportField[] getCustomAcraReportFields() {
        List<ReportField> customizedFields = new ArrayList<>(ACRAConstants.DEFAULT_REPORT_FIELDS);
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
            // include list of permissions at the moment of crash
            ACRA.getErrorReporter().putCustomData("CRASH_PERMISSIONS", PermissionUtils.getAppPermissions());
            ACRA.getErrorReporter().handleSilentException(throwable);
        }
    }
}
