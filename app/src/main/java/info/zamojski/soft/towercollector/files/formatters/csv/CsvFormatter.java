/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.csv;

import info.zamojski.soft.towercollector.utils.ApkUtils;

import java.text.NumberFormat;
import java.util.Locale;

public abstract class CsvFormatter implements ICsvFormatter {

    protected static final Locale LOCALE = Locale.ENGLISH;

    protected static final NumberFormat coordsDoubleFormater;

    protected static String deviceName;

    static {
        coordsDoubleFormater = NumberFormat.getNumberInstance(LOCALE);
        coordsDoubleFormater.setGroupingUsed(false);
        coordsDoubleFormater.setMinimumFractionDigits(8);
        coordsDoubleFormater.setMaximumFractionDigits(12);

        deviceName = ApkUtils.getDeviceName();
    }

    protected String formatCoordinate(double value) {
        return coordsDoubleFormater.format(value);
    }
}
