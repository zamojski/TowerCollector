/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.Log;

public class CollectorPreferenceFragment extends HelpfulPreferenceFragment implements OnSharedPreferenceChangeListener {

    private static final String TAG = CollectorPreferenceFragment.class.getSimpleName();

    private ListPreference collectorKeepScreenOnPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_collector);

        collectorKeepScreenOnPreference = (ListPreference) findPreference(getString(R.string.preferences_collector_keep_screen_on_mode_key));

        setupNeighboringCellsDialog();
        setupCollectorKeepScreenOnDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        // set summaries
        CharSequence collectorKeepScreenOnLabel = collectorKeepScreenOnPreference.getEntry();
        collectorKeepScreenOnPreference.setSummary(formatValueString(R.string.preferences_collector_keep_screen_on_summary, collectorKeepScreenOnLabel));
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.preferences_collector_keep_screen_on_mode_key))) {
            String collectorKeepScreenOnValue = collectorKeepScreenOnPreference.getValue();
            CharSequence collectorKeepScreenOnLabel = collectorKeepScreenOnPreference.getEntry();
            Log.d(TAG, "onSharedPreferenceChanged(): User set keep screen on = \"" + collectorKeepScreenOnValue + "\"");
            collectorKeepScreenOnPreference.setSummary(formatValueString(R.string.preferences_collector_keep_screen_on_summary, collectorKeepScreenOnLabel));
        }
    }

    private void setupNeighboringCellsDialog() {
        setupDialog(R.string.preferences_about_neighboring_cells_key, R.string.info_about_neighboring_cells_title, R.raw.info_about_neighboring_cells_content);
    }

    private void setupCollectorKeepScreenOnDialog() {
        setupDialog(R.string.preferences_about_collector_keep_screen_on_key, R.string.info_about_collector_keep_screen_on_title, R.raw.info_about_collector_keep_screen_on_content);
    }
}
