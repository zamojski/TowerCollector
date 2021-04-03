/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import timber.log.Timber;

public class DisplayPreferenceFragment extends DialogEnabledPreferenceFragment implements OnSharedPreferenceChangeListener {

    private ListPreference appThemePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_display);

        appThemePreference = findPreference(getString(R.string.preferences_app_theme_mode_key));

        setupMainKeepScreenOnDialog();
        setupNotificationSettingsLink();
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
            Timber.d("onSharedPreferenceChanged(): User set app theme = \"%s\"", appThemeValue);
            appThemePreference.setSummary(formatValueString(R.string.preferences_app_theme_summary, appThemeLabel));
            MyApplication.getApplication().initTheme();
            Toast.makeText(getActivity(), R.string.preferences_restart_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMainKeepScreenOnDialog() {
        setupDialog(R.string.preferences_about_main_keep_screen_on_key, R.string.info_about_main_keep_screen_on_title, R.raw.info_about_main_keep_screen_on_content);
    }

    private void setupNotificationSettingsLink() {
        PreferenceScreen preference = findPreference(getString(R.string.preferences_notification_settings_key));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openNotificationSettingsActivity();
                return true;
            }
        });
    }

    private void openNotificationSettingsActivity() {
        Intent settingsIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, MyApplication.getApplication().getPackageName());
        } else {
            settingsIntent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("app_package", MyApplication.getApplication().getPackageName())
                    .putExtra("app_uid", MyApplication.getApplication().getApplicationInfo().uid);
        }
        try {
            startActivity(settingsIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), R.string.dialog_could_not_open_android_settings, Toast.LENGTH_LONG).show();
        }
    }
}
