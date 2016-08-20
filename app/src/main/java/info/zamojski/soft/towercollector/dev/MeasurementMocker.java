/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dev;

import org.greenrobot.eventbus.EventBus;

import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.model.CellsCount;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.model.Statistics;
import info.zamojski.soft.towercollector.utils.DateUtils;
import trikita.log.Log;

public class MeasurementMocker {

    private static final String TAG = MeasurementMocker.class.getSimpleName();

    public static void mockMainActivityData() {
        Log.d("mockMainActivityData(): mocking data");
        Measurement m = mockLastMeasurement();
        CellsCount cellsCount = mockCellsCount();
        Statistics stats = mockStatistics();
        MeasurementSavedEvent event = new MeasurementSavedEvent(m, cellsCount, stats);
        EventBus.getDefault().postSticky(event);
    }

    private static Measurement mockLastMeasurement() {
        Measurement m = new Measurement();

        m.setTimestamp(System.currentTimeMillis());

        m.setLatitude(52.0693267);
        m.setLongitude(19.4781224);

        m.setGpsAccuracy(12.0f);
        m.setGpsSpeed(1.23f);
        m.setGpsBearing(36.2f);
        m.setGpsAltitude(101.0);

        m.setLteCellInfo(260, 6, 5114, 1055842, 28);
        m.setLteSignalInfo(46, -94, 76);

        return m;
    }

    private static CellsCount mockCellsCount() {
        return new CellsCount(1, 3);
    }

    private static Statistics mockStatistics() {
        Statistics stats = new Statistics();

        stats.setSinceGlobal(DateUtils.getTimeFrom(2015, 6, 7, 11, 19, 10).getTime());
        stats.setLocationsGlobal(304798);
        stats.setDiscoveredCellsGlobal(5827);

        stats.setSinceLocal(DateUtils.addHours(DateUtils.addDays(DateUtils.getCurrentDate(), -6), 13).getTime());
        stats.setLocationsLocal(1922);
        stats.setCellsLocal(74);
        stats.setDiscoveredCellsLocal(18);

        stats.setLocationsToday(147);
        stats.setCellsToday(11);
        stats.setDiscoveredCellsToday(0);

        return stats;
    }
}
