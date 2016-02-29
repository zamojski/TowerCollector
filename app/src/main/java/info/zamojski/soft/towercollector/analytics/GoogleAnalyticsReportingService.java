/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.analytics;

import info.zamojski.soft.towercollector.analytics.internal.Action;
import info.zamojski.soft.towercollector.analytics.internal.Category;
import info.zamojski.soft.towercollector.analytics.internal.Dimension;
import info.zamojski.soft.towercollector.analytics.internal.Label;
import info.zamojski.soft.towercollector.analytics.internal.Metric;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

//TODO: GA import com.google.analytics.tracking.android.EasyTracker;
//TODO: GA import com.google.analytics.tracking.android.Fields;
//TODO: GA import com.google.analytics.tracking.android.GAServiceManager;
//TODO: GA import com.google.analytics.tracking.android.GoogleAnalytics;
//TODO: GA import com.google.analytics.tracking.android.MapBuilder;
//TODO: GA import com.google.analytics.tracking.android.StandardExceptionParser;

public class GoogleAnalyticsReportingService implements IAnalyticsReportingService {

    private static final String TAG = GoogleAnalyticsReportingService.class.getSimpleName();

    private Context context;

    //TODO: GA private GoogleAnalytics analytics;
    //TODO: GA private EasyTracker tracker;

    public GoogleAnalyticsReportingService(Context context, boolean dryRun, boolean trackingEnabled) {
        this.context = context;

        //TODO: GA this.analytics = GoogleAnalytics.getInstance(context);

        Log.d(TAG, "ctor(): Setting dry run = " + dryRun);
        //TODO: GA this.analytics.setDryRun(dryRun);

        Log.d(TAG, "ctor(): Setting tracking enabled = " + trackingEnabled);
        //TODO: GA analytics.setAppOptOut(!trackingEnabled);

        //use manual dispatch on non-Google compatible devices (rest may be supported by Google Play Services)
        //TODO: GA GAServiceManager.getInstance().setLocalDispatchPeriod(0);

        //TODO: GA tracker = EasyTracker.getInstance(context);
    }

    @Override
    public void setAppOptOut(boolean optOut) {
        //TODO: GA analytics.setAppOptOut(optOut);
    }

    @Deprecated
    @Override
    public void startActivity(Activity activity) {
        //TODO: GA this.tracker.activityStart(activity);
    }

    @Deprecated
    @Override
    public void stopActivity(Activity activity) {
        //TODO: GA this.tracker.activityStop(activity);
    }

    @Override
    public void sendException(Throwable throwable, boolean isFatal) {
        //TODO: GA this.tracker.send(MapBuilder.createException(new StandardExceptionParser(context, null).getDescription(Thread.currentThread().getName(), throwable), isFatal).build());
    }

    @Override
    public void sendUpdateAction(String source) {
        //TODO: GA this.tracker.send(MapBuilder.createEvent(Category.Tasks, Action.Update, Label.Select, 1L)
        //TODO: GA         .set(Fields.customDimension(Dimension.UpdateSource), source)
        //TODO: GA         .build());
    }

    @Override
    public void sendMigrationStarted() {
        //TODO: GA this.tracker.send(MapBuilder.createEvent(Category.Tasks, Action.DbMigration, Label.Start, 1L)
        //TODO: GA         .build());
    }

    @Override
    public void sendMigrationFinished(long duration, int oldDbVersion, AnalyticsStatistics stats) {
        //TODO: GA this.tracker.send(MapBuilder.createEvent(Category.Tasks, Action.DbMigration, Label.Finish, 1L)
        //TODO: GA         .set(Fields.customDimension(Dimension.MigrationDbVersion), String.valueOf(oldDbVersion))
        //TODO: GA         .set(Fields.customMetric(Metric.StatisticsLocations), String.valueOf(stats.getLocations()))
        //TODO: GA         .set(Fields.customMetric(Metric.StatisticsCells), String.valueOf(stats.getCells()))
        //TODO: GA         .set(Fields.customMetric(Metric.StatisticsDays), String.valueOf(stats.getDays()))
        //TODO: GA         .set(Fields.customMetric(Metric.Duration), String.valueOf(duration))
        //TODO: GA         .build());
        //TODO: GA this.tracker.send(MapBuilder.createTiming(Category.Tasks, duration, Action.DbMigration, Label.Finish)
        //TODO: GA         .set(Fields.customDimension(Dimension.MigrationDbVersion), String.valueOf(oldDbVersion))
        //TODO: GA         .build());
    }

