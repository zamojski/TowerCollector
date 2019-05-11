/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.analytics;

import info.zamojski.soft.towercollector.model.AnalyticsStatistics;

import android.app.Activity;

public interface IAnalyticsReportingService {

    void setAppOptOut(boolean optOut);

    void sendUpdateAction(String source);

    void sendMigrationStarted();

    void sendMigrationFinished(long duration, int oldDbVersion, AnalyticsStatistics stats);

    void sendCollectorStarted(IntentSource source);

    void sendCollectorFinished(long duration, String transportMode, AnalyticsStatistics stats);

    void sendCollectorApiVersionUsed(String apiVersion);

    void sendUploadStarted(IntentSource source, boolean ocid);

    void sendUploadFinished(long duration, String networkType, AnalyticsStatistics stats, boolean ocid);

    void sendExportStarted();

    void sendExportFinished(long duration, String fileType, AnalyticsStatistics stats);

    void sendExportDeleteAction();

    void sendExportKeepAction();

    void sendExportUploadAction();

    void sendPrefsUpdateCheckEnabled(boolean enabled);

    void sendPrefsNotifyMeasurementsCollected(boolean enabled);

    void sendPrefsAppTheme(String theme);

    void sendPrefsCollectorApiVersion(String apiVersion);

    void sendHelpDialogOpened(String dialogName);
}
