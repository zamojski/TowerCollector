/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import info.zamojski.soft.towercollector.enums.NetworkGroup;

public abstract class CellBase {

    public static final int UNKNOWN_CID = Integer.MAX_VALUE; // safe for all network types except 5G (NR), equals CellInfo.UNAVAILABLE which requires newer SDK
    public static final long UNKNOWN_CID_LONG = Long.MAX_VALUE; // safe for 5G (NR), equals CellInfo.UNAVAILABLE_LONG which requires newer SDK

    /**
     * Mobile Country Code.
     */
    protected int mcc = Cell.UNKNOWN_CID;
    /**
     * Mobile Network Code.
     */
    protected int mnc = Cell.UNKNOWN_CID;
    /**
     * Location Area Code.
     */
    protected int lac = Cell.UNKNOWN_CID;
    /**
     * Cell Tower ID.
     */
    protected long cid = Cell.UNKNOWN_CID_LONG;
    /**
     * Network Type.
     */
    protected NetworkGroup networkType = NetworkGroup.Unknown;
    /**
     * Discovered at Unix Timestamp with milliseconds.
     */
    protected long discoveredAt;
    /**
     * Is cell neighboring.
     */
    protected boolean neighboring = false;

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

    public long getDiscoveredAt() {
        return discoveredAt;
    }

    public void setDiscoveredAt(long discoveredAt) {
        this.discoveredAt = discoveredAt;
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
        else if(networkType == NetworkGroup.Lte) // LTE (reversed order)
            return cid / 256;
        return Cell.UNKNOWN_CID_LONG;
    }

    // NOTE: keep synchronized with Cell implementation
    public long getRnc() {
        if (cid <= 65536)
            return Cell.UNKNOWN_CID_LONG;
        if (networkType == NetworkGroup.Wcdma)
            return cid / 65536;
        else if(networkType == NetworkGroup.Lte) // LTE (reversed order)
            return cid % 256;
        return Cell.UNKNOWN_CID_LONG;
    }
}
