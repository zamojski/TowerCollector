/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.analytics;

import android.app.Application;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

import info.zamojski.soft.towercollector.BuildConfig;
import info.zamojski.soft.towercollector.analytics.internal.Event;
import info.zamojski.soft.towercollector.analytics.internal.Label;
import info.zamojski.soft.towercollector.analytics.internal.Parameter;
import info.zamojski.soft.towercollector.analytics.internal.UserProperty;
import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;

public class GoogleAnalyticsReportingService implements IAnalyticsReportingService {

    private final FirebaseAnalytics analytics;

    public GoogleAnalyticsReportingService(Application application, boolean trackingEnabled, boolean sendEvents) {
        this.analytics = FirebaseAnalytics.getInstance(application);
        this.analytics.setAnalyticsCollectionEnabled(trackingEnabled && sendEvents);
        this.analytics.setUserProperty(UserProperty.Market, BuildConfig.MARKET_NAME);
    }

    @Override
    public void setTrackingEnabled(boolean trackingEnabled) {
        analytics.setAnalyticsCollectionEnabled(trackingEnabled);
    }

    @Override
    public void sendUpdateAction(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(Parameter.Source, source);
        this.analytics.logEvent(Event.UpdateSelected, bundle);
    }

    @Override
    public void sendMigrationFinished(long duration, int oldDbVersion, AnalyticsStatistics stats) {
        Bundle bundle = new Bundle();
        bundle.putString(Parameter.DbVersion, String.valueOf(oldDbVersion));
        bundle.putInt(Parameter.Locations, stats.getLocations());
        bundle.putInt(Parameter.Cells, stats.getCells());
        bundle.putInt(Parameter.Days, stats.getDays());
        bundle.putLong(Parameter.Duration, duration);
        this.analytics.logEvent(Event.DbMigrated, bundle);
    }

    @Override
    public void sendCollectorFinished(IntentSource source, String meansOfTransport, int apiVersion, long duration, AnalyticsStatistics stats, Map<NetworkGroup, Integer> collectedCellTypes) {
        Bundle bundle = new Bundle();
        bundle.putString(Parameter.Source, convertToStartLabel(source));
        bundle.putString(Parameter.MeansOfTransport, meansOfTransport);
        bundle.putInt(Parameter.CollectorApiVersion, apiVersion);
        bundle.putInt(Parameter.Locations, stats.getLocations());
        bundle.putInt(Parameter.Cells, stats.getCells());
        bundle.putLong(Parameter.Duration, duration);
        for (Map.Entry<NetworkGroup, Integer> entry : collectedCellTypes.entrySet()) {
            bundle.putInt(Parameter.NetworkType + "_" + entry.getKey(), entry.getValue());
        }
        this.analytics.logEvent(Event.MeasurementsCollected, bundle);
    }

    @Override
    public void sendUploadFinished(IntentSource source, String networkType, long duration, AnalyticsStatistics stats, String target) {
        Bundle bundle = new Bundle();
        bundle.putString(Parameter.Source, convertToStartLabel(source));
        bundle.putString(Parameter.Target, target);
        bundle.putString(Parameter.NetworkType, networkType);
        bundle.putInt(Parameter.Locations, stats.getLocations());
        bundle.putInt(Parameter.Cells, stats.getCells());
        bundle.putInt(Parameter.Days, stats.getDays());
        bundle.putLong(Parameter.Duration, duration);
        this.analytics.logEvent(Event.MeasurementsUploaded, bundle);
    }

    @Override
    public void sendExportFinished(IntentSource source, long duration, String fileType, AnalyticsStatistics stats) {
        Bundle bundle = new Bundle();
        bundle.putString(Parameter.Source, convertToStartLabel(source));
        bundle.putString(Parameter.FileType, fileType);
        bundle.putInt(Parameter.Locations, stats.getLocations());
        bundle.putInt(Parameter.Cells, stats.getCells());
        bundle.putInt(Parameter.Days, stats.getDays());
        bundle.putLong(Parameter.Duration, duration);
        this.analytics.logEvent(Event.MeasurementsExported, bundle);
    }

    @Override
    public void sendExportFinishedTotal(long duration, int numberOfFiles, AnalyticsStatistics stats) {
        Bundle bundle = new Bundle();
        bundle.putInt(Parameter.NumberOfFiles, numberOfFiles);
        bundle.putInt(Parameter.Locations, stats.getLocations());
        bundle.putInt(Parameter.Cells, stats.getCells());
        bundle.putInt(Parameter.Days, stats.getDays());
        bundle.putLong(Parameter.Duration, duration);
        this.analytics.logEvent(Event.MeasurementsExportedTotal, bundle);
    }

    @Override
    public void sendExportDeleteAction() {
        sendExportAction(Label.Delete);
    }

    @Override
    public void sendExportKeepAction() {
        sendExportAction(Label.Keep);
    }

    @Override
    public void sendExportShareAction() {
        sendExportAction(Label.Share);
    }

    @Override
    public void sendExportUploadAction() {
        sendExportAction(Label.Upload);
    }

    @Override
    public void sendPrefsUpdateCheckEnabled(boolean enabled) {
        Bundle bundle = new Bundle();
        bundle.putInt(Parameter.Enabled, enabled ? 1 : 0);
        this.analytics.logEvent(Event.PrefsUpdateCheckSelected, bundle);
    }

    @Override
    public void sendPrefsNotifyMeasurementsCollected(boolean enabled) {
        Bundle bundle = new Bundle();
        bundle.putInt(Parameter.Enabled, enabled ? 1 : 0);
        this.analytics.logEvent(Event.PrefsNotifyCollectedSelected, bundle);
    }

    @Override
    public void sendPrefsAppTheme(String theme) {
        Bundle bundle = new Bundle();
        bundle.putString(Parameter.AppTheme, theme);
        this.analytics.logEvent(Event.PrefsAppThemeSelected, bundle);
    }

    @Override
    public void sendPrefsCollectorApiVersion(int apiVersion) {
        Bundle bundle = new Bundle();
        bundle.putInt(Parameter.CollectorApiVersion, apiVersion);
        this.analytics.logEvent(Event.PrefsCollectorApiVersionSelected, bundle);
    }

    @Override
    public void sendHelpDialogOpened(String dialogName) {
        Bundle bundle = new Bundle();
        bundle.putString(Parameter.HelpSection, dialogName);
        this.analytics.logEvent(Event.PrefsHelpOpened, bundle);
    }

    private void sendExportAction(String action) {
        Bundle bundle = new Bundle();
        bundle.putString(Parameter.Action, action);
        this.analytics.logEvent(Event.ActionAfterExportSelected, bundle);
    }

    private String convertToStartLabel(IntentSource source) {
        switch (source) {
            case User:
                return Label.Start;
            case Application:
                return Label.StartIntent;
            case System:
                return Label.StartSystemIntent;
            case Shortcut:
                return Label.ShortcutIntent;
            case QuickSettingsTile:
                return Label.QuickSettingsTileIntent;
            default:
                throw new UnsupportedOperationException(String.format("Unsupported intent source '%s'", source));
        }
    }
}
