/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

import androidx.annotation.IntegerRes;
import androidx.annotation.StringRes;

class FloatPreferenceProvider extends PreferenceProviderBase<Float> {

    public FloatPreferenceProvider(Context context) {
        super(context);
    }

    @Override
    Float getPreferenceDefaultValue(@IntegerRes int defaultValueKey) {
        Resources resources = context.getResources();
        return (float)resources.getInteger(defaultValueKey);
    }

    @Override
    Float getPreferenceValue(SharedPreferences prefs, @StringRes int valueKey, Float defaultValue) {
        String key = context.getString(valueKey);
        return prefs.getFloat(key, defaultValue);
    }

    @Override
    void setPreferenceValue(Editor editor, @StringRes int valueKey, Float value) {
        String key = context.getString(valueKey);
        editor.putFloat(key, value);
    }

}
