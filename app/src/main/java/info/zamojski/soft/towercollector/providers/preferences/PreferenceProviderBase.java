/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers.preferences;

import info.zamojski.soft.towercollector.MyApplication;

import org.acra.ACRA;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

abstract class PreferenceProviderBase<T> {

    protected Context context;

    public PreferenceProviderBase(Context context) {
        this.context = context;
    }

    public T getPreference(int valueKey, int defaultValueKey) {
        T value;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        T defaultValue = getPreferenceDefaultValue(defaultValueKey);
        try {
            value = getPreferenceValue(prefs, valueKey, defaultValue);
            Log.d(getLogTag(), "getPreference(): Preference `" + context.getString(valueKey) + "` loaded with value `" + value + "`");
        } catch (ClassCastException ex) {
            Log.e(getLogTag(), "getPreference(): Error while loading preference `" + context.getString(valueKey) + "`, restoring default", ex);
            MyApplication.getAnalytics().sendException(ex, Boolean.FALSE);
            ACRA.getErrorReporter().handleSilentException(ex);
            value = defaultValue;
            SharedPreferences.Editor editor = prefs.edit();
            setPreferenceValue(editor, valueKey, defaultValue);
            editor.commit();
        }
        return value;
    }

    public void setPreference(int valueKey, T value) {
        Log.d(getLogTag(), "setPreference(): Preference `" + context.getString(valueKey) + "` value set to `" + value + "`");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        setPreferenceValue(editor, valueKey, value);
        editor.commit();
    }

    abstract String getLogTag();

    abstract T getPreferenceDefaultValue(int defaultValueKey);

    abstract T getPreferenceValue(SharedPreferences prefs, int valueKey, T defaultValue);

    abstract void setPreferenceValue(SharedPreferences.Editor editor, int valueKey, T value);
}
