/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.collector.parsers;

import de.greenrobot.event.EventBus;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.collector.ParseResult;
import info.zamojski.soft.towercollector.collector.validators.ConditionsValidator;
import info.zamojski.soft.towercollector.collector.validators.LocationValidator;
import info.zamojski.soft.towercollector.collector.validators.SystemTimeValidator;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.events.MeasurementProcessedEvent;
import info.zamojski.soft.towercollector.model.Measurement;

import android.location.Location;
import android.location.LocationManager;
import trikita.log.Log;

public abstract class MeasurementParser implements Runnable {

    private static final String TAG = MeasurementParser.class.getSimpleName();

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
            lastSavedLocation.setTime(lastSavedMeasurement.getTimestamp());
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
        long systemTimestamp = measurement.getTimestamp();
        long gpsTimestamp = location.getTime();
        if (!systemTimeValidator.isValid(systemTimestamp, gpsTimestamp)) {
            Log.d(TAG, "fixMeasurementTimestamp(): Fixing measurement time = %s, gps time = %s", systemTimestamp, gpsTimestamp);
            measurement.setTimestamp(gpsTimestamp);
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
