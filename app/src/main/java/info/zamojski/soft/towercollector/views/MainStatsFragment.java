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

import android.os.Bundle;

import trikita.log.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainStatsFragment extends MainFragmentBase {

    private static final String TAG = MainStatsFragment.class.getSimpleName();

    private TextView mainStatsLocalTitleTextView;
    private TextView mainStatsGlobalTitleTextView;

    private TextView mainStatsTodayLocationsValueTextView;
    private TextView mainStatsTodayCellsValueTextView;
    private TextView mainStatsLocalLocationsValueTextView;
    private TextView mainStatsLocalCellsValueTextView;
    private TextView mainStatsGlobalLocationsValueTextView;
    private TextView mainStatsGlobalCellsValueTextView;

    private String mainStatsLocalTitlePattern;
    private String mainStatsGlobalTitlePattern;

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
        mainStatsLocalTitleTextView = (TextView) view.findViewById(R.id.main_stats_local_title_textview);
        mainStatsGlobalTitleTextView = (TextView) view.findViewById(R.id.main_stats_global_title_textview);

        mainStatsTodayLocationsValueTextView = (TextView) view.findViewById(R.id.main_stats_today_locations_value_textview);
        mainStatsTodayCellsValueTextView = (TextView) view.findViewById(R.id.main_stats_today_cells_value_textview);
        mainStatsLocalLocationsValueTextView = (TextView) view.findViewById(R.id.main_stats_local_locations_value_textview);
        mainStatsLocalCellsValueTextView = (TextView) view.findViewById(R.id.main_stats_local_cells_value_textview);
        mainStatsGlobalLocationsValueTextView = (TextView) view.findViewById(R.id.main_stats_global_locations_value_textview);
        mainStatsGlobalCellsValueTextView = (TextView) view.findViewById(R.id.main_stats_global_cells_value_textview);

        mainStatsLocalTitlePattern = getString(R.string.main_stats_local_title);
        mainStatsGlobalTitlePattern = getString(R.string.main_stats_global_title);
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
        Log.d("printStatistics(): Showing stats %s", stats);
        long sinceLocal = stats.getSinceLocal();
        if (sinceLocal == 0)
            sinceLocal = System.currentTimeMillis();
        mainStatsLocalTitleTextView.setText(String.format(mainStatsLocalTitlePattern, dateTimeFormatStandard.format(new Date(sinceLocal))));
        long sinceGlobal = stats.getSinceGlobal();
        if (sinceGlobal == 0)
            sinceGlobal = System.currentTimeMillis();
        mainStatsGlobalTitleTextView.setText(String.format(mainStatsGlobalTitlePattern, dateTimeFormatStandard.format(new Date(sinceGlobal))));

        mainStatsTodayLocationsValueTextView.setText(String.valueOf(stats.getLocationsToday()));
        mainStatsTodayCellsValueTextView.setText(String.format("%d (%d)", stats.getCellsToday(), stats.getDiscoveredCellsToday()));
        mainStatsLocalLocationsValueTextView.setText(String.valueOf(stats.getLocationsLocal()));
        mainStatsLocalCellsValueTextView.setText(String.format("%d (%d)", stats.getCellsLocal(), stats.getDiscoveredCellsLocal()));
        mainStatsGlobalLocationsValueTextView.setText(String.valueOf(stats.getLocationsGlobal()));
        mainStatsGlobalCellsValueTextView.setText(String.valueOf(stats.getDiscoveredCellsGlobal()));
    }
}
