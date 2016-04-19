/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import java.util.Arrays;
import java.util.List;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import de.greenrobot.event.EventBus;
import info.zamojski.soft.towercollector.analytics.GoogleAnalyticsReportingService;
import info.zamojski.soft.towercollector.analytics.IAnalyticsReportingService;
import info.zamojski.soft.towercollector.providers.AppThemeProvider;
import info.zamojski.soft.towercollector.providers.preferences.PreferencesProvider;

import android.app.Application;
import trikita.log.Log;

public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getSimpleName();

    private static IAnalyticsReportingService analyticsService;
    private static MyApplication application;
    private static PreferencesProvider prefProvider;

    private static int appTheme;

    private static String backgroundTaskName = null;

    // don't use BuildConfig as it sometimes doesn't set DEBUG to true
    private static final boolean EVENTBUS_SUBSCRIBER_CAN_THROW = true;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        initEventBus();
        initACRA();
        initProviders();
        initTheme();
        initGA();
    }

    private void initEventBus() {
        Log.d(TAG, "initEventBus(): Initializing EventBus");
        EventBus.builder()
                .throwSubscriberException(EVENTBUS_SUBSCRIBER_CAN_THROW)
                .installDefaultEventBus();
    }

    private void initProviders() {
        Log.d(TAG, "initProviders(): Initializing preferences");
        prefProvider = new PreferencesProvider(this);
    }

    public void initTheme() {
        Log.d(TAG, "initTheme(): Initializing theme");
        String appThemeName = getPreferencesProvider().getAppTheme();
        AppThemeProvider themeProvider = new AppThemeProvider(this);
        appTheme = themeProvider.getTheme(appThemeName);
    }

    private void initGA() {
        Log.d(TAG, "initGA(): Initializing Google Analytics");
        boolean trackingEnabled = getPreferencesProvider().getTrackingEnabled();
        analyticsService = new GoogleAnalyticsReportingService(this, trackingEnabled);
    }

    private void initACRA() {
        Log.d(TAG, "initACRA(): Initializing ACRA");
        ACRAConfiguration config = ACRA.getNewDefaultConfig(this);
        // Configure connection
        config.setSendReportsInDevMode(BuildConfig.ACRA_SEND_REPORTS_IN_DEV_MODE);
        config.setFormUri(BuildConfig.ACRA_FORM_URI);
        config.setFormUriBasicAuthLogin(BuildConfig.ACRA_FORM_URI_BASIC_AUTH_LOGIN);
        config.setFormUriBasicAuthPassword(BuildConfig.ACRA_FORM_URI_BASIC_AUTH_PASSWORD);
        config.setHttpMethod(Method.valueOf(BuildConfig.ACRA_HTTP_METHOD));
        config.setReportType(Type.valueOf(BuildConfig.ACRA_REPORT_TYPE));
        config.setExcludeMatchingSharedPreferencesKeys(new String[]{"api_key"});
        // Configure reported content
        ReportField[] customReportContent = getCustomAcraReportFields();
        config.setCustomReportContent(customReportContent);
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
}
