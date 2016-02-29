/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class GeneralPreferenceFragment extends HelpfulPreferenceFragment implements OnSharedPreferenceChangeListener {

    private static final String TAG = GeneralPreferenceFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_general);

        setupUsageStatisticsDialog();
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
            Log.d(TAG, "onSharedPreferenceChanged(): User set tracking enabled = " + isTrackingEnabled);
            MyApplication.getAnalytics().setAppOptOut(!isTrackingEnabled);
        }
    }

    private void setupUsageStatisticsDialog() {
        setupDialog(R.string.preferences_about_tracking_key, R.string.info_usage_statistics_title, R.raw.info_usage_statistics_content);
    }
}
