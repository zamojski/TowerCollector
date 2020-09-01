/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.csv;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.providers.GeneralCellUtils;
import info.zamojski.soft.towercollector.providers.ICellUtils;

public class CsvExportFormatter extends CsvFormatter {

    private static final NumberFormat gpsDoubleFormatter;
    private static final SimpleDateFormat exportDateFormatter;

    private static final ICellUtils cellUtils;

    static {
        gpsDoubleFormatter = NumberFormat.getNumberInstance(LOCALE);
        gpsDoubleFormatter.setGroupingUsed(false);
        gpsDoubleFormatter.setMinimumFractionDigits(0);
        gpsDoubleFormatter.setMaximumFractionDigits(2);
        cellUtils = new GeneralCellUtils();
        exportDateFormatter = new SimpleDateFormat("\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\"", LOCALE);
        exportDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String formatHeader() {
        return "mcc,mnc,lac,cell_id,psc,asu,dbm,ta,lat,lon,accuracy,speed,bearing,altitude,measured_at,net_type,neighboring,device,rsrp,rsrq,rssi,rssnr,cqi,rscp,csi_rsrp,csi_rsrq,csi_sinr,ss_rsrp,ss_rsrq,ss_sinr,cdma_dbm,cdma_ecio,evdo_dbm,evdo_ecio,evdo_snr\r\n";
    }

    @Override
    public String formatEntry(Measurement m) {
        StringBuilder sb = new StringBuilder(150);

        for (Cell c : m.getCells()) {
            // mcc value only when defined
            int mcc = c.getMcc();
            if (mcc != Cell.UNKNOWN_CID)
                sb.append(formatInt(mcc));
            sb.append(',');
            sb.append(formatInt(c.getMnc()));
            sb.append(',');
            sb.append(formatInt(c.getLac()));
            sb.append(',');
            sb.append(formatLong(c.getCid()));
            sb.append(',');
            int psc = c.getPsc();
            if (psc != Cell.UNKNOWN_CID)
                sb.append(formatInt(psc));
            sb.append(',');

            sb.append(formatAsuSignal(c.getAsu()));
            sb.append(',');
            sb.append(formatDbmSignal(c.getDbm()));
            sb.append(',');
            int ta = c.getTa();
            if (ta != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(ta));
            sb.append(',');

            sb.append(formatCoordinate(m.getLatitude()));
            sb.append(',');
            sb.append(formatCoordinate(m.getLongitude()));
            sb.append(',');

            sb.append(formatGpsValue(m.getGpsAccuracy()));
            sb.append(',');
            sb.append(formatGpsValue(m.getGpsSpeed()));
            sb.append(',');
            sb.append(formatGpsValue(m.getGpsBearing()));
            sb.append(',');
            sb.append(formatGpsValue(m.getGpsAltitude()));
            sb.append(',');

            sb.append(formatDate(m.getMeasuredAt()));
            sb.append(',');

            sb.append(cellUtils.getSystemType(c.getNetworkType()));
            sb.append(',');

            sb.append(c.isNeighboring());
            sb.append(',');

            sb.append("\"");
            sb.append(deviceName);
            sb.append("\"");

            sb.append(',');
            int rsrp = c.getRsrp();
            if (rsrp != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(rsrp));
            sb.append(',');
            int rsrq = c.getRsrq();
            if (rsrq != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(rsrq));
            sb.append(',');
            int rssi = c.getRssi();
            if (rssi != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(rssi));
            sb.append(',');
            int rssnr = c.getRssnr();
            if (rssnr != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(rssnr));
            sb.append(',');
            int cqi = c.getCqi();
            if (cqi != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(cqi));
            sb.append(',');
            int rscp = c.getRscp();
            if (rscp != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(rscp));
            sb.append(',');
            int csiRsrp = c.getCsiRsrp();
            if (csiRsrp != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(csiRsrp));
            sb.append(',');
            int csiRsrq = c.getCsiRsrq();
            if (csiRsrq != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(csiRsrq));
            sb.append(',');
            int csiSinr = c.getCsiSinr();
            if (csiSinr != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(csiSinr));
            sb.append(',');
            int ssRsrp = c.getSsRsrp();
            if (ssRsrp != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(ssRsrp));
            sb.append(',');
            int ssRsrq = c.getSsRsrq();
            if (ssRsrq != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(ssRsrq));
            sb.append(',');
            int ssSinr = c.getSsSinr();
            if (ssSinr != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(ssSinr));
            sb.append(',');
            int cdmaDbm = c.getCdmaDbm();
            if (cdmaDbm != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(cdmaDbm));
            sb.append(',');
            int cdmaEcio = c.getCdmaEcio();
            if (cdmaEcio != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(cdmaEcio));
            sb.append(',');
            int evdoDbm = c.getEvdoDbm();
            if (evdoDbm != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(evdoDbm));
            sb.append(',');
            int evdoEcio = c.getEvdoEcio();
            if (evdoEcio != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(evdoEcio));
            sb.append(',');
            int evdoSnr = c.getEvdoSnr();
            if (evdoSnr != Cell.UNKNOWN_SIGNAL)
                sb.append(formatInt(evdoSnr));

            sb.append("\r\n");
        }

        return sb.toString();
    }

    private String formatGpsValue(double value) {
        return gpsDoubleFormatter.format(value);
    }

    private String formatAsuSignal(int asu) {
        String asuString;
        if (asu != Cell.UNKNOWN_SIGNAL)
            asuString = String.valueOf(asu);
        else
            asuString = "";
        return asuString;
    }

    private String formatDbmSignal(int dbm) {
        if (dbm != Cell.UNKNOWN_SIGNAL)
            return String.valueOf(dbm);
        else
            return "";
    }

    private String formatDate(long timestamp) {
        return exportDateFormatter.format(new Date(timestamp));
    }
}
