/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Locale;

import timber.log.Timber;


public class HashUtils {


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
            Timber.e(ex, "toSha1(): Unsupported hashing algorithm %s", hashingAlgorithm);
        } catch (UnsupportedEncodingException ex) {
            Timber.e(ex, "toSha1(): Unsupported encoding %s", textEncoding);
        }
        return text;
    }

    public static String toSha1(double latitude, double longitude, double accuracy, double speed, double bearing, double altitude) {
        final Locale LOCALE = Locale.ENGLISH;

        NumberFormat coordsDoubleFormatter = NumberFormat.getNumberInstance(LOCALE);
        coordsDoubleFormatter.setGroupingUsed(false);
        coordsDoubleFormatter.setMinimumFractionDigits(9);
        coordsDoubleFormatter.setMaximumFractionDigits(9);

        NumberFormat gpsDoubleFormatter = NumberFormat.getNumberInstance(LOCALE);
        gpsDoubleFormatter.setGroupingUsed(false);
        gpsDoubleFormatter.setMinimumFractionDigits(2);
        gpsDoubleFormatter.setMaximumFractionDigits(2);

        StringBuilder sb = new StringBuilder();

        sb.append(coordsDoubleFormatter.format(latitude)).append("_");
        sb.append(coordsDoubleFormatter.format(longitude)).append("_");
        sb.append(gpsDoubleFormatter.format(accuracy)).append("_");
        sb.append(gpsDoubleFormatter.format(speed)).append("_");
        sb.append(gpsDoubleFormatter.format(bearing)).append("_");
        sb.append(gpsDoubleFormatter.format(altitude));

        return toSha1(sb.toString());
    }
}
