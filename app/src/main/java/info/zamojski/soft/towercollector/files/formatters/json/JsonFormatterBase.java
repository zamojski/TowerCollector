/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.json;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Locale;

public abstract class JsonFormatterBase {
    protected static final Locale LOCALE = Locale.ENGLISH;

    private static final int COORDS_PRECISION = 12;
    private static final int GPS_VALUE_PRECISION = 2;
    private static final int ROUNDING_METHOD = BigDecimal.ROUND_HALF_EVEN;

    protected Object formatNullable(int value, int invalid) {
        if (value == invalid)
            return JSONObject.NULL;
        return value;
    }

    protected double formatCoordinate(double value) {
        return new BigDecimal(value).setScale(COORDS_PRECISION, ROUNDING_METHOD).doubleValue();
    }

    protected double formatGpsValue(double value) {
        return new BigDecimal(value).setScale(GPS_VALUE_PRECISION, ROUNDING_METHOD).doubleValue();
    }
}
