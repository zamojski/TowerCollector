/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import info.zamojski.soft.towercollector.BuildConfig;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import timber.log.Timber;

public class GeneralPreferenceFragment extends DialogEnabledPreferenceFragment implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_general);

        setupUsageStatisticsAvailability();
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.preferences_tracking_enabled_key))) {
            boolean trackingEnabledDefault = getResources().getBoolean(R.bool.preferences_tracking_enabled_default_value);
            boolean isTrackingEnabled = sharedPreferences.getBoolean(key, trackingEnabledDefault);
            Timber.d("onSharedPreferenceChanged(): User set tracking enabled = %s", isTrackingEnabled);
            MyApplication.getAnalytics().setAppOptOut(!isTrackingEnabled);
        }
    }

    private void setupUsageStatisticsAvailability() {
        boolean available = BuildConfig.ANALYTICS_AVAILABLE;
        if (!available) {
            PreferenceCategory settingsCategoryPreference = (PreferenceCategory) findPreference(getString(R.string.preferences_general_category_settings_key));
            SwitchPreference trackingPreference = (SwitchPreference) findPreference(getString(R.string.preferences_tracking_enabled_key));
            settingsCategoryPreference.removePreference(trackingPreference);

            // We have only one help preference so the whole category is to be removed
            PreferenceScreen generalScreenPreference = getPreferenceScreen();
            PreferenceCategory helpCategoryPreference = (PreferenceCategory) findPreference(getString(R.string.preferences_general_category_help_key));
            generalScreenPreference.removePreference(helpCategoryPreference);
        } else {
            setupUsageStatisticsDialog();
        }
    }

    private void setupUsageStatisticsDialog() {
        setupDialog(R.string.preferences_about_tracking_key, R.string.info_usage_statistics_title, R.raw.info_usage_statistics_content);
    }
}
