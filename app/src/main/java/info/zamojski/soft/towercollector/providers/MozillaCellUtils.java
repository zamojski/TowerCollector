/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers;

import info.zamojski.soft.towercollector.enums.NetworkGroup;

public class MozillaCellUtils implements ICellUtils {
    @Override
    public String getSystemType(NetworkGroup networkType) {
        switch (networkType) {
            case Gsm:
                return "gsm";
            case Wcdma:
                return "wcdma";
            case Lte:
                return "lte";
            case Nr:
                return "nr"; // TODO: verify when supported
            case Tdscdma:
                return "tdscdma"; // TODO: verify when supported
            case Cdma:
            case Unknown:
            default:
                return "";
        }
    }
}
