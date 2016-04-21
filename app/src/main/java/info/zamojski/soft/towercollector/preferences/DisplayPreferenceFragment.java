/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;

import trikita.log.Log;

import android.widget.Toast;

public class DisplayPreferenceFragment extends DialogEnabledPreferenceFragment implements OnSharedPreferenceChangeListener {

    private static final String TAG = DisplayPreferenceFragment.class.getSimpleName();

    private ListPreference appThemePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_display);

        appThemePreference = (ListPreference) findPreference(getString(R.string.preferences_app_theme_mode_key));

        setupMainKeepScreenOnDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        // set summaries
        setupListPreferenceSummary(appThemePreference, R.string.preferences_app_theme_summary);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.preferences_app_theme_mode_key))) {
            String appThemeValue = appThemePreference.getValue();
            CharSequence appThemeLabel = appThemePreference.getEntry();
            Log.d(TAG, "onSharedPreferenceChanged(): User set app theme = \"" + appThemeValue + "\"");
            appThemePreference.setSummary(formatValueString(R.string.preferences_app_theme_summary, appThemeLabel));
            MyApplication.getApplication().initTheme();
            Toast.makeText(getActivity(), R.string.preferences_restart_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMainKeepScreenOnDialog() {
        setupDialog(R.string.preferences_about_main_keep_screen_on_key, R.string.info_about_main_keep_screen_on_title, R.raw.info_about_main_keep_screen_on_content);
    }
}
