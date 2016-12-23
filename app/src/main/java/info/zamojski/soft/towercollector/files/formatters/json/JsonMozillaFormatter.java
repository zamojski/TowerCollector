/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.providers.MozillaCellUtils;
import info.zamojski.soft.towercollector.providers.ICellUtils;
import info.zamojski.soft.towercollector.utils.StringUtils;

public class JsonMozillaFormatter extends JsonFormatterBase implements IJsonFormatter {

    private static final ICellUtils cellUtils;

    static {
        cellUtils = new MozillaCellUtils();
    }

    @Override
    public String formatList(List<Measurement> ms) throws JSONException {
        if (ms.size() == 0) {
            return new JSONObject().toString();
        }
        JSONArray items = new JSONArray();
        for (Measurement m : ms) {
            if (m.getNetworkType() == NetworkGroup.Cdma || m.getMcc() == Measurement.UNKNOWN_CID)
                continue; // Not supported
            JSONObject item = new JSONObject();
            item.put("timestamp", m.getTimestamp());
            JSONObject position = new JSONObject();
            position.put("latitude", formatCoordinate(m.getLatitude()));
            position.put("longitude", formatCoordinate(m.getLongitude()));
            position.put("accuracy", formatGpsValue(m.getGpsAccuracy()));
            position.put("altitude", formatGpsValue(m.getGpsAltitude()));
            position.put("heading", formatGpsValue(m.getGpsBearing()));
            position.put("speed", formatGpsValue(m.getGpsSpeed()));
            position.put("source", "gps");
            item.put("position", position);
            JSONArray cellTowers = new JSONArray();
            JSONObject cellTower = new JSONObject();
            cellTower.put("radioType", formatRadioType(m.getNetworkType()));
            cellTower.put("mobileCountryCode", formatInt(m.getMcc()));
            cellTower.put("mobileNetworkCode", formatInt(m.getMnc()));
            cellTower.put("locationAreaCode", formatInt(m.getLac()));
            cellTower.put("cellId", formatInt(m.getCid()));
            cellTower.put("primaryScramblingCode", formatNullable(m.getPsc(), Measurement.UNKNOWN_CID));
            cellTower.put("asu", formatNullable(m.getAsu(), Measurement.UNKNOWN_SIGNAL));
            cellTower.put("signalStrength", formatNullable(m.getDbm(), Measurement.UNKNOWN_SIGNAL));
            cellTower.put("timingAdvance", formatNullable(m.getTa(), Measurement.UNKNOWN_SIGNAL));
            cellTower.put("serving", m.isNeighboring() ? 0 : 1);
            cellTowers.put(cellTowers);
            item.put("cellTowers", cellTowers);
            items.put(item);
        }
        JSONObject root = new JSONObject();
        root.put("items", items);
        return root.toString();
    }

    private Object formatRadioType(NetworkGroup networkGroup) {
        String systemType = cellUtils.getSystemType(networkGroup);
        if (StringUtils.isNullEmptyOrWhitespace(systemType))
            return JSONObject.NULL;
        return systemType;
    }
}
