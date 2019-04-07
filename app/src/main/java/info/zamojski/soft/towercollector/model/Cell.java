/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import java.io.Serializable;

import info.zamojski.soft.towercollector.enums.NetworkGroup;

public class Cell implements Serializable {

    private static final long serialVersionUID = -1561237876324180202L;

    public static final int UNKNOWN_CID = Integer.MAX_VALUE; // safe for all network types
    public static final int UNKNOWN_SIGNAL = Integer.MAX_VALUE; // safe for all network types

    /**
     * Cell ID.
     */
    private int cellId;
    /**
     * Cell Signal ID.
     */
    private int cellSignalId;
    /**
     * Mobile Country Code.
     */
    private int mcc = UNKNOWN_CID;
    /**
     * Mobile Network Code.
     */
    private int mnc = UNKNOWN_CID;
    /**
     * Location Area Code.
     */
    private int lac = UNKNOWN_CID;
    /**
     * Cell Tower ID.
     */
    private int cid = UNKNOWN_CID;
    /**
     * Primary Scrambling Code.
     */
    private int psc = UNKNOWN_CID;
    /**
     * Network Type.
     */
    private NetworkGroup networkType = NetworkGroup.Unknown;
    /**
     * Is cell neighboring.
     */
    private boolean neighboring = false;
    /**
     * Timing Advance.
     */
    private int ta = UNKNOWN_SIGNAL;
    /**
     * Arbitrary Strength Unit Level.
     */
    private int asu = UNKNOWN_SIGNAL;
    /**
     * Signal Strength in dBm.
     */
    private int dbm = UNKNOWN_SIGNAL;

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getCellSignalId() {
        return cellSignalId;
    }

    public void setCellSignalId(int cellSignalId) {
        this.cellSignalId = cellSignalId;
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

    public int getPsc() {
        return psc;
    }

    public void setPsc(int psc) {
        this.psc = psc;
    }

    public NetworkGroup getNetworkType() {
        return networkType;
    }

    public void setNetworkType(NetworkGroup networkType) {
        this.networkType = networkType;
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

    public int getLongCid() {
        if (cid <= 65536)
            return UNKNOWN_CID;
        return cid;
    }

    public int getShortCid() {
        if (cid <= 65536)
            return UNKNOWN_CID;
        if (networkType == NetworkGroup.Wcdma)
            return cid % 65536;
        else // LTE (reversed order)
            return cid / 256;
    }

    public int getRnc() {
        if (cid <= 65536)
            return UNKNOWN_CID;
        if (networkType == NetworkGroup.Wcdma)
            return cid / 65536;
        else // LTE (reversed order)
            return cid % 256;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
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
        return "Cell{" +
                "cellId=" + cellId +
                ", cellSignalId=" + cellSignalId +
                ", mcc=" + mcc +
                ", mnc=" + mnc +
                ", lac=" + lac +
                ", cid=" + cid +
                ", psc=" + psc +
                ", networkType=" + networkType +
                ", neighboring=" + neighboring +
                ", ta=" + ta +
                ", asu=" + asu +
                ", dbm=" + dbm +
                '}';
    }
}
