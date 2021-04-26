/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers;

import info.zamojski.soft.towercollector.enums.NetworkGroup;

public class OpenCellIdCellUtils implements ICellUtils {

    public String getSystemType(NetworkGroup networkType) {
        switch (networkType) {
            case Cdma:
                return "CDMA";
            case Gsm:
                return "GSM";
            case Wcdma:
                return "UMTS";
            case Lte:
                return "LTE";
            case Nr:
                return "NR";
            case Tdscdma:
                return "TDSCDMA";
            case Unknown:
            default:
                return "";
        }
    }
}
