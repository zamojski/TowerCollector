/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers.preferences.migration;

import android.content.Context;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.providers.preferences.BooleanPreferenceProvider;
import info.zamojski.soft.towercollector.providers.preferences.StringPreferenceProvider;
import info.zamojski.soft.towercollector.utils.StringUtils;

public class UpgradeScript1 extends UpgradeScriptBase implements IUpgradeScript {

    @Override
    public void performUpgrade(Context context) {
        // initialize
        StringPreferenceProvider stringPreferenceProvider = getStringPreferenceProvider(context);
        BooleanPreferenceProvider booleanPreferenceProvider = getBooleanPreferenceProvider(context);

        // set use shared ocid api key if personal api key not provided
        boolean isPersonalApiKeyUndefined = StringUtils.isNullEmptyOrWhitespace(stringPreferenceProvider.getPreference(R.string.preferences_opencellid_api_key_key, R.string.preferences_opencellid_api_key_default_value));
        booleanPreferenceProvider.setPreference(R.string.preferences_opencellid_use_shared_api_key_key, isPersonalApiKeyUndefined);
    }
}
