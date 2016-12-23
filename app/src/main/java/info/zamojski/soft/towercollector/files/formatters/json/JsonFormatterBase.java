/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.json;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

import info.zamojski.soft.towercollector.providers.GeneralCellUtils;
import info.zamojski.soft.towercollector.providers.ICellUtils;

public abstract class JsonFormatterBase {
    protected static final Locale LOCALE = Locale.ENGLISH;

    private static final NumberFormat coordsDoubleFormatter;
    private static final NumberFormat gpsDoubleFormatter;

    protected static final NumberFormat intFormatter;

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
    }

    protected Object formatNullable(int value, int invalid) {
        if (value != invalid)
            return formatInt(value);
        return JSONObject.NULL;
    }

    protected String formatCoordinate(double value) {
        return coordsDoubleFormatter.format(value);
    }

    protected String formatGpsValue(double value) {
        return gpsDoubleFormatter.format(value);
    }

    protected String formatInt(int value) {
        return intFormatter.format(value);
    }
}
