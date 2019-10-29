/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.text.TextUtils;

public class StringUtils {

    public static int toInteger(String value, int defaultValue) {
        if (TextUtils.isEmpty(value))
            return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public static boolean isNullEmptyOrWhitespace(String value) {
        return value == null || value.isEmpty() || value.trim().isEmpty();
    }

    public static String substring(String s, int start, int length) {
        return s.substring(start, Math.min(start + length, s.length()));
    }

    public static boolean mayBeJson(String string) {
        return !isNullEmptyOrWhitespace(string)
                && ("null".equals(string)
                || (string.startsWith("[") && string.endsWith("]")) || (string.startsWith("{") && string.endsWith("}")));
    }
}