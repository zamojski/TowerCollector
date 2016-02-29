/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dev;

import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.Measurement;

public class MeasurementBuilder {

    private Measurement m;

    public MeasurementBuilder() {
        m = new Measurement();
    }

    public Measurement build() {
        return new Measurement(m);
    }

    public MeasurementBuilder setGsmCell(int mcc, int mnc, int lac, int cid) {
        m.setNetworkType(NetworkGroup.Gsm);
        m.setGsmCellInfo(mcc, mnc, lac, cid);
        return this;
    }

    public MeasurementBuilder setWcdmaCell(int mcc, int mnc, int lac, int cid, int psc) {
        m.setNetworkType(NetworkGroup.Wcdma);
        m.setWcdmaCellInfo(mcc, mnc, lac, cid, psc);
        return this;
    }

    public MeasurementBuilder setLteCell(int mcc, int mnc, int tac, int ci, int pci) {
        m.setNetworkType(NetworkGroup.Lte);
        m.setLteCellInfo(mcc, mnc, tac, ci, pci);
        return this;
    }

    public MeasurementBuilder setCdmaCell(int sid, int nid, int bid) {
        m.setNetworkType(NetworkGroup.Cdma);
        m.setCdmaCellInfo(sid, nid, bid);
        return this;
    }

    public MeasurementBuilder setGsmSignal(int asu, int dbm) {
        m.setGsmSignalInfo(asu, dbm);
        return this;
    }

    public MeasurementBuilder setWcdmaSignal(int asu, int dbm) {
        m.setWcdmaSignalInfo(asu, dbm);
        return this;
    }

    public MeasurementBuilder setLteSignal(int asu, int dbm, int ta) {
        m.setLteSignalInfo(asu, dbm, ta);
        return this;
    }

    public MeasurementBuilder setCdmaSignal(int asu, int dbm) {
        m.setCdmaSignalInfo(asu, dbm);
        return this;
    }

    public MeasurementBuilder setLocation(double lat, double lon, double altitude, float accuracy) {
        m.setLatitude(lat);
        m.setLongitude(lon);
        m.setGpsAltitude(altitude);
        m.setGpsAccuracy(accuracy);
        return this;
    }

    public MeasurementBuilder setMovement(float bearing, float speed) {
        m.setGpsBearing(bearing);
        m.setGpsSpeed(speed);
        return this;
    }

    public MeasurementBuilder setTime(long timestamp) {
        m.setTimestamp(timestamp);
        return this;
    }

}
