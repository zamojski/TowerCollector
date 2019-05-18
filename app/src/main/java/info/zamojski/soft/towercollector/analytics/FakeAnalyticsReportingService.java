/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.analytics;

import info.zamojski.soft.towercollector.model.AnalyticsStatistics;

public class FakeAnalyticsReportingService implements IAnalyticsReportingService {
    @Override
    public void setAppOptOut(boolean optOut) {

    }

    @Override
    public void sendUpdateAction(String source) {

    }

    @Override
    public void sendMigrationStarted() {

    }

    @Override
    public void sendMigrationFinished(long duration, int oldDbVersion, AnalyticsStatistics stats) {

    }

    @Override
    public void sendCollectorStarted(IntentSource source) {

    }

    @Override
    public void sendCollectorFinished(long duration, String transportMode, AnalyticsStatistics stats) {

    }

    @Override
    public void sendCollectorApiVersionUsed(String apiVersion) {

    }

    @Override
    public void sendUploadStarted(IntentSource source, boolean ocid) {

    }

    @Override
    public void sendUploadFinished(long duration, String networkType, AnalyticsStatistics stats, boolean ocid) {

    }

    @Override
    public void sendExportStarted() {

    }

    @Override
    public void sendExportFinished(long duration, String fileType, AnalyticsStatistics stats) {

    }

    @Override
    public void sendExportDeleteAction() {

    }

    @Override
    public void sendExportKeepAction() {

    }

    @Override
    public void sendExportUploadAction() {

    }

    @Override
    public void sendPrefsUpdateCheckEnabled(boolean enabled) {

    }

    @Override
    public void sendPrefsNotifyMeasurementsCollected(boolean enabled) {

    }

    @Override
    public void sendPrefsAppTheme(String theme) {

    }

    @Override
    public void sendPrefsCollectorApiVersion(String apiVersion) {

    }

    @Override
    public void sendHelpDialogOpened(String dialogName) {

    }
}
