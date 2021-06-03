/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class MapCell extends CellBase implements Serializable {

    private static final long serialVersionUID = -1567513676324180202L;

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
