/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.text.TextUtils;

import trikita.log.Log;

public class StringUtils {

    private static final String TAG = StringUtils.class.getSimpleName();

    public static String encodeHtml(String content) {
        final String textEncoding = "UTF-8";
        String encodedContent = content;
        try {
            encodedContent = URLEncoder.encode(encodedContent, textEncoding);
        } catch (UnsupportedEncodingException ex) {
            Log.e(TAG, "encodeHtml(): Unsupported encoding " + textEncoding, ex);
        }
        return encodedContent.replaceAll("\\+", " ");
    }

    public static float toFloat(String value) {
        if (TextUtils.isEmpty(value))
            return 0.0f;
        try {
            float val = Float.parseFloat(value);
            return val;
        } catch (NumberFormatException ex) {
            return 0.0f;
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