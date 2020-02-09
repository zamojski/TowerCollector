/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import java.util.Date;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.model.Statistics;
import timber.log.Timber;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainStatsFragment extends MainFragmentBase {

    private TextView mainStatsLocalTitleTextView;
    private TextView mainStatsGlobalTitleTextView;

    private TextView mainStatsTodayLocationsValueTextView;
    private TextView mainStatsTodayCellsValueTextView;
    private TextView mainStatsLocalLocationsValueTextView;
    private TextView mainStatsLocalCellsValueTextView;
    private TextView mainStatsGlobalLocationsValueTextView;
    private TextView mainStatsGlobalCellsValueTextView;

    private TextView mainStatsToUploadOcidValueTextView;
    private TextView mainStatsToUploadMlsValueTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_stats_fragment, container, false);
        configureControls(rootView);
        return rootView;
    }

    @Override
    protected void configureOnResume() {
        super.configureOnResume();
        MyApplication app = MyApplication.getApplication();
        Statistics stats = MeasurementsDatabase.getInstance(app).getMeasurementsStatistics();
        printStatistics(stats);
    }

    @Override
    protected void configureControls(View view) {
        super.configureControls(view);
        mainStatsLocalTitleTextView = view.findViewById(R.id.main_stats_local_title_textview);
        mainStatsGlobalTitleTextView = view.findViewById(R.id.main_stats_global_title_textview);

        mainStatsTodayLocationsValueTextView = view.findViewById(R.id.main_stats_today_locations_value_textview);
        mainStatsTodayCellsValueTextView = view.findViewById(R.id.main_stats_today_cells_value_textview);
        mainStatsLocalLocationsValueTextView = view.findViewById(R.id.main_stats_local_locations_value_textview);
        mainStatsLocalCellsValueTextView = view.findViewById(R.id.main_stats_local_cells_value_textview);
        mainStatsGlobalLocationsValueTextView = view.findViewById(R.id.main_stats_global_locations_value_textview);
        mainStatsGlobalCellsValueTextView = view.findViewById(R.id.main_stats_global_cells_value_textview);

        mainStatsToUploadOcidValueTextView = view.findViewById(R.id.main_stats_to_upload_ocid_locations_value_textview);
        mainStatsToUploadMlsValueTextView = view.findViewById(R.id.main_stats_to_upload_mls_locations_value_textview);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeasurementSavedEvent event) {
        Statistics stats = event.getStatistics();
        printStatistics(stats);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PrintMainWindowEvent event) {
        Statistics stats = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getMeasurementsStatistics();
        printStatistics(stats);
    }

    private void printStatistics(Statistics stats) {
        Timber.d("printStatistics(): Showing stats %s", stats);
        long sinceLocal = stats.getSinceLocal();
        if (sinceLocal == 0)
            sinceLocal = System.currentTimeMillis();
        mainStatsLocalTitleTextView.setText(String.format(locale, getStringForLocale(R.string.main_stats_local_title), dateTimeFormatStandard.format(new Date(sinceLocal))));
        long sinceGlobal = stats.getSinceGlobal();
        if (sinceGlobal == 0)
            sinceGlobal = System.currentTimeMillis();
        mainStatsGlobalTitleTextView.setText(String.format(locale, getStringForLocale(R.string.main_stats_global_title), dateTimeFormatStandard.format(new Date(sinceGlobal))));
        mainStatsTodayLocationsValueTextView.setText(String.format(locale, "%d", stats.getLocationsToday()));
        mainStatsTodayCellsValueTextView.setText(String.format(locale, "%d (%d)", stats.getCellsToday(), stats.getDiscoveredCellsToday()));
        mainStatsLocalLocationsValueTextView.setText(String.format(locale, "%d", stats.getLocationsLocal()));
        mainStatsLocalCellsValueTextView.setText(String.format(locale, "%d (%d)", stats.getCellsLocal(), stats.getDiscoveredCellsLocal()));
        mainStatsGlobalLocationsValueTextView.setText(String.format(locale, "%d", stats.getLocationsGlobal()));
        mainStatsGlobalCellsValueTextView.setText(String.format(locale, "%d", stats.getDiscoveredCellsGlobal()));
        mainStatsToUploadOcidValueTextView.setText(String.format(locale, "%d", stats.getToUploadOcid()));
        mainStatsToUploadMlsValueTextView.setText(String.format(locale, "%d", stats.getToUploadMls()));
    }
}
