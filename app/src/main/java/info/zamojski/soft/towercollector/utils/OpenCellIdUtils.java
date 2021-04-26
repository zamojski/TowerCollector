/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

public class OpenCellIdUtils {
    public static boolean isApiKeyValid(String apiKey) {
        // old 8 motions - e.g. "9743a66f914cc249efca164485a19c5c"
        // new ENAiKOON - guid, e.g. "9743a66f-914c-c249-efca-164485a19c5c"
        // admin ENAiKOON - there are some custom keys defined by administrators
        // old Unwired Labs - e.g. "9743a66f914cc2"
        // new Unwired Labs - e.g. "pk.9743a66f914cc249efca164485a19c5c"
        return (apiKey.matches("pk\\.[a-fA-F0-9]{32}") || apiKey.matches("[a-fA-F0-9]{14}") || apiKey.matches("[a-fA-F0-9]{32}") || apiKey.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"));
    }
}
