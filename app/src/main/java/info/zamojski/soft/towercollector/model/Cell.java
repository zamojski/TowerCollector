/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.utils.StringUtils;

public class Cell implements Serializable {

    private static final long serialVersionUID = -1561237876324180202L;

    public static final int UNKNOWN_CID = Integer.MAX_VALUE; // safe for all network types except 5G (NR)
    public static final long UNKNOWN_CID_LONG = Long.MAX_VALUE; // safe for 5G (NR)
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
    private long cid = UNKNOWN_CID_LONG;
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

    public long getLongCid() {
        if (cid <= 65536)
            return UNKNOWN_CID;
        return cid;
    }

    public long getShortCid() {
        if (cid <= 65536)
            return UNKNOWN_CID;
        if (networkType == NetworkGroup.Wcdma)
            return cid % 65536;
        else if (networkType == NetworkGroup.Nr) // TODO: 5G rule unknown
            return 0;
        else // LTE (reversed order)
            return cid / 256;
    }

    public long getRnc() {
        if (cid <= 65536)
            return UNKNOWN_CID;
        if (networkType == NetworkGroup.Wcdma)
            return cid / 65536;
        else if (networkType == NetworkGroup.Nr) // TODO: 5G rule unknown
            return 0;
        else // LTE (reversed order)
            return cid % 256;
    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
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

    public void setNrCellInfo(String mccString, String mncString, int tac, long nci, int pci) {
        this.mcc = StringUtils.toInteger(mccString, Cell.UNKNOWN_CID);
        this.mnc = StringUtils.toInteger(mncString, Cell.UNKNOWN_CID);
        this.lac = tac;
        this.cid = nci;
        this.psc = pci;
        this.networkType = NetworkGroup.Nr;
    }

    public void setNrSignalInfo(int asu, int signalStrength) {
        this.asu = asu;
        this.dbm = signalStrength;
    }

    public void setTdscdmaCellInfo(String mccString, String mncString, int lac, int cid, int cpid) {
        this.mcc = StringUtils.toInteger(mccString, Cell.UNKNOWN_CID);
        this.mnc = StringUtils.toInteger(mncString, Cell.UNKNOWN_CID);
        this.lac = lac;
        this.cid = cid;
        this.psc = cpid;
        this.networkType = NetworkGroup.Tdscdma;
    }

    public void setTdscdmaSignalInfo(int asu, int signalStrength) {
        this.asu = asu;
        this.dbm = signalStrength;
    }

    public void setGsmCellLocation(int mcc, int mnc, int lac, int cid, NetworkGroup networkType) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cid = cid;
        this.psc = UNKNOWN_CID;
        this.networkType = networkType;
    }

    public void setGsmCellLocation(int mcc, int mnc, int lac, long cid, int psc, NetworkGroup networkType) {
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

    @NotNull
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
