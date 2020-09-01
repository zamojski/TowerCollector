/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.analytics;

import java.util.Map;

import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;

public class FakeAnalyticsReportingService implements IAnalyticsReportingService {
    @Override
    public void setTrackingEnabled(boolean trackingEnabled) {

    }

    @Override
    public void sendUpdateAction(String source) {

    }

    @Override
    public void sendMigrationFinished(long duration, int oldDbVersion, AnalyticsStatistics stats) {

    }

    @Override
    public void sendCollectorFinished(IntentSource source, String meansOfTransport, int apiVersion, long duration, AnalyticsStatistics stats, Map<NetworkGroup,Integer> collectedCellTypes) {

    }

    @Override
    public void sendUploadFinished(IntentSource source, String networkType, long duration, AnalyticsStatistics stats, boolean ocid) {

    }

    @Override
    public void sendExportFinished(long duration, String fileType, AnalyticsStatistics stats) {

    }

    @Override
    public void sendExportFinishedTotal(long duration, int numberOfFiles, AnalyticsStatistics stats) {

    }

    @Override
    public void sendExportDeleteAction() {

    }

    @Override
    public void sendExportKeepAction() {

    }

    @Override
    public void sendExportShareAction() {

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
    public void sendPrefsCollectorApiVersion(int apiVersion) {

    }

    @Override
    public void sendHelpDialogOpened(String dialogName) {

    }
}
