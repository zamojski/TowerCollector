/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

class IntegerPreferenceProvider extends PreferenceProviderBase<Integer> {

    public IntegerPreferenceProvider(Context context) {
        super(context);
    }

    @Override
    Integer getPreferenceDefaultValue(int defaultValueKey) {
        Resources resources = context.getResources();
        return resources.getInteger(defaultValueKey);
    }

    @Override
    Integer getPreferenceValue(SharedPreferences prefs, int valueKey, Integer defaultValue) {
        String key = context.getString(valueKey);
        return prefs.getInt(key, defaultValue);
    }

    @Override
    void setPreferenceValue(Editor editor, int valueKey, Integer value) {
        String key = context.getString(valueKey);
        editor.putInt(key, value);
    }

}
