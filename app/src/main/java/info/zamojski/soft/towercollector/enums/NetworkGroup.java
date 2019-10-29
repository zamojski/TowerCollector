/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.enums;

public enum NetworkGroup {
    Unknown(0),
    Cdma(1),
    Gsm(2),
    Wcdma(3),
    Lte(4),
    Nr(5),
    Tdscdma(6);

    private int value;

    private NetworkGroup(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static NetworkGroup fromValue(int value) {
        switch (value) {
            case 1:
                return Cdma;
            case 2:
                return Gsm;
            case 3:
                return Wcdma;
            case 4:
                return Lte;
            case 5:
                return Nr;
            case 6:
                return Tdscdma;
            case 0:
            default:
                return Unknown;
        }
    }
}
