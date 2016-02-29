/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import info.zamojski.soft.towercollector.enums.NetworkGroup;

import java.io.Serializable;

public class Measurement implements Serializable {
    private static final long serialVersionUID = -1561707154666574202L;
    public static final float GPS_VALUE_NOT_AVAILABLE = 0.0f;
    public static final int UNKNOWN_CID = Integer.MAX_VALUE; // safe for all network types
    public static final int UNKNOWN_SIGNAL = Integer.MAX_VALUE; // safe for all network types
    /**
     * Measurement Row ID.
     */
    private int rowId;
    /**
     * Mobile Country Code.
     */
    private int mcc;
    /**
     * Mobile Network Code.
     */
    private int mnc;
    /**
     * Location Area Code.
     */
    private int lac;
    /**
     * Cell Tower ID.
     */
    private int cid;
    /**
     * Primary Scrambling Code.
     */
    private int psc;
    /**
     * Is cell neighboring.
     */
    private boolean neighboring;
    /**
     * Timing Advance.
     */
    private int ta;
    /**
     * Network Type as defined in TelephonyManager.
     */
    private NetworkGroup networkType;
    /**
     * Arbitrary Strength Unit Level.
     */
    private int asu;
    /**
     * Signal Strength in dBm.
     */
    private int dbm;
    /**
     * Geographic Latitude.
     */
    private double latitude;
    /**
     * Geographic Longitude.
     */
    private double longitude;
    /**
     * GPS Accuracy in m.
     * 0 - not available.
     */
    private float gpsAccuracy;
    /**
     * GPS Speed in m/s.
     * 0 - not available.
     */
    private float gpsSpeed;
    /**
     * GPS Bearing in degrees within range of (0-360].
     * 0 - not available.
     */
    private float gpsBearing;
    /**
     * GPS Altitude in m.
     * 0 - not available.
     */
    private double gpsAltitude;
    /**
     * Unix Timestamp with milliseconds.
     */
    private long timestamp;

    /**
     * Default constructor.
     */
    public Measurement() {
        mcc = mnc = cid = lac = psc = UNKNOWN_CID;
        neighboring = false;
        ta = asu = dbm = UNKNOWN_SIGNAL;
        gpsAccuracy = GPS_VALUE_NOT_AVAILABLE;
        gpsSpeed = GPS_VALUE_NOT_AVAILABLE;
        gpsBearing = GPS_VALUE_NOT_AVAILABLE;
        gpsAltitude = GPS_VALUE_NOT_AVAILABLE;
        networkType = NetworkGroup.Unknown;
        timestamp = System.currentTimeMillis();// with milliseconds
    }

