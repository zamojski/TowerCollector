/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

public class ExceptionUtils {

    public static Throwable getRootCause(Throwable ex) {
        if (ex == null) {
            return null;
        }

        Throwable cause;
        Throwable result = ex;

        while (null != (cause = result.getCause()) && (result != cause)) {
            result = cause;
        }

        return result;
    }
}
