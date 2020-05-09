/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.providers.GeneralCellUtils;
import info.zamojski.soft.towercollector.providers.ICellUtils;

public class JsonBroadcastFormatter extends JsonFormatterBase implements IJsonFormatter {

    private static final SimpleDateFormat exportDateFormatter;

    private static final ICellUtils cellUtils;

    static {
        exportDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", LOCALE);
        exportDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        cellUtils = new GeneralCellUtils();
    }

    @Override
    public String formatHeader() {
        return "";
    }

    @Override
    public String formatList(List<Measurement> ms) throws JSONException {
        if (ms.size() == 0) {
            return new JSONObject().toString();
        }
        JSONObject root = new JSONObject();
        for (Measurement m : ms) {
            root.put("measured_at", formatDate(m.getMeasuredAt()));
            JSONObject gps = new JSONObject();
            gps.put("lat", formatCoordinate(m.getLatitude()));
            gps.put("lon", formatCoordinate(m.getLongitude()));
            gps.put("accuracy", formatGpsValue(m.getGpsAccuracy()));
            gps.put("speed", formatGpsValue(m.getGpsSpeed()));
            gps.put("bearing", formatGpsValue(m.getGpsBearing()));
            gps.put("altitude", formatGpsValue(m.getGpsAltitude()));
            root.put("gps", gps);
            JSONArray cells = new JSONArray();
            for (Cell c : m.getCells()) {
                JSONObject cell = new JSONObject();
                cell.put("mcc", formatNullable(c.getMcc(), Cell.UNKNOWN_CID));
                cell.put("mnc", c.getMnc());
                cell.put("lac", c.getLac());
                cell.put("cell_id", c.getCid());
                cell.put("psc", formatNullable(c.getPsc(), Cell.UNKNOWN_CID));
                cell.put("asu", formatNullable(c.getAsu(), Cell.UNKNOWN_SIGNAL));
                cell.put("dbm", formatNullable(c.getDbm(), Cell.UNKNOWN_SIGNAL));
                cell.put("ta", formatNullable(c.getTa(), Cell.UNKNOWN_SIGNAL));
                cell.put("rsrp", formatNullable(c.getRsrp(), Cell.UNKNOWN_SIGNAL));
                cell.put("rsrq", formatNullable(c.getRsrq(), Cell.UNKNOWN_SIGNAL));
                cell.put("rssi", formatNullable(c.getRssi(), Cell.UNKNOWN_SIGNAL));
                cell.put("rssnr", formatNullable(c.getRssnr(), Cell.UNKNOWN_SIGNAL));
                cell.put("cqi", formatNullable(c.getCqi(), Cell.UNKNOWN_SIGNAL));
                cell.put("rscp", formatNullable(c.getRscp(), Cell.UNKNOWN_SIGNAL));
                cell.put("csi_rsrp", formatNullable(c.getCsiRsrp(), Cell.UNKNOWN_SIGNAL));
                cell.put("csi_rsrq", formatNullable(c.getCsiRsrq(), Cell.UNKNOWN_SIGNAL));
                cell.put("csi_sinr", formatNullable(c.getCsiSinr(), Cell.UNKNOWN_SIGNAL));
                cell.put("ss_rsrp", formatNullable(c.getSsRsrp(), Cell.UNKNOWN_SIGNAL));
                cell.put("ss_rsrq", formatNullable(c.getSsRsrq(), Cell.UNKNOWN_SIGNAL));
                cell.put("ss_sinr", formatNullable(c.getSsSinr(), Cell.UNKNOWN_SIGNAL));
                cell.put("cdma_dbm", formatNullable(c.getCdmaDbm(), Cell.UNKNOWN_SIGNAL));
                cell.put("cdma_ecio", formatNullable(c.getCdmaEcio(), Cell.UNKNOWN_SIGNAL));
                cell.put("evdo_dbm", formatNullable(c.getEvdoDbm(), Cell.UNKNOWN_SIGNAL));
                cell.put("evdo_ecio", formatNullable(c.getEvdoEcio(), Cell.UNKNOWN_SIGNAL));
                cell.put("evdo_snr", formatNullable(c.getEvdoSnr(), Cell.UNKNOWN_SIGNAL));
                cell.put("neighboring", c.isNeighboring());
                cell.put("net_type", cellUtils.getSystemType(c.getNetworkType()));
                cells.put(cell);
            }
            root.put("cells", cells);
        }
        return root.toString();
    }

    @Override
    public String formatNewSegment() {
        return "";
    }

    @Override
    public String formatFooter() {
        return "";
    }

    private String formatDate(long timestamp) {
        return exportDateFormatter.format(new Date(timestamp));
    }

    private Object formatNullable(int value, int invalid) {
        if (value == invalid)
            return JSONObject.NULL;
        return value;
    }
}
