/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.utils.MobileUtils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;

import trikita.log.Log;

import android.widget.Toast;

public class AdvancedPreferenceFragment extends DialogEnabledPreferenceFragment implements OnSharedPreferenceChangeListener {

    private static final String TAG = AdvancedPreferenceFragment.class.getSimpleName();

    private ListPreference collectorApiVersionPreference;
    private ListPreference fileLoggingLevelPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_advanced);

        collectorApiVersionPreference = (ListPreference) findPreference(getString(R.string.preferences_collector_api_version_key));
        fileLoggingLevelPreference = (ListPreference) findPreference(getString(R.string.preferences_file_logging_level_key));

        setupApiVersionDialog();

        setupApiVersionSelection();
    }

    private void setupApiVersionSelection() {
        boolean api17Compatible = MobileUtils.isApi17VersionCompatible();
        if (api17Compatible) {
            collectorApiVersionPreference.setEnabled(true);
        } else {
            collectorApiVersionPreference.setValue(getString(R.string.preferences_collector_api_version_entries_value_api_1));
            collectorApiVersionPreference.setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        // set summaries
        setupListPreferenceSummary(collectorApiVersionPreference, R.string.preferences_collector_api_version_summary);
        setupListPreferenceSummary(fileLoggingLevelPreference, R.string.preferences_file_logging_level_summary);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.preferences_collector_api_version_key))) {
            String collectorApiVersionValue = collectorApiVersionPreference.getValue();
            CharSequence collectorApiVersionLabel = collectorApiVersionPreference.getEntry();
            Log.d(TAG, "onSharedPreferenceChanged(): User set api version = \"%s\"", collectorApiVersionValue);
            collectorApiVersionPreference.setSummary(formatValueString(R.string.preferences_collector_api_version_summary, collectorApiVersionLabel));
            setupApiVersionSelection();
            Toast.makeText(getActivity(), R.string.preferences_restart_collector, Toast.LENGTH_SHORT).show();
        } else if (key.equals(getString(R.string.preferences_file_logging_level_key))) {
            String fileLoggingLevelValue = fileLoggingLevelPreference.getValue();
            CharSequence fileLoggingLevelLabel = fileLoggingLevelPreference.getEntry();
            Log.d(TAG, "onSharedPreferenceChanged(): User set file logging level = \"%s\"", fileLoggingLevelValue);
            fileLoggingLevelPreference.setSummary(formatValueString(R.string.preferences_file_logging_level_summary, fileLoggingLevelLabel));
        }
    }

    private void setupApiVersionDialog() {
        setupDialog(R.string.preferences_about_collector_api_version_key, R.string.info_about_collector_api_version_title, R.raw.info_about_collector_api_version_content);
    }
}