    @Override
    public void sendCollectorStarted(boolean byIntent) {
        //TODO: GA this.tracker.send(MapBuilder.createEvent(Category.Tasks, Action.Collect, (byIntent ? Label.StartIntent : Label.Start), 1L)
        //TODO: GA         .build());
    }

    @Override
    public void sendCollectorFinished(long duration, String meansOfTransport, AnalyticsStatistics stats) {
        //TODO: GA this.tracker.send(MapBuilder.createEvent(Category.Tasks, Action.Collect, Label.Finish, 1L)
        //TODO: GA         .set(Fields.customDimension(Dimension.CollectorMeansOfTrasport), meansOfTransport)
        //TODO: GA         .set(Fields.customMetric(Metric.CollectedLocationsInSession), String.valueOf(stats.getLocations()))
        //TODO: GA         .set(Fields.customMetric(Metric.CollectedCellsInSession), String.valueOf(stats.getCells()))
        //TODO: GA         .set(Fields.customMetric(Metric.Duration), String.valueOf(duration))
        //TODO: GA         .build());
        //TODO: GA this.tracker.send(MapBuilder.createTiming(Category.Tasks, duration, Action.Collect, Label.Finish)
        //TODO: GA         .set(Fields.customDimension(Dimension.CollectorMeansOfTrasport), meansOfTransport)
        //TODO: GA         .build());
    }

    @Override
    public void sendUploadStarted(boolean byIntent) {
        //TODO: GA this.tracker.send(MapBuilder.createEvent(Category.Tasks, Action.Upload, (byIntent ? Label.StartIntent : Label.Start), 1L)
        //TODO: GA         .build());
    }

    @Override
    public void sendUploadFinished(long duration, String networkType, AnalyticsStatistics stats) {
        //TODO: GA this.tracker.send(MapBuilder.createEvent(Category.Tasks, Action.Upload, Label.Finish, 1L)
        //TODO: GA         .set(Fields.customDimension(Dimension.UploadNetworkType), networkType)
        //TODO: GA         .set(Fields.customMetric(Metric.StatisticsLocations), String.valueOf(stats.getLocations()))
        //TODO: GA         .set(Fields.customMetric(Metric.StatisticsCells), String.valueOf(stats.getCells()))
        //TODO: GA         .set(Fields.customMetric(Metric.StatisticsDays), String.valueOf(stats.getDays()))
        //TODO: GA         .set(Fields.customMetric(Metric.Duration), String.valueOf(duration))
        //TODO: GA         .build());
        //TODO: GA this.tracker.send(MapBuilder.createTiming(Category.Tasks, duration, Action.Upload, Label.Finish)
        //TODO: GA         .set(Fields.customDimension(Dimension.UploadNetworkType), networkType)
        //TODO: GA         .build());
    }

    @Override
    public void sendExportStarted() {
        //TODO: GA this.tracker.send(MapBuilder.createEvent(Category.Tasks, Action.Export, Label.Start, 1L)
        //TODO: GA         .build());
    }

    @Override
    public void sendExportFinished(long duration, String fileType, AnalyticsStatistics stats) {
        //TODO: GA this.tracker.send(MapBuilder.createEvent(Category.Tasks, Action.Export, Label.Finish, 1L)
        //TODO: GA         .set(Fields.customDimension(Dimension.ExportFileType), fileType)
        //TODO: GA         .set(Fields.customMetric(Metric.StatisticsLocations), String.valueOf(stats.getLocations()))
        //TODO: GA         .set(Fields.customMetric(Metric.StatisticsCells), String.valueOf(stats.getCells()))
        //TODO: GA         .set(Fields.customMetric(Metric.StatisticsDays), String.valueOf(stats.getDays()))
        //TODO: GA         .set(Fields.customMetric(Metric.Duration), String.valueOf(duration))
        //TODO: GA         .build());
        //TODO: GA this.tracker.send(MapBuilder.createTiming(Category.Tasks, duration, Action.Export, Label.Finish)
        //TODO: GA         .set(Fields.customDimension(Dimension.ExportFileType), fileType)
        //TODO: GA         .build());
    }

    @Override
    public void sendExportDeleteAction() {
        //TODO: GA sendExportAction(Label.Delete);
    }

    @Override
    public void sendExportKeepAction() {
        sendExportAction(Label.Keep);
    }

    @Override
    public void sendExportUploadAction() {
        sendExportAction(Label.Upload);
    }

    private void sendExportAction(String action) {
        //TODO: GA this.tracker.send(MapBuilder.createEvent(Category.Tasks, Action.Export, action, 1L)
        //TODO: GA         .build());
    }
}
