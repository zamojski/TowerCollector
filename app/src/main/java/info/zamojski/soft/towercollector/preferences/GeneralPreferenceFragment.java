/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import info.zamojski.soft.towercollector.BuildConfig;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.utils.StorageUtils;
import timber.log.Timber;

public class GeneralPreferenceFragment extends DialogEnabledPreferenceFragment implements OnSharedPreferenceChangeListener {

    private PreferenceScreen changeStorageLocationPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_general);

        changeStorageLocationPreference = findPreference(getString(R.string.preferences_change_storage_location_key));

        setupUsageStatisticsAvailability();
        setupChangeStorageLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
        changeStorageLocationPreference.setSummary(formatValueStringPoor(R.string.preferences_change_storage_location_summary, (storageUri == null ? getString(R.string.preferences_value_undefined) : storageUri.getPath())));
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
            MyApplication.getAnalytics().setTrackingEnabled(isTrackingEnabled);
        }
    }

    private void setupUsageStatisticsAvailability() {
        boolean available = BuildConfig.ANALYTICS_AVAILABLE;
        if (!available) {
            PreferenceCategory settingsCategoryPreference = findPreference(getString(R.string.preferences_general_category_settings_key));
            SwitchPreferenceCompat trackingPreference = findPreference(getString(R.string.preferences_tracking_enabled_key));
            settingsCategoryPreference.removePreference(trackingPreference);

            // We have only one help preference so the whole category is to be removed
            PreferenceScreen generalScreenPreference = getPreferenceScreen();
            PreferenceCategory helpCategoryPreference = findPreference(getString(R.string.preferences_general_category_help_key));
            generalScreenPreference.removePreference(helpCategoryPreference);
        } else {
            setupUsageStatisticsDialog();
        }
    }

    private void setupUsageStatisticsDialog() {
        setupDialog(R.string.preferences_about_tracking_key, R.string.info_usage_statistics_title, R.raw.info_usage_statistics_content);
    }

    private void setupChangeStorageLocation() {
        setupOnClick(R.string.preferences_change_storage_location_key, new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                StorageUtils.requestStorageUri(getActivity());
                return true;
            }
        });
    }

    private String formatValueStringPoor(int textId, CharSequence value) {
        String text = getString(textId);
        String formattedValue = getString(R.string.preferences_current_value_summary_poor_format, value);
        return text + formattedValue;
    }
}
