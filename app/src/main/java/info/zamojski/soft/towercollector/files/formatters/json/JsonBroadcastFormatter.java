/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.providers.GeneralCellUtils;
import info.zamojski.soft.towercollector.providers.ICellUtils;

public class JsonBroadcastFormatter implements IJsonFormatter {

    protected static final Locale LOCALE = Locale.ENGLISH;

    private static final NumberFormat coordsDoubleFormatter;
    private static final NumberFormat gpsDoubleFormatter;
    private static final SimpleDateFormat exportDateFormatter;

    protected static final NumberFormat intFormatter;

    private static final ICellUtils cellUtils;

    static {
        coordsDoubleFormatter = NumberFormat.getNumberInstance(LOCALE);
        coordsDoubleFormatter.setGroupingUsed(false);
        coordsDoubleFormatter.setMinimumFractionDigits(8);
        coordsDoubleFormatter.setMaximumFractionDigits(12);

        gpsDoubleFormatter = NumberFormat.getNumberInstance(LOCALE);
        gpsDoubleFormatter.setGroupingUsed(false);
        gpsDoubleFormatter.setMinimumFractionDigits(0);
        gpsDoubleFormatter.setMaximumFractionDigits(2);

        intFormatter = NumberFormat.getNumberInstance(LOCALE);
        intFormatter.setParseIntegerOnly(true);
        intFormatter.setGroupingUsed(false);

        cellUtils = new GeneralCellUtils();

        exportDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", LOCALE);
        exportDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String formatList(List<Measurement> ms) throws JSONException {
        if (ms.size() == 0) {
            return new JSONObject().toString();
        }
        Measurement firstM = ms.get(0);
        JSONObject root = new JSONObject();
        root.put("measured_at", formatDate(firstM.getTimestamp()));
        JSONObject gps = new JSONObject();
        gps.put("lat", formatCoordinate(firstM.getLatitude()));
        gps.put("lon", formatCoordinate(firstM.getLongitude()));
        gps.put("accuracy", formatGpsValue(firstM.getGpsAccuracy()));
        gps.put("speed", formatGpsValue(firstM.getGpsSpeed()));
        gps.put("bearing", formatGpsValue(firstM.getGpsBearing()));
        gps.put("altitude", formatGpsValue(firstM.getGpsAltitude()));
        root.put("gps", gps);
        JSONArray cells = new JSONArray();
        for (Measurement m : ms) {
            JSONObject cell = new JSONObject();
            cell.put("mcc", formatNullable(m.getMcc(), Measurement.UNKNOWN_CID));
            cell.put("mnc", formatInt(m.getMnc()));
            cell.put("lac", formatInt(m.getLac()));
            cell.put("cell_id", formatInt(m.getCid()));
            cell.put("psc", formatNullable(m.getPsc(), Measurement.UNKNOWN_CID));
            cell.put("asu", formatNullable(m.getAsu(), Measurement.UNKNOWN_SIGNAL));
            cell.put("dbm", formatNullable(m.getDbm(), Measurement.UNKNOWN_SIGNAL));
            cell.put("ta", formatNullable(m.getTa(), Measurement.UNKNOWN_SIGNAL));
            cell.put("neighboring", m.isNeighboring());
            cell.put("net_type", cellUtils.getSystemType(m.getNetworkType()));
            cells.put(cell);
        }
        root.put("cells", cells);
        return root.toString();
    }

    private Object formatNullable(int value, int invalid) {
        if (value != invalid)
            return formatInt(value);
        return JSONObject.NULL;
    }

    private String formatCoordinate(double value) {
        return coordsDoubleFormatter.format(value);
    }

    private String formatGpsValue(double value) {
        return gpsDoubleFormatter.format(value);
    }

    private String formatDate(long timestamp) {
        return exportDateFormatter.format(new Date(timestamp));
    }

    private String formatInt(int value) {
        return intFormatter.format(value);
    }
}
