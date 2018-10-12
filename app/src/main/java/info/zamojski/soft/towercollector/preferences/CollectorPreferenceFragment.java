/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.CollectorService;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import timber.log.Timber;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

public class CollectorPreferenceFragment extends DialogEnabledPreferenceFragment implements OnSharedPreferenceChangeListener {

    private ListPreference collectorKeepScreenOnPreference;
    private ListPreference collectorLowBatteryActionPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_collector);

        collectorKeepScreenOnPreference = (ListPreference) findPreference(getString(R.string.preferences_collector_keep_screen_on_mode_key));
        collectorLowBatteryActionPreference = (ListPreference) findPreference(getString(R.string.preferences_collector_low_battery_action_key));

        setupNeighboringCellsDialog();
        setupCollectorKeepScreenOnDialog();
        setupNotifyMeasurementsCollectedDialog();

        setupHideCollectorNotificationAvailability();
    }

    private void setupHideCollectorNotificationAvailability() {
        boolean available = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.O);
        if (!available) {
            PreferenceCategory settingsCategoryPreference = (PreferenceCategory) findPreference(getString(R.string.preferences_general_category_settings_key));
            SwitchPreference hideCollectorNotificationPreference = (SwitchPreference) findPreference(getString(R.string.preferences_hide_collector_notification_key));
            settingsCategoryPreference.removePreference(hideCollectorNotificationPreference);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        // set summaries
        setupListPreferenceSummary(collectorKeepScreenOnPreference, R.string.preferences_collector_keep_screen_on_summary);
        setupListPreferenceSummary(collectorLowBatteryActionPreference, R.string.preferences_collector_low_battery_action_summary);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.preferences_gps_optimizations_enabled_key))
                || key.equals(getString(R.string.preferences_collect_neighboring_cells_key))
                || key.equals(getString(R.string.preferences_notify_measurements_collected_key))
                || key.equals(getString(R.string.preferences_hide_collector_notification_key))) {
            if (MyApplication.isBackgroundTaskRunning(CollectorService.class)) {
                Toast.makeText(getActivity(), R.string.preferences_restart_collector, Toast.LENGTH_SHORT).show();
            }
        } else if (key.equals(getString(R.string.preferences_collector_keep_screen_on_mode_key))) {
            String collectorKeepScreenOnValue = collectorKeepScreenOnPreference.getValue();
            CharSequence collectorKeepScreenOnLabel = collectorKeepScreenOnPreference.getEntry();
            Timber.d("onSharedPreferenceChanged(): User set keep screen on = \"%s\"", collectorKeepScreenOnValue);
            collectorKeepScreenOnPreference.setSummary(formatValueString(R.string.preferences_collector_keep_screen_on_summary, collectorKeepScreenOnLabel));
            if (MyApplication.isBackgroundTaskRunning(CollectorService.class)) {
                Toast.makeText(getActivity(), R.string.preferences_restart_collector, Toast.LENGTH_SHORT).show();
            }
        } else if (key.equals(getString(R.string.preferences_collector_low_battery_action_key))) {
            String collectorLowBatteryActionValue = collectorLowBatteryActionPreference.getValue();
            CharSequence collectorLowBatteryActionLabel = collectorLowBatteryActionPreference.getEntry();
            Timber.d("onSharedPreferenceChanged(): User set low battery action = \"%s\"", collectorLowBatteryActionValue);
            collectorLowBatteryActionPreference.setSummary(formatValueString(R.string.preferences_collector_low_battery_action_summary, collectorLowBatteryActionLabel));
        }
    }

    private void setupNeighboringCellsDialog() {
        setupDialog(R.string.preferences_about_neighboring_cells_key, R.string.info_about_neighboring_cells_title, R.raw.info_about_neighboring_cells_content);
    }

    private void setupNotifyMeasurementsCollectedDialog() {
        setupDialog(R.string.preferences_about_collector_keep_screen_on_key, R.string.info_about_collector_keep_screen_on_title, R.raw.info_about_collector_keep_screen_on_content);
    }

    private void setupCollectorKeepScreenOnDialog() {
        setupDialog(R.string.preferences_about_notify_measurements_collected_key, R.string.info_about_notify_measurements_collected_title, R.raw.info_about_notify_measurements_collected_content, true);
    }
}
