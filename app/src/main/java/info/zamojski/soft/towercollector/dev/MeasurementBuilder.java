/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dev;

import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.Measurement;

public class MeasurementBuilder {

    private Measurement m;
    private Cell c;

    public MeasurementBuilder() {
        m = new Measurement();
        c = new Cell();
        m.addCell(c);
        setTime(System.currentTimeMillis());
    }

    public Measurement build() {
        return m;
    }

    public MeasurementBuilder setGsmCell(int mcc, int mnc, int lac, int cid) {
        c.setGsmCellInfo(mcc, mnc, lac, cid);
        return this;
    }

    public MeasurementBuilder setWcdmaCell(int mcc, int mnc, int lac, int cid, int psc) {
        c.setWcdmaCellInfo(mcc, mnc, lac, cid, psc);
        return this;
    }

    public MeasurementBuilder setLteCell(int mcc, int mnc, int tac, int ci, int pci) {
        c.setLteCellInfo(mcc, mnc, tac, ci, pci);
        return this;
    }

    public MeasurementBuilder setCdmaCell(int sid, int nid, int bid) {
        c.setCdmaCellInfo(sid, nid, bid);
        return this;
    }

    public MeasurementBuilder setNrCell(int mcc, int mnc, int tac, long nci, int pci) {
        c.setNrCellInfo(String.valueOf(mcc), String.valueOf(mnc), tac, nci, pci);
        return this;
    }

    public MeasurementBuilder setTdscdmaCell(int mcc, int mnc, int lac, int cid, int cpid) {
        c.setTdscdmaCellInfo(String.valueOf(mcc), String.valueOf(mnc), lac, cid, cpid);
        return this;
    }

    public MeasurementBuilder setGsmSignal(int asu, int dbm, int ta) {
        c.setGsmSignalInfo(asu, dbm, ta);
        return this;
    }

    public MeasurementBuilder setWcdmaSignal(int asu, int dbm) {
        c.setWcdmaSignalInfo(asu, dbm);
        return this;
    }

    public MeasurementBuilder setLteSignal(int asu, int dbm, int ta, int rsrp, int rsrq, int rssi, int rssnr, int cqi) {
        c.setLteSignalInfo(asu, dbm, ta, rsrp, rsrq, rssi, rssnr, cqi);
        return this;
    }

    public MeasurementBuilder setCdmaSignal(int asu, int dbm, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr) {
        c.setCdmaSignalInfo(asu, dbm, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr);
        return this;
    }

    public MeasurementBuilder setNrSignal(int asu, int dbm, int csiRsrp, int csiRsrq, int csiSinr, int ssRsrp, int ssRsrq, int ssSinr) {
        c.setNrSignalInfo(asu, dbm, csiRsrp, csiRsrq, csiSinr, ssRsrp, ssRsrq, ssSinr);
        return this;
    }

    public MeasurementBuilder setTdscdmaSignal(int asu, int dbm, int rscp) {
        c.setTdscdmaSignalInfo(asu, dbm, rscp);
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
        m.setMeasuredAt(timestamp);
        return this;
    }

}
