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

    public static final int UNKNOWN_CID = Integer.MAX_VALUE; // safe for all network types except 5G (NR), equals CellInfo.UNAVAILABLE which requires newer SDK
    public static final long UNKNOWN_CID_LONG = Long.MAX_VALUE; // safe for 5G (NR), equals CellInfo.UNAVAILABLE_LONG which requires newer SDK
    public static final int UNKNOWN_SIGNAL = Integer.MAX_VALUE; // safe for all network types, equals CellInfo.UNAVAILABLE which requires newer SDK

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
    /**
     * Reference Signal Received Power in dBm.
     */
    private int rsrp = UNKNOWN_SIGNAL;
    /**
     * Reference Signal Received Quality in dB.
     */
    private int rsrq = UNKNOWN_SIGNAL;
    /**
     * Received Signal Strength Indication in dBm.
     */
    private int rssi = UNKNOWN_SIGNAL;
    /**
     * Reference Signal Signal-to-Noise Ratio in dB.
     */
    private int rssnr = UNKNOWN_SIGNAL;
    /**
     * Channel Quality Indicator.
     */
    private int cqi = UNKNOWN_SIGNAL;
    /**
     * Received Signal Code Power in dBm.
     */
    private int rscp = UNKNOWN_SIGNAL;
    /**
     * Channel State Information (CSI) Reference Signal Received Power in dBm.
     */
    private int csiRsrp = UNKNOWN_SIGNAL;
    /**
     * Channel State Information (CSI) Reference Signal Received Quality in dB.
     */
    private int csiRsrq = UNKNOWN_SIGNAL;
    /**
     * Channel State Information (CSI) Signal-to-Noise and Interference Ratio in dB.
     */
    private int csiSinr = UNKNOWN_SIGNAL;
    /**
     * Synchronization Signal (SS) Reference Signal Received Power in dBm.
     */
    private int ssRsrp = UNKNOWN_SIGNAL;
    /**
     * Synchronization Signal (SS) Reference Signal Received Quality in dB.
     */
    private int ssRsrq = UNKNOWN_SIGNAL;
    /**
     * Synchronization Signal (SS) Signal-to-Noise and Interference Ratio in dB.
     */
    private int ssSinr = UNKNOWN_SIGNAL;
    /**
     * CDMA RSSI value in dBm.
     */
    private int cdmaDbm = UNKNOWN_SIGNAL;
    /**
     * CDMA Ec/Io value in dB*10.
     */
    private int cdmaEcio = UNKNOWN_SIGNAL;
    /**
     * EVDO RSSI value in dBm.
     */
    private int evdoDbm = UNKNOWN_SIGNAL;
    /**
     * EVDO Ec/Io value in dB*10.
     */
    private int evdoEcio = UNKNOWN_SIGNAL;
    /**
     * Signal to noise ratio.
     */
    private int evdoSnr = UNKNOWN_SIGNAL;
    /**
     * Ec/No (Energy per chip over the noise spectral density) as dB
     */
    private int ecNo = UNKNOWN_SIGNAL;
    /**
     * Absolute Radio Frequency Channel Number.
     */
    private int arfcn = UNKNOWN_CID;


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
            return UNKNOWN_CID_LONG;
        return cid;
    }

    public long getShortCid() {
        if (cid <= 65536)
            return UNKNOWN_CID_LONG;
        if (networkType == NetworkGroup.Wcdma)
            return cid % 65536;
        else if(networkType== NetworkGroup.Lte) // LTE (reversed order)
            return cid / 256;
        return UNKNOWN_CID_LONG;
    }

    public long getRnc() {
        if (cid <= 65536)
            return UNKNOWN_CID_LONG;
        if (networkType == NetworkGroup.Wcdma)
            return cid / 65536;
        else if(networkType== NetworkGroup.Lte) // LTE (reversed order)
            return cid % 256;
        return UNKNOWN_CID_LONG;
    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public int getRsrp() {
        return rsrp;
    }

    public void setRsrp(int rsrp) {
        this.rsrp = rsrp;
    }

    public int getRsrq() {
        return rsrq;
    }

    public void setRsrq(int rsrq) {
        this.rsrq = rsrq;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getRssnr() {
        return rssnr;
    }

    public void setRssnr(int rssnr) {
        this.rssnr = rssnr;
    }

    public int getCqi() {
        return cqi;
    }

    public void setCqi(int cqi) {
        this.cqi = cqi;
    }

    public int getRscp() {
        return rscp;
    }

    public void setRscp(int rscp) {
        this.rscp = rscp;
    }

    public int getCsiRsrp() {
        return csiRsrp;
    }

    public void setCsiRsrp(int csiRsrp) {
        this.csiRsrp = csiRsrp;
    }

    public int getCsiRsrq() {
        return csiRsrq;
    }

    public void setCsiRsrq(int csiRsrq) {
        this.csiRsrq = csiRsrq;
    }

    public int getCsiSinr() {
        return csiSinr;
    }

    public void setCsiSinr(int csiSinr) {
        this.csiSinr = csiSinr;
    }

    public int getSsRsrp() {
        return ssRsrp;
    }

    public void setSsRsrp(int ssRsrp) {
        this.ssRsrp = ssRsrp;
    }

    public int getSsRsrq() {
        return ssRsrq;
    }

    public void setSsRsrq(int ssRsrq) {
        this.ssRsrq = ssRsrq;
    }

    public int getSsSinr() {
        return ssSinr;
    }

    public void setSsSinr(int ssSinr) {
        this.ssSinr = ssSinr;
    }

    public int getCdmaDbm() {
        return cdmaDbm;
    }

    public void setCdmaDbm(int cdmaDbm) {
        this.cdmaDbm = cdmaDbm;
    }

    public int getCdmaEcio() {
        return cdmaEcio;
    }

    public void setCdmaEcio(int cdmaEcio) {
        this.cdmaEcio = cdmaEcio;
    }

    public int getEvdoDbm() {
        return evdoDbm;
    }

    public void setEvdoDbm(int evdoDbm) {
        this.evdoDbm = evdoDbm;
    }

    public int getEvdoEcio() {
        return evdoEcio;
    }

    public void setEvdoEcio(int evdoEcio) {
        this.evdoEcio = evdoEcio;
    }

    public int getEvdoSnr() {
        return evdoSnr;
    }

    public void setEvdoSnr(int evdoSnr) {
        this.evdoSnr = evdoSnr;
    }

    public int getEcNo() {
        return ecNo;
    }

    public void setEcNo(int ecNo) {
        this.ecNo = ecNo;
    }

    public int getArfcn() {
        return arfcn;
    }

    public void setArfcn(int arfcn) {
        this.arfcn = arfcn;
    }

    public void setGsmCellInfo(int mcc, int mnc, int lac, long cid) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cid = cid;
        this.networkType = NetworkGroup.Gsm;
    }

    public void setGsmSignalInfo(int asu, int signalStrength, int timingAdvance, int rssi, int arfcn) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.ta = timingAdvance;
        this.rssi = rssi;
        this.arfcn = arfcn;
    }

    public void setWcdmaCellInfo(int mcc, int mnc, int lac, long cid, int psc) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cid = cid;
        this.psc = psc;
        this.networkType = NetworkGroup.Wcdma;
    }

    public void setWcdmaSignalInfo(int asu, int signalStrength, int ecNo, int arfcn) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.ecNo = ecNo;
        this.arfcn = arfcn;
    }

    public void setCdmaCellInfo(int systemId, int networkId, long baseStationId) {
        this.mnc = systemId;
        this.lac = networkId;
        this.cid = baseStationId;
        this.networkType = NetworkGroup.Cdma;
    }

    public void setCdmaSignalInfo(int asu, int signalStrength, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.cdmaDbm = cdmaDbm;
        this.cdmaEcio = cdmaEcio;
        this.evdoDbm = evdoDbm;
        this.evdoEcio = evdoEcio;
        this.evdoSnr = evdoSnr;
    }

    public void setLteCellInfo(int mcc, int mnc, int tac, long ci, int pci) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = tac;
        this.cid = ci;
        this.psc = pci;
        this.networkType = NetworkGroup.Lte;
    }

    public void setLteSignalInfo(int asu, int signalStrength, int timingAdvance, int rsrp, int rsrq, int rssi, int rssnr, int cqi, int arfcn) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.ta = timingAdvance;
        this.rsrp = rsrp;
        this.rsrq = rsrq;
        this.rssi = rssi;
        this.rssnr = rssnr;
        this.cqi = cqi;
        this.arfcn = arfcn;
    }

    public void setNrCellInfo(String mccString, String mncString, int tac, long nci, int pci) {
        this.mcc = StringUtils.toInteger(mccString, Cell.UNKNOWN_CID);
        this.mnc = StringUtils.toInteger(mncString, Cell.UNKNOWN_CID);
        this.lac = tac;
        this.cid = nci;
        this.psc = pci;
        this.networkType = NetworkGroup.Nr;
    }

    public void setNrSignalInfo(int asu, int signalStrength, int csiRsrp, int csiRsrq, int csiSinr, int ssRsrp, int ssRsrq, int ssSinr, int arfcn) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.csiRsrp = csiRsrp;
        this.csiRsrq = csiRsrq;
        this.csiSinr = csiSinr;
        this.ssRsrp = ssRsrp;
        this.ssRsrq = ssRsrq;
        this.ssSinr = ssSinr;
        this.arfcn = arfcn;
    }

    public void setTdscdmaCellInfo(String mccString, String mncString, int lac, long cid, int cpid) {
        this.mcc = StringUtils.toInteger(mccString, Cell.UNKNOWN_CID);
        this.mnc = StringUtils.toInteger(mncString, Cell.UNKNOWN_CID);
        this.lac = lac;
        this.cid = cid;
        this.psc = cpid;
        this.networkType = NetworkGroup.Tdscdma;
    }

    public void setTdscdmaSignalInfo(int asu, int signalStrength, int rscp, int arfcn) {
        this.asu = asu;
        this.dbm = signalStrength;
        this.rscp = rscp;
        this.arfcn = arfcn;
    }

    public void setGsmCellLocation(int mcc, int mnc, int lac, long cid, NetworkGroup networkType) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cid = cid;
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
    }

    public void setCdmaCellLocation(int systemId, int networkId, long baseStationId) {
        this.mnc = systemId;
        this.lac = networkId;
        this.cid = baseStationId;
        this.networkType = NetworkGroup.Cdma;
    }

    public void setCdmaLocationSignal(int asu, int signalStrength) {
        this.asu = asu;
        this.dbm = signalStrength;
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
                ", rsrp=" + rsrp +
                ", rsrq=" + rsrq +
                ", rssi=" + rssi +
                ", rssnr=" + rssnr +
                ", cqi=" + cqi +
                ", rscp=" + rscp +
                ", csiRsrp=" + csiRsrp +
                ", csiRsrq=" + csiRsrq +
                ", csiSinr=" + csiSinr +
                ", ssRsrp=" + ssRsrp +
                ", ssRsrq=" + ssRsrq +
                ", ssSinr=" + ssSinr +
                ", cdmaDbm=" + cdmaDbm +
                ", cdmaEcio=" + cdmaEcio +
                ", evdoDbm=" + evdoDbm +
                ", evdoEcio=" + evdoEcio +
                ", evdoSnr=" + evdoSnr +
                ", ecNo=" + ecNo +
                ", arfcn=" + arfcn +
                '}';
    }
}
