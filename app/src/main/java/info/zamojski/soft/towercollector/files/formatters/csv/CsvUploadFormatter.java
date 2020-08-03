/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.csv;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.providers.ICellUtils;
import info.zamojski.soft.towercollector.providers.OpencellidCellUtils;
import info.zamojski.soft.towercollector.utils.StringUtils;

public class CsvUploadFormatter extends CsvFormatter {

    private static final ICellUtils cellUtils;
    private static final SimpleDateFormat uploadDateFormatter;

    static {
        cellUtils = new OpencellidCellUtils();
        deviceName = StringUtils.substring(deviceName, 0, 50);
        uploadDateFormatter = new SimpleDateFormat("\"yyyy-MM-dd HH:mm:ss.SSS'Z'\"", LOCALE);
        uploadDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String formatHeader() {
        return "lat,lon,mcc,mnc,sid,lac,tac,nid,cellid,bid,psc,pci,signal,ta,measured_at,rating,speed,direction,act,devn\n";
    }

    @Override
    public String formatRow(Measurement m) {
        StringBuilder sb = new StringBuilder(140);

        for (Cell c : m.getCells()) {
            sb.append(formatCoordinate(m.getLatitude()));
            sb.append(',');
            sb.append(formatCoordinate(m.getLongitude()));
            sb.append(',');

            // depending on cell type
            if (c.getNetworkType() == NetworkGroup.Cdma || c.getMcc() == Cell.UNKNOWN_CID) {
                // mcc
                sb.append(',');
                // mnc
                sb.append(',');
                // sid
                sb.append(formatInt(c.getMnc()));
                sb.append(',');
                // lac
                sb.append(',');
                // tac
                sb.append(',');
                // nid
                sb.append(formatInt(c.getLac()));
                sb.append(',');
                // cellid
                sb.append(',');
                // bid
                sb.append(formatLong(c.getCid()));
                sb.append(',');
            } else {
                // mcc
                sb.append(formatInt(c.getMcc()));
                sb.append(',');
                // mnc
                sb.append(formatInt(c.getMnc()));
                sb.append(',');
                // sid
                sb.append(',');
                // lac
                if (c.getNetworkType() != NetworkGroup.Lte && c.getNetworkType() != NetworkGroup.Nr)
                    sb.append(formatInt(c.getLac()));
                sb.append(',');
                // tac
                if (c.getNetworkType() == NetworkGroup.Lte || c.getNetworkType() == NetworkGroup.Nr)
                    sb.append(formatInt(c.getLac()));
                sb.append(',');
                // nid
                sb.append(',');
                // cellid
                sb.append(formatLong(c.getCid()));
                sb.append(',');
                // bid
                sb.append(',');
            }

            // psc
            int psc = c.getPsc();
            if (psc != Cell.UNKNOWN_CID && (c.getNetworkType() == NetworkGroup.Wcdma || c.getNetworkType() == NetworkGroup.Tdscdma))
                sb.append(formatInt(psc));
            sb.append(',');
            // pci
            if (psc != Cell.UNKNOWN_CID && (c.getNetworkType() == NetworkGroup.Lte || c.getNetworkType() == NetworkGroup.Nr))
                sb.append(formatInt(psc));
            sb.append(',');

            sb.append(formatSignal(c.getAsu(), c.getDbm()));
            sb.append(',');
            int ta = c.getTa();
            if (ta != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(ta));
            sb.append(',');

            sb.append(formatDate(m.getMeasuredAt()));
            sb.append(',');

            sb.append(formatInt(convertToInt(m.getGpsAccuracy())));
            sb.append(',');
            sb.append(formatInt(convertToInt(m.getGpsSpeed())));
            sb.append(',');
            sb.append(formatInt(convertToInt(m.getGpsBearing())));
            sb.append(',');

            sb.append(cellUtils.getSystemType(c.getNetworkType()));
            sb.append(',');

            sb.append("\"");
            sb.append(deviceName);
            sb.append("\"");

            sb.append('\n');
        }

        return sb.toString();
    }

    private int convertToInt(double value) {
        return (int) Math.round(value);
    }

    private String formatSignal(int asu, int dbm) {
        String signalString;
        if (asu != Cell.UNKNOWN_SIGNAL)
            signalString = String.valueOf(asu);
        else if (dbm != Cell.UNKNOWN_SIGNAL)
            signalString = String.valueOf(dbm);
        else
            signalString = "";
        return signalString;
    }

    private String formatDate(long timestamp) {
        return uploadDateFormatter.format(new Date(timestamp));
    }
}
