/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import org.greenrobot.eventbus.EventBus;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.events.MapEnabledChanged;
import info.zamojski.soft.towercollector.utils.MapUtils;

public class MapPreferenceFragment extends DialogEnabledPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SwitchPreferenceCompat mapEnablePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_map);

        mapEnablePreference = findPreference(getString(R.string.preferences_main_map_enable_key));

        MapUtils.configureMap(MyApplication.getApplication());
        setupClearCache();
    }

    private void setupClearCache() {
        PreferenceScreen preference = findPreference(getString(R.string.preferences_main_map_clear_cache_key));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MapUtils.clearMapCache(getActivity());
                return true;
            }
        });
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
        if (key.equals(getString(R.string.preferences_main_map_enable_key))) {
            boolean mapEnabledValue = mapEnablePreference.isChecked();
            if (!mapEnabledValue) {
                MapUtils.clearMapCache(getActivity());
            }
            EventBus.getDefault().postSticky(new MapEnabledChanged());
        } else if (key.equals(getString(R.string.preferences_main_map_force_light_theme_key))) {
            Toast.makeText(getActivity(), R.string.preferences_restart_app, Toast.LENGTH_SHORT).show();
        }
    }
}
