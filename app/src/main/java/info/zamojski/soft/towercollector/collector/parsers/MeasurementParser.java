/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.parsers;

import org.greenrobot.eventbus.EventBus;

import info.zamojski.soft.towercollector.BuildConfig;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.collector.ParseResult;
import info.zamojski.soft.towercollector.collector.validators.ConditionsValidator;
import info.zamojski.soft.towercollector.collector.validators.LocationValidator;
import info.zamojski.soft.towercollector.collector.validators.SystemTimeValidator;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.events.MeasurementProcessedEvent;
import info.zamojski.soft.towercollector.model.Measurement;
import timber.log.Timber;

import android.location.Location;
import android.location.LocationManager;

public abstract class MeasurementParser implements Runnable {

    protected float MAX_REASONABLE_SPEED = 500.0f; // in m/s

    protected LocationValidator locationValidator;

    protected ConditionsValidator conditionsValidator;

    protected SystemTimeValidator systemTimeValidator;

    protected Measurement lastSavedMeasurement;
    protected Location lastSavedLocation;

    protected boolean collectNeighboringCells;

    protected MeasurementParser(LocationValidator locationValidator, ConditionsValidator conditionsValidator,
                                SystemTimeValidator systemTimeValidator, boolean collectNeighboringCells) {
        this.locationValidator = locationValidator;
        this.conditionsValidator = conditionsValidator;
        this.systemTimeValidator = systemTimeValidator;
        this.collectNeighboringCells = collectNeighboringCells;
    }

    protected void getAndSetLastLocation() {
        lastSavedMeasurement = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getLastMeasurement();
        if (lastSavedMeasurement != null) {
            // simulate location if there is starting point (some values may not be used, but don't know which exactly)
            lastSavedLocation = new Location(LocationManager.PASSIVE_PROVIDER);
            lastSavedLocation.setLatitude(lastSavedMeasurement.getLatitude());
            lastSavedLocation.setLongitude(lastSavedMeasurement.getLongitude());
            lastSavedLocation.setAccuracy(lastSavedMeasurement.getGpsAccuracy());
            lastSavedLocation.setSpeed(lastSavedMeasurement.getGpsSpeed());
            lastSavedLocation.setBearing(lastSavedMeasurement.getGpsBearing());
            lastSavedLocation.setAltitude(lastSavedMeasurement.getGpsAltitude());
            lastSavedLocation.setTime(lastSavedMeasurement.getMeasuredAt());
        }
    }

    protected void updateMeasurementWithLocation(Measurement measurement, Location location) {
        measurement.setLatitude(location.getLatitude());
        measurement.setLongitude(location.getLongitude());
        measurement.setGpsAccuracy(location.getAccuracy());
        float speed = location.getSpeed();
        if (speed > MAX_REASONABLE_SPEED)
            speed = 0;
        measurement.setGpsSpeed(speed);
        measurement.setGpsBearing(location.getBearing());
        measurement.setGpsAltitude(location.getAltitude());
    }

    protected void fixMeasurementTimestamp(Measurement measurement, Location location) {
        // update timestamp if user has incorrect system time in phone
        // that means if earlier than fix or later by one day
        // but only if gps time later than app build time
        long systemTimestamp = measurement.getMeasuredAt();
        long gpsTimestamp = location.getTime();
        if (!systemTimeValidator.isValid(systemTimestamp, gpsTimestamp)) {
            long appBuildTimestamp = BuildConfig.BUILD_DATE_TIME;
            Timber.i("fixMeasurementTimestamp(): Fixing measurement time = %s, gps time = %s, app time = %s", systemTimestamp, gpsTimestamp, appBuildTimestamp);
            // starting on November 3, 2019, mobile devices manufactured between 2006 and 2016 may have their GPS accuracy impacted due to GPS Rollover issue
            if (gpsTimestamp >= appBuildTimestamp) {
                measurement.setMeasuredAt(gpsTimestamp);
                Timber.i("fixMeasurementTimestamp(): Fixed measurement time using gps time");
            } else if (systemTimestamp < appBuildTimestamp) {
                throw new IllegalStateException("System (" + systemTimestamp + ") and GPS (" + gpsTimestamp + ") timestamps are older than app build time (" + appBuildTimestamp + ")");
            }
        }
    }

    protected void notifyResult(ParseResult result) {
        EventBus.getDefault().post(new MeasurementProcessedEvent(result));
    }

    public void start() {
        EventBus.getDefault().register(this);
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void run() {
        start();
    }
}
