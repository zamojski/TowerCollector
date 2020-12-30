/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.IntegerRes;
import androidx.annotation.StringRes;

import info.zamojski.soft.towercollector.MyApplication;
import timber.log.Timber;

abstract class PreferenceProviderBase<T> {

    protected Context context;

    PreferenceProviderBase(Context context) {
        this.context = context;
    }

    T getPreference(@StringRes int valueKey, int defaultValueKey) {
        T value;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        T defaultValue = getPreferenceDefaultValue(defaultValueKey);
        try {
            value = getPreferenceValue(prefs, valueKey, defaultValue);
            Timber.d("getPreference(): Preference `%s` loaded with value `%s`", context.getString(valueKey), value);
        } catch (ClassCastException ex) {
            Timber.e(ex, "getPreference(): Error while loading preference `%s`, restoring default", context.getString(valueKey));
            MyApplication.handleSilentException(ex);
            value = defaultValue;
            SharedPreferences.Editor editor = prefs.edit();
            setPreferenceValue(editor, valueKey, defaultValue);
            editor.apply();
        }
        return value;
    }

    void setPreference(@StringRes int valueKey, T value) {
        Timber.d("setPreference(): Preference `%s` value set to `%s`", context.getString(valueKey), value);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        setPreferenceValue(editor, valueKey, value);
        editor.apply();
    }

    abstract T getPreferenceDefaultValue(@IntegerRes int defaultValueKey);

    abstract T getPreferenceValue(SharedPreferences prefs, @StringRes int valueKey, T defaultValue);

    abstract void setPreferenceValue(SharedPreferences.Editor editor, @StringRes int valueKey, T value);
}
