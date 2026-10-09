/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.acra;

import android.content.Context;

import androidx.annotation.NonNull;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;
import org.acra.collector.BaseReportFieldCollector;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;

import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import timber.log.Timber;

public class DatabaseVersionCollector extends BaseReportFieldCollector {

    public DatabaseVersionCollector() {
        super(ReportField.CUSTOM_DATA);
    }

    @NonNull
    @Override
    public Order getOrder() {
        return Order.EARLY;
    }

    @Override
    public void collect(@NonNull ReportField reportField, @NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder, @NonNull CrashReportData target) {
        try {
            int dbVersion = MeasurementsDatabase.getDatabaseVersion(context);
            reportBuilder.getCustomData().put("DB_VERSION", String.valueOf(dbVersion));
        } catch (Throwable t) {
            Timber.e(t, "collect(): Failed to collect DB_VERSION for ACRA report");
            reportBuilder.getCustomData().put("DB_VERSION", "ERROR: " + t.getMessage());
        }
    }
}