    /**
     * Copy constructor (doesn't clone rowId!).
     */
    public Measurement(Measurement m) {
        this.mcc = m.mcc;
        this.mnc = m.mnc;
        this.lac = m.lac;
        this.cid = m.cid;
        this.psc = m.psc;
        this.networkType = m.networkType;
        this.neighboring = m.neighboring;

        this.asu = m.asu;
        this.dbm = m.dbm;
        this.ta = m.ta;

        this.latitude = m.latitude;
        this.longitude = m.longitude;

        this.gpsAccuracy = m.gpsAccuracy;
        this.gpsAltitude = m.gpsAltitude;
        this.gpsBearing = m.gpsBearing;
        this.gpsSpeed = m.gpsSpeed;

        this.timestamp = m.timestamp;
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public int getMcc() {
        return mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public int getMnc() {
        return mnc;
    }

    public void setMnc(int mnc) {
        this.mnc = mnc;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getPsc() {
        return psc;
    }

    public void setPsc(int psc) {
        this.psc = psc;
    }

    public boolean isNeighboring() {
        return neighboring;
    }

    public void setNeighboring(boolean neighboring) {
        this.neighboring = neighboring;
    }

    public int getTa() {
        return ta;
    }

    public void setTa(int ta) {
        this.ta = ta;
    }

    public NetworkGroup getNetworkType() {
        return networkType;
    }

    public void setNetworkType(NetworkGroup networkType) {
        this.networkType = networkType;
    }

    public int getAsu() {
        return asu;
    }

    public void setAsu(int asu) {
        this.asu = asu;
    }

    public int getDbm() {
        return dbm;
    }

    public void setDbm(int dBm) {
        this.dbm = dBm;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getGpsAccuracy() {
        return gpsAccuracy;
    }

    public void setGpsAccuracy(float gpsAccuracy) {
        this.gpsAccuracy = gpsAccuracy;
    }

    public float getGpsSpeed() {
        return gpsSpeed;
    }

    public void setGpsSpeed(float gpsSpeed) {
        this.gpsSpeed = gpsSpeed;
    }

    public float getGpsBearing() {
        return gpsBearing;
    }

    public void setGpsBearing(float gpsBearing) {
        this.gpsBearing = gpsBearing;
    }

    public double getGpsAltitude() {
        return gpsAltitude;
    }

    public void setGpsAltitude(double gpsAltitude) {
        this.gpsAltitude = gpsAltitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setGsmCellInfo(int mcc, int mnc, int lac, int cid) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cid = cid;
        this.psc = UNKNOWN_CID;
        this.networkType = NetworkGroup.Gsm;
    }

    public void setGsmSignalInfo(int asu, int signalStrength) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.ta = UNKNOWN_SIGNAL;
    }

    public void setWcdmaCellInfo(int mcc, int mnc, int lac, int cid, int psc) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cid = cid;
        this.psc = psc;
        this.networkType = NetworkGroup.Wcdma;
    }

    public void setWcdmaSignalInfo(int asu, int signalStrength) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.ta = UNKNOWN_SIGNAL;
    }

    public void setCdmaCellInfo(int systemId, int networkId, int baseStationId) {
        this.mcc = UNKNOWN_CID;
        this.mnc = systemId;
        this.lac = networkId;
        this.cid = baseStationId;
        this.psc = UNKNOWN_CID;
        this.networkType = NetworkGroup.Cdma;
    }

    public void setCdmaSignalInfo(int asu, int signalStrength) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.ta = UNKNOWN_SIGNAL;
    }

    public void setLteCellInfo(int mcc, int mnc, int tac, int ci, int pci) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = tac;
        this.cid = ci;
        this.psc = pci;
        this.networkType = NetworkGroup.Lte;
    }

    public void setLteSignalInfo(int asu, int signalStrength, int timingAdvance) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.ta = timingAdvance;
    }

    public void setGsmCellLocation(int mcc, int mnc, int lac, int cid, NetworkGroup networkType) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cid = cid;
        this.psc = UNKNOWN_CID;
        this.networkType = networkType;
    }

    public void setGsmCellLocation(int mcc, int mnc, int lac, int cid, int psc, NetworkGroup networkType) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cid = cid;
        this.psc = psc;
        this.networkType = networkType;
    }

    public void setGsmLocationSignal(int asu, int signalStrength) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.ta = UNKNOWN_SIGNAL;
    }

    public void setCdmaCellLocation(int systemId, int networkId, int baseStationId) {
        this.mcc = UNKNOWN_CID;
        this.mnc = systemId;
        this.lac = networkId;
        this.cid = baseStationId;
        this.psc = UNKNOWN_CID;
        this.networkType = NetworkGroup.Cdma;
    }

    public void setCdmaLocationSignal(int asu, int signalStrength) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.ta = UNKNOWN_SIGNAL;
    }

    @Override
    public String toString() {
        return "Measurement [rowId=" + rowId + ", mcc=" + mcc + ", mnc=" + mnc + ", lac=" + lac + ", cid=" + cid + ", psc=" + psc + ", neighboring=" + neighboring + ", ta=" + ta + ", networkType=" + networkType + ", asu=" + asu + ", dbm=" + dbm + ", latitude=" + latitude + ", longitude=" + longitude + ", gpsAccuracy=" + gpsAccuracy + ", gpsSpeed=" + gpsSpeed + ", gpsBearing=" + gpsBearing + ", gpsAltitude=" + gpsAltitude + ", timestamp=" + timestamp + "]";
    }
}
