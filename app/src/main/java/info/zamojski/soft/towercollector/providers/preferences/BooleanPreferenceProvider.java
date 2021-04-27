/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import androidx.annotation.BoolRes;
import androidx.annotation.StringRes;

public class BooleanPreferenceProvider extends PreferenceProviderBase<Boolean> {

    public BooleanPreferenceProvider(Context context) {
        super(context);
    }

    @Override
    Boolean getPreferenceDefaultValue(@BoolRes int defaultValueKey) {
        Resources resources = context.getResources();
        return resources.getBoolean(defaultValueKey);
    }

    @Override
    Boolean getPreferenceValue(SharedPreferences prefs, @StringRes int valueKey, Boolean defaultValue) {
        String key = context.getString(valueKey);
        return prefs.getBoolean(key, defaultValue);
    }

    @Override
    void setPreferenceValue(Editor editor, @StringRes int valueKey, Boolean value) {
        String key = context.getString(valueKey);
        editor.putBoolean(key, value);
    }

}
