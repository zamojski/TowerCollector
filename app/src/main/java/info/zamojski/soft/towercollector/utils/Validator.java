/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

public class Validator {
    public static boolean isOpenCellIdApiKeyValid(String apiKey) {
        // old - md5, eg. "9743a66f914cc249efca164485a19c5c"
        // new - guid based on sha256, eg. "9743a66f-914c-c249-efca-164485a19c5c"
        // admin - there are some custom keys defined by administrators
        return (apiKey.matches("[a-fA-F0-9]{32}") || apiKey.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"));
    }
}
