/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.analytics;

import info.zamojski.soft.towercollector.model.AnalyticsStatistics;

public interface IAnalyticsReportingService {

    void setTrackingEnabled(boolean trackingEnabled);

    void sendUpdateAction(String source);

    void sendMigrationFinished(long duration, int oldDbVersion, AnalyticsStatistics stats);

    void sendCollectorFinished(IntentSource source, String meansOfTransport, int apiVersion, long duration, AnalyticsStatistics stats);

    void sendUploadFinished(IntentSource source, String networkType, long duration, AnalyticsStatistics stats, boolean ocid);

    void sendExportFinished(long duration, String fileType, AnalyticsStatistics stats);

    void sendExportFinishedTotal(long duration, int numberOfFiles, AnalyticsStatistics stats);

    void sendExportDeleteAction();

    void sendExportKeepAction();

    void sendExportUploadAction();

    void sendPrefsUpdateCheckEnabled(boolean enabled);

    void sendPrefsNotifyMeasurementsCollected(boolean enabled);

    void sendPrefsAppTheme(String theme);

    void sendPrefsCollectorApiVersion(int apiVersion);

    void sendHelpDialogOpened(String dialogName);
}
