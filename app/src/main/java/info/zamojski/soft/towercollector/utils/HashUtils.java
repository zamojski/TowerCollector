/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Locale;

public class HashUtils {

    private static final String TAG = HashUtils.class.getSimpleName();

    public static String toSha1(String text) {
        final String hashingAlgorithm = "SHA-1";
        final String textEncoding = "UTF-8";
        try {
            final MessageDigest digest = MessageDigest.getInstance(hashingAlgorithm);
            byte[] result = digest.digest(text.getBytes(textEncoding));

            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            Log.e(TAG, "toSha1(): Unsupported hashing algorithm " + hashingAlgorithm, ex);
        } catch (UnsupportedEncodingException ex) {
            Log.e(TAG, "toSha1(): Unsupported encoding " + textEncoding, ex);
        }
        return text;
    }

    public static String toSha1(double latitude, double longitude, double accuracy, double speed, double bearing, double altitude) {
        final Locale LOCALE = Locale.ENGLISH;

        NumberFormat coordsDoubleFormater = NumberFormat.getNumberInstance(LOCALE);
        coordsDoubleFormater.setGroupingUsed(false);
        coordsDoubleFormater.setMinimumFractionDigits(9);
        coordsDoubleFormater.setMaximumFractionDigits(9);

        NumberFormat gpsDoubleFormater = NumberFormat.getNumberInstance(LOCALE);
        gpsDoubleFormater.setGroupingUsed(false);
        gpsDoubleFormater.setMinimumFractionDigits(2);
        gpsDoubleFormater.setMaximumFractionDigits(2);

        StringBuilder sb = new StringBuilder();

        sb.append(coordsDoubleFormater.format(latitude)).append("_");
        sb.append(coordsDoubleFormater.format(longitude)).append("_");
        sb.append(gpsDoubleFormater.format(accuracy)).append("_");
        sb.append(gpsDoubleFormater.format(speed)).append("_");
        sb.append(gpsDoubleFormater.format(bearing)).append("_");
        sb.append(gpsDoubleFormater.format(altitude));

        return toSha1(sb.toString());
    }
}
