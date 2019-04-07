/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.providers.ICellUtils;
import info.zamojski.soft.towercollector.providers.MozillaCellUtils;
import info.zamojski.soft.towercollector.utils.StringUtils;

public abstract class JsonMozillaFormatterBase extends JsonFormatterBase {

    private static final ICellUtils cellUtils;

    static {
        cellUtils = new MozillaCellUtils();
    }

    protected List<JSONObject> formatItems(List<Measurement> ms) throws JSONException {
        List<JSONObject> items = new ArrayList<>();
        for (Measurement m : ms) {
            JSONObject item = new JSONObject();
            item.put("timestamp", m.getMeasuredAt());
            JSONObject position = new JSONObject();
            position.put("latitude", formatCoordinate(m.getLatitude()));
            position.put("longitude", formatCoordinate(m.getLongitude()));
            position.put("longitude", formatCoordinate(m.getLongitude()));
            position.put("accuracy", formatGpsValue(m.getGpsAccuracy()));
            position.put("altitude", formatGpsValue(m.getGpsAltitude()));
            position.put("heading", formatGpsValue(m.getGpsBearing()));
            position.put("speed", formatGpsValue(m.getGpsSpeed()));
            position.put("source", "gps");
            item.put("position", position);
            JSONArray cellTowers = new JSONArray();
            for (Cell c : m.getCells()) {
                if (c.getNetworkType() == NetworkGroup.Cdma || c.getMcc() == Cell.UNKNOWN_CID)
                    continue; // Not supported
                JSONObject cellTower = new JSONObject();
                cellTower.put("radioType", formatRadioType(c.getNetworkType()));
                cellTower.put("mobileCountryCode", c.getMcc());
                cellTower.put("mobileNetworkCode", c.getMnc());
                cellTower.put("locationAreaCode", c.getLac());
                cellTower.put("cellId", c.getCid());
                int psc = c.getPsc();
                if (psc != Cell.UNKNOWN_CID)
                    cellTower.put("primaryScramblingCode", psc);
                int asu = c.getAsu();
                if (asu != Cell.UNKNOWN_SIGNAL)
                    cellTower.put("asu", asu);
                int dbm = c.getDbm();
                if (dbm != Cell.UNKNOWN_SIGNAL)
                    cellTower.put("signalStrength", dbm);
                int ta = c.getTa();
                if (ta != Cell.UNKNOWN_SIGNAL)
                    cellTower.put("timingAdvance", ta);
                cellTower.put("serving", c.isNeighboring() ? 0 : 1);
                cellTowers.put(cellTower);
            }
            item.put("cellTowers", cellTowers);
            // add only if measurement contains valid cells
            if (cellTowers.length() > 0)
                items.add(item);
        }
        return items;
    }

    private Object formatRadioType(NetworkGroup networkGroup) {
        String systemType = cellUtils.getSystemType(networkGroup);
        if (StringUtils.isNullEmptyOrWhitespace(systemType))
            return JSONObject.NULL;
        return systemType;
    }
}
