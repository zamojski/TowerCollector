/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import java.util.Arrays;
import java.util.List;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ConfigurationBuilder;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import org.greenrobot.eventbus.EventBus;

import info.zamojski.soft.towercollector.analytics.GoogleAnalyticsReportingService;
import info.zamojski.soft.towercollector.analytics.IAnalyticsReportingService;
import info.zamojski.soft.towercollector.logging.AndroidFilePrinter;
import info.zamojski.soft.towercollector.providers.AppThemeProvider;
import info.zamojski.soft.towercollector.providers.preferences.PreferencesProvider;
import info.zamojski.soft.towercollector.utils.ApkUtils;

import android.Manifest;
import android.app.Application;

import info.zamojski.soft.towercollector.utils.PermissionUtils;
import trikita.log.Log;

public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getSimpleName();

    private static IAnalyticsReportingService analyticsService;
    private static MyApplication application;
    private static PreferencesProvider prefProvider;

    private static Thread.UncaughtExceptionHandler defaultHandler;

    private static int appTheme;

    private static String backgroundTaskName = null;

    // don't use BuildConfig as it sometimes doesn't set DEBUG to true
    private static final boolean EVENTBUS_SUBSCRIBER_CAN_THROW = true;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        // Logging to file is dependent on preferences but this will skip logging of initialization
        initPreferencesProvider();
        initLogger();
        initEventBus();
        initACRA();
        // Exception handling must be initialized after ACRA to obtain crash details
        initUnhandledExceptionHandler();
        initTheme();
        initGA();
    }

    private void initUnhandledExceptionHandler() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e("CRASHED", ex);
                defaultHandler.uncaughtException(thread, ex);
            }
        });
    }

    public void initLogger() {
        // Default configuration
        Log.usePrinter(Log.ANDROID, true).level(Log.D).useFormat(true);
        // File logging based on preferences
        String fileLoggingLevel = getPreferencesProvider().getFileLoggingLevel();
        if (fileLoggingLevel.equals(getString(R.string.preferences_file_logging_level_entries_value_disabled))) {
            Log.usePrinter(AndroidFilePrinter.getInstance(), false);
        } else {
            if (PermissionUtils.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (fileLoggingLevel.equals(getString(R.string.preferences_file_logging_level_entries_value_debug))) {
                    Log.level(Log.D);
                } else if (fileLoggingLevel.equals(getString(R.string.preferences_file_logging_level_entries_value_info))) {
                    Log.level(Log.I);
                } else if (fileLoggingLevel.equals(getString(R.string.preferences_file_logging_level_entries_value_warning))) {
                    Log.level(Log.W);
                } else if (fileLoggingLevel.equals(getString(R.string.preferences_file_logging_level_entries_value_error))) {
                    Log.level(Log.E);
                }
                Log.usePrinter(AndroidFilePrinter.getInstance(), true);
            }
        }
    }

    private void initEventBus() {
        Log.d("initEventBus(): Initializing EventBus");
        EventBus.builder()
                .throwSubscriberException(EVENTBUS_SUBSCRIBER_CAN_THROW)
                .installDefaultEventBus();
    }

    private void initPreferencesProvider() {
        Log.d("initProviders(): Initializing preferences");
        prefProvider = new PreferencesProvider(this);
    }

    public void initTheme() {
        Log.d("initTheme(): Initializing theme");
        String appThemeName = getPreferencesProvider().getAppTheme();
        AppThemeProvider themeProvider = new AppThemeProvider(this);
        appTheme = themeProvider.getTheme(appThemeName);
    }

    private void initGA() {
        Log.d("initGA(): Initializing Google Analytics");
        boolean trackingEnabled = getPreferencesProvider().getTrackingEnabled();
        boolean dryRun = ApkUtils.isApkDebuggable(application);
        analyticsService = new GoogleAnalyticsReportingService(this, trackingEnabled, dryRun);
    }

    private void initACRA() {
        Log.d("initACRA(): Initializing ACRA");
        ConfigurationBuilder configBuilder = new ConfigurationBuilder(this);
        // Configure connection
        configBuilder.setSendReportsInDevMode(BuildConfig.ACRA_SEND_REPORTS_IN_DEV_MODE);
        configBuilder.setFormUri(BuildConfig.ACRA_FORM_URI);
        configBuilder.setFormUriBasicAuthLogin(BuildConfig.ACRA_FORM_URI_BASIC_AUTH_LOGIN);
        configBuilder.setFormUriBasicAuthPassword(BuildConfig.ACRA_FORM_URI_BASIC_AUTH_PASSWORD);
        configBuilder.setHttpMethod(Method.valueOf(BuildConfig.ACRA_HTTP_METHOD));
        configBuilder.setReportType(Type.valueOf(BuildConfig.ACRA_REPORT_TYPE));
        configBuilder.setExcludeMatchingSharedPreferencesKeys(new String[]{"api_key"});
        // Configure reported content
        ReportField[] customReportContent = getCustomAcraReportFields();
        configBuilder.setCustomReportContent(customReportContent);
        ACRAConfiguration config = configBuilder.build();
        ACRA.init(this, config);
    }

    private ReportField[] getCustomAcraReportFields() {
        List<ReportField> customizedFields = Arrays.asList(ACRAConstants.DEFAULT_REPORT_FIELDS);
        // remove Device ID to make sure it will not be included in report
        customizedFields.remove(ReportField.DEVICE_ID);
        return customizedFields.toArray(new ReportField[customizedFields.size()]);
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
}
