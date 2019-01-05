/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.analytics;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.analytics.internal.Action;
import info.zamojski.soft.towercollector.analytics.internal.Category;
import info.zamojski.soft.towercollector.analytics.internal.Dimension;
import info.zamojski.soft.towercollector.analytics.internal.Label;
import info.zamojski.soft.towercollector.analytics.internal.Metric;
import info.zamojski.soft.towercollector.analytics.internal.Screens;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

public class GoogleAnalyticsReportingService implements IAnalyticsReportingService {

    private Application application;

    private GoogleAnalytics analytics;
    private Tracker tracker;

    public GoogleAnalyticsReportingService(Application application, boolean trackingEnabled, boolean dryRun) {
        this.application = application;

        this.analytics = GoogleAnalytics.getInstance(application);
        this.analytics.setAppOptOut(!trackingEnabled);
        this.analytics.setDryRun(dryRun);

        this.tracker = analytics.newTracker(R.xml.global_tracker);
    }

    @Override
    public void setAppOptOut(boolean optOut) {
        analytics.setAppOptOut(optOut);
    }

    @Override
    public void sendException(Throwable throwable, boolean isFatal) {
        this.tracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription(new StandardExceptionParser(this.application, null).getDescription(Thread.currentThread().getName(), throwable))
                .setFatal(isFatal)
                .build());
    }

    @Override
    public void sendMainActivityStarted() {
        this.tracker.setScreenName(Screens.MainActivity);
        this.tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void sendMainActivityStopped() {
        this.tracker.setScreenName(null);
    }

    @Override
    public void sendPreferencesActivityStarted() {
        this.tracker.setScreenName(Screens.PreferencesActivity);
        this.tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void sendPreferencesActivityStopped() {
        this.tracker.setScreenName(null);
    }

    @Override
    public void sendUpdateAction(String source) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Tasks, Action.Update)
                .setLabel(Label.Select)
                .setValue(1L)
                .setCustomDimension(Dimension.UpdateSource, source)
                .build());
    }

    @Override
    public void sendMigrationStarted() {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Tasks, Action.DbMigration)
                .setLabel(Label.Start)
                .setValue(1L)
                .build());
    }

    @Override
    public void sendMigrationFinished(long duration, int oldDbVersion, AnalyticsStatistics stats) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Tasks, Action.DbMigration)
                .setLabel(Label.Finish)
                .setValue(1L)
                .setCustomDimension(Dimension.MigrationDbVersion, String.valueOf(oldDbVersion))
                .setCustomMetric(Metric.StatisticsLocations, stats.getLocations())
                .setCustomMetric(Metric.StatisticsCells, stats.getCells())
                .setCustomMetric(Metric.StatisticsDays, stats.getDays())
                .setCustomMetric(Metric.Duration, duration)
                .build());
        this.tracker.send(new HitBuilders.TimingBuilder(Category.Tasks, Action.DbMigration, duration)
                .setLabel(Label.Finish)
                .setCustomDimension(Dimension.MigrationDbVersion, String.valueOf(oldDbVersion))
                .build());
    }

    @Override
    public void sendCollectorStarted(IntentSource source) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Tasks, Action.Collect)
                .setLabel(convertToStartLabel(source))
                .setValue(1L)
                .build());
    }

    @Override
    public void sendCollectorFinished(long duration, String meansOfTransport, AnalyticsStatistics stats) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Tasks, Action.Collect)
                .setLabel(Label.Finish)
                .setValue(1L)
                .setCustomDimension(Dimension.CollectorMeansOfTrasport, meansOfTransport)
                .setCustomMetric(Metric.CollectedLocationsInSession, stats.getLocations())
                .setCustomMetric(Metric.CollectedCellsInSession, stats.getCells())
                .setCustomMetric(Metric.Duration, duration)
                .build());
        this.tracker.send(new HitBuilders.TimingBuilder(Category.Tasks, Action.Collect, duration)
                .setLabel(Label.Finish)
                .setCustomDimension(Dimension.CollectorMeansOfTrasport, meansOfTransport)
                .build());
    }

    @Override
    public void sendCollectorApiVersionUsed(String apiVersion) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Runtime, Action.CollectorApiVersion)
                .setLabel(apiVersion)
                .setValue(1L)
                .build());
    }

    @Override
    public void sendUploadStarted(IntentSource source, boolean ocid) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Tasks, ocid ? Action.UploadOcid : Action.UploadMls)
                .setLabel(convertToStartLabel(source))
                .setValue(1L)
                .build());
    }

    @Override
    public void sendUploadFinished(long duration, String networkType, AnalyticsStatistics stats, boolean ocid) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Tasks, ocid ? Action.UploadOcid : Action.UploadMls)
                .setLabel(Label.Finish)
                .setValue(1L)
                .setCustomDimension(Dimension.UploadNetworkType, networkType)
                .setCustomMetric(Metric.StatisticsLocations, stats.getLocations())
                .setCustomMetric(Metric.StatisticsCells, stats.getCells())
                .setCustomMetric(Metric.StatisticsDays, stats.getDays())
                .setCustomMetric(Metric.Duration, duration)
                .build());
        this.tracker.send(new HitBuilders.TimingBuilder(Category.Tasks, ocid ? Action.UploadOcid : Action.UploadMls, duration)
                .setLabel(Label.Finish)
                .setCustomDimension(Dimension.UploadNetworkType, networkType)
                .build());
    }

    @Override
    public void sendExportStarted() {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Tasks, Action.Export)
                .setLabel(Label.Start)
                .setValue(1L)
                .build());
    }

    @Override
    public void sendExportFinished(long duration, String fileType, AnalyticsStatistics stats) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Tasks, Action.Export)
                .setLabel(Label.Finish)
                .setValue(1L)
                .setCustomDimension(Dimension.ExportFileType, fileType)
                .setCustomMetric(Metric.StatisticsLocations, stats.getLocations())
                .setCustomMetric(Metric.StatisticsCells, stats.getCells())
                .setCustomMetric(Metric.StatisticsDays, stats.getDays())
                .setCustomMetric(Metric.Duration, duration)
                .build());
        this.tracker.send(new HitBuilders.TimingBuilder(Category.Tasks, Action.Export, duration)
                .setLabel(Label.Finish)
                .setCustomDimension(Dimension.ExportFileType, fileType)
                .build());
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
    public void sendExportUploadAction() {
        sendExportAction(Label.Upload);
    }

    @Override
    public void sendPrefsUpdateCheckEnabled(boolean enabled) {
        sendBooleanValue(Action.UpdateCheckEnabled, enabled);
    }

    @Override
    public void sendPrefsNotifyMeasurementsCollected(boolean enabled) {
        sendBooleanValue(Action.NotifyMeasurementsCollectedEnabled, enabled);
    }

    @Override
    public void sendPrefsAppTheme(String theme) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Preferences, Action.AppTheme)
                .setLabel(theme)
                .setValue(1L)
                .build());
    }

    @Override
    public void sendPrefsCollectorApiVersion(String apiVersion) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Preferences, Action.CollectorApiVersion)
                .setLabel(apiVersion)
                .setValue(1L)
                .build());
    }

    @Override
    public void sendHelpDialogOpened(String dialogName) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Help, Action.Open)
                .setLabel(dialogName)
                .setValue(1L)
                .build());
    }

    private void sendExportAction(String action) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Tasks, Action.Export)
                .setLabel(action)
                .setValue(1L)
                .build());
    }

    private void sendBooleanValue(String action, boolean value) {
        this.tracker.send(new HitBuilders.EventBuilder(Category.Preferences, action)
                .setLabel(Label.Usage)
                .setValue(value ? 1L : 0L)
                .build());
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
            default:
                throw new UnsupportedOperationException(String.format("Unsupported intent source '%s'", source));
        }
    }
}
