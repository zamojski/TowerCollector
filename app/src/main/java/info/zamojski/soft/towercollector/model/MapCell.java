/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import info.zamojski.soft.towercollector.enums.NetworkGroup;

public class MapCell implements Serializable {

    private static final long serialVersionUID = -1567513676324180202L;

    /**
     * Mobile Country Code.
     */
    private int mcc = Cell.UNKNOWN_CID;
    /**
     * Mobile Network Code.
     */
    private int mnc = Cell.UNKNOWN_CID;
    /**
     * Location Area Code.
     */
    private int lac = Cell.UNKNOWN_CID;
    /**
     * Cell Tower ID.
     */
    private long cid = Cell.UNKNOWN_CID_LONG;
    /**
     * Network Type.
     */
    private NetworkGroup networkType = NetworkGroup.Unknown;
    /**
     * Is cell neighboring.
     */
    private boolean neighboring = false;

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

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    // NOTE: keep synchronized with Cell implementation
    public boolean isCidLong() {
        return ((getNetworkType() == NetworkGroup.Wcdma || getNetworkType() == NetworkGroup.Lte) && getLongCid() != Cell.UNKNOWN_CID_LONG);
    }

    // NOTE: keep synchronized with Cell implementation
    public long getLongCid() {
        if (cid <= 65536)
            return Cell.UNKNOWN_CID_LONG;
        return cid;
    }

    // NOTE: keep synchronized with Cell implementation
    public long getShortCid() {
        if (cid <= 65536)
            return Cell.UNKNOWN_CID_LONG;
        if (networkType == NetworkGroup.Wcdma)
            return cid % 65536;
        else if(networkType== NetworkGroup.Lte) // LTE (reversed order)
            return cid / 256;
        return Cell.UNKNOWN_CID_LONG;
    }

    // NOTE: keep synchronized with Cell implementation
    public long getRnc() {
        if (cid <= 65536)
            return Cell.UNKNOWN_CID_LONG;
        if (networkType == NetworkGroup.Wcdma)
            return cid / 65536;
        else if(networkType== NetworkGroup.Lte) // LTE (reversed order)
            return cid % 256;
        return Cell.UNKNOWN_CID_LONG;
    }

    public static MapCell fromCell(Cell c) {
        MapCell cc = new MapCell();
        cc.setMcc(c.getMcc());
        cc.setMnc(c.getMnc());
        cc.setLac(c.getLac());
        cc.setCid(c.getCid());
        cc.setNetworkType(c.getNetworkType());
        cc.setNeighboring(c.isNeighboring());
        return cc;
    }

    @NotNull
    @Override
    public String toString() {
        return "MapCell{" +
                "mcc=" + mcc +
                ", mnc=" + mnc +
                ", lac=" + lac +
                ", cid=" + cid +
                ", networkType=" + networkType +
                ", neighboring=" + neighboring +
                '}';
    }
}
