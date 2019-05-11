/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.analytics;

import android.app.Application;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import info.zamojski.soft.towercollector.analytics.internal.Action;
import info.zamojski.soft.towercollector.analytics.internal.Category;
import info.zamojski.soft.towercollector.analytics.internal.Dimension;
import info.zamojski.soft.towercollector.analytics.internal.Event;
import info.zamojski.soft.towercollector.analytics.internal.Label;
import info.zamojski.soft.towercollector.analytics.internal.Metric;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;

public class GoogleAnalyticsReportingService implements IAnalyticsReportingService {

    private FirebaseAnalytics analytics;

    public GoogleAnalyticsReportingService(Application application, boolean trackingEnabled, boolean sendEvents) {
        this.analytics = FirebaseAnalytics.getInstance(application);
        this.analytics.setAnalyticsCollectionEnabled(trackingEnabled && sendEvents);
    }

    @Override
    public void setAppOptOut(boolean optOut) {
        analytics.setAnalyticsCollectionEnabled(!optOut);
    }

    @Override
    public void sendUpdateAction(String source) {
        Bundle event = createEventBundle(Category.Tasks, Action.Update, Label.Select, 1L);
        event.putString(Dimension.UpdateSource, source);
        this.analytics.logEvent(Event.Event, event);
    }

    @Override
    public void sendMigrationStarted() {
        Bundle event = createEventBundle(Category.Tasks, Action.DbMigration, Label.Start, 1L);
        this.analytics.logEvent(Event.Event, event);
    }

    @Override
    public void sendMigrationFinished(long duration, int oldDbVersion, AnalyticsStatistics stats) {
        Bundle event = createEventBundle(Category.Tasks, Action.DbMigration, Label.Finish, 1L);
        event.putString(Dimension.MigrationDbVersion, String.valueOf(oldDbVersion));
        event.putInt(Metric.StatisticsLocations, stats.getLocations());
        event.putInt(Metric.StatisticsCells, stats.getCells());
        event.putInt(Metric.StatisticsDays, stats.getDays());
        event.putLong(Metric.Duration, duration);
        this.analytics.logEvent(Event.Event, event);
        Bundle timing = createTimingBundle(Category.Tasks, Action.DbMigration, Label.Finish, duration);
        timing.putString(Dimension.MigrationDbVersion, String.valueOf(oldDbVersion));
        this.analytics.logEvent(Event.Timing, timing);
    }

    @Override
    public void sendCollectorStarted(IntentSource source) {
        Bundle event = createEventBundle(Category.Tasks, Action.Collect, convertToStartLabel(source), 1L);
        this.analytics.logEvent(Event.Event, event);
    }

    @Override
    public void sendCollectorFinished(long duration, String meansOfTransport, AnalyticsStatistics stats) {
        Bundle event = createEventBundle(Category.Tasks, Action.Collect, Label.Finish, 1L);
        event.putString(Dimension.CollectMeansOfTransport, meansOfTransport);
        event.putInt(Metric.CollectedLocationsInSession, stats.getLocations());
        event.putInt(Metric.CollectedCellsInSession, stats.getCells());
        event.putLong(Metric.Duration, duration);
        this.analytics.logEvent(Event.Event, event);
        Bundle timing = createTimingBundle(Category.Tasks, Action.Collect, Label.Finish, duration);
        timing.putString(Dimension.CollectMeansOfTransport, meansOfTransport);
        this.analytics.logEvent(Event.Timing, timing);
    }

    @Override
    public void sendCollectorApiVersionUsed(String apiVersion) {
        Bundle event = createEventBundle(Category.Runtime, Action.CollectorApiVersion, apiVersion, 1L);
        this.analytics.logEvent(Event.Event, event);
    }

    @Override
    public void sendUploadStarted(IntentSource source, boolean ocid) {
        Bundle event = createEventBundle(Category.Tasks, ocid ? Action.UploadOcid : Action.UploadMls, convertToStartLabel(source), 1L);
        this.analytics.logEvent(Event.Event, event);
    }

    @Override
    public void sendUploadFinished(long duration, String networkType, AnalyticsStatistics stats, boolean ocid) {
        Bundle event = createEventBundle(Category.Tasks, ocid ? Action.UploadOcid : Action.UploadMls, Label.Finish, 1L);
        event.putString(Dimension.UploadNetworkType, networkType);
        event.putInt(Metric.StatisticsLocations, stats.getLocations());
        event.putInt(Metric.StatisticsCells, stats.getCells());
        event.putInt(Metric.StatisticsDays, stats.getDays());
        event.putLong(Metric.Duration, duration);
        this.analytics.logEvent(Event.Event, event);
        Bundle timing = createTimingBundle(Category.Tasks, ocid ? Action.UploadOcid : Action.UploadMls, Label.Finish, duration);
        timing.putString(Dimension.UploadNetworkType, networkType);
        this.analytics.logEvent(Event.Timing, timing);
    }

    @Override
    public void sendExportStarted() {
        Bundle event = createEventBundle(Category.Tasks, Action.Export, Label.Start, 1L);
        this.analytics.logEvent(Event.Event, event);
    }

    @Override
    public void sendExportFinished(long duration, String fileType, AnalyticsStatistics stats) {
        Bundle event = createEventBundle(Category.Tasks, Action.Export, Label.Finish, 1L);
        event.putString(Dimension.ExportFileType, fileType);
        event.putInt(Metric.StatisticsLocations, stats.getLocations());
        event.putInt(Metric.StatisticsCells, stats.getCells());
        event.putInt(Metric.StatisticsDays, stats.getDays());
        event.putLong(Metric.Duration, duration);
        this.analytics.logEvent(Event.Event, event);
        Bundle timing = createTimingBundle(Category.Tasks, Action.Export, Label.Finish, duration);
        timing.putString(Dimension.ExportFileType, fileType);
        this.analytics.logEvent(Event.Timing, timing);
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
        Bundle event = createEventBundle(Category.Preferences, Action.AppTheme, theme, 1L);
        this.analytics.logEvent(Event.Event, event);
    }

    @Override
    public void sendPrefsCollectorApiVersion(String apiVersion) {
        Bundle event = createEventBundle(Category.Preferences, Action.CollectorApiVersion, apiVersion, 1L);
        this.analytics.logEvent(Event.Event, event);
    }

    @Override
    public void sendHelpDialogOpened(String dialogName) {
        Bundle event = createEventBundle(Category.Help, Action.Open, dialogName, 1L);
        this.analytics.logEvent(Event.Event, event);
    }

    private void sendExportAction(String action) {
        Bundle event = createEventBundle(Category.Tasks, Action.Export, action, 1L);
        this.analytics.logEvent(Event.Event, event);
    }

    private void sendBooleanValue(String action, boolean value) {
        Bundle event = createEventBundle(Category.Preferences, action, Label.Usage, value ? 1L : 0L);
        this.analytics.logEvent(Event.Event, event);
    }

    private Bundle createEventBundle(String category, String action, String label, long value) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, category);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, action);
        bundle.putString(FirebaseAnalytics.Param.ITEM_VARIANT, label);
        bundle.putLong(FirebaseAnalytics.Param.QUANTITY, value);
        return bundle;
    }

    private Bundle createTimingBundle(String category, String action, String label, long duration) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, category);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, action);
        bundle.putString(FirebaseAnalytics.Param.ITEM_VARIANT, label);
        bundle.putLong(FirebaseAnalytics.Param.QUANTITY, duration);
        return bundle;
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
