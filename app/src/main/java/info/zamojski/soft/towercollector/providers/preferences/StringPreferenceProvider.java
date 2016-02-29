/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

class StringPreferenceProvider extends PreferenceProviderBase<String> {

    private static final String TAG = StringPreferenceProvider.class.getSimpleName();

    public StringPreferenceProvider(Context context) {
        super(context);
    }

    @Override
    String getLogTag() {
        return TAG;
    }

    @Override
    String getPreferenceDefaultValue(int defaultValueKey) {
        Resources resources = context.getResources();
        return resources.getString(defaultValueKey);
    }

    @Override
    String getPreferenceValue(SharedPreferences prefs, int valueKey, String defaultValue) {
        String key = context.getString(valueKey);
        return prefs.getString(key, defaultValue);
    }

    @Override
    void setPreferenceValue(Editor editor, int valueKey, String value) {
        String key = context.getString(valueKey);
        editor.putString(key, value);
    }

}
