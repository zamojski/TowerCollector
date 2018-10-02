/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.analytics;

import info.zamojski.soft.towercollector.BuildConfig;
import info.zamojski.soft.towercollector.MyApplication;
import timber.log.Timber;

public class AnalyticsServiceFactory {
    public IAnalyticsReportingService createInstance() {
        Timber.d("createInstance(): Creating Google Analytics");
        boolean trackingEnabled = MyApplication.getPreferencesProvider().getTrackingEnabled();
        boolean dryRun = BuildConfig.DEBUG;
        return new GoogleAnalyticsReportingService(MyApplication.getApplication(), trackingEnabled, dryRun);
    }
}
