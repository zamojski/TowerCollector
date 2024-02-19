/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.controls.TrimmedEditTextPreference;
import info.zamojski.soft.towercollector.utils.NetworkUtils;
import info.zamojski.soft.towercollector.utils.OpenCellIdUtils;
import timber.log.Timber;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

public class UploadPreferenceFragment extends DialogEnabledPreferenceFragment implements OnSharedPreferenceChangeListener {

    private TrimmedEditTextPreference apiKeyPreference;
    private SwitchPreferenceCompat customMlsEnabledPreference;
    private TrimmedEditTextPreference customMlsUrlPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_upload, rootKey);

        apiKeyPreference = findPreference(getString(R.string.preferences_opencellid_api_key_key));
        apiKeyPreference.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        });

        customMlsEnabledPreference = findPreference(getString(R.string.preferences_custom_mls_enabled_key));
        customMlsUrlPreference = findPreference(getString(R.string.preferences_custom_mls_url_key));
        customMlsUrlPreference.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        });

        setupRegisterApiKeyLink();

        setupApiKeyFormatDialog();
        setupAboutOpenCellIdDialog();
        setupAboutMlsDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        // set summaries
        apiKeyPreference.setSummary(formatValueString(R.string.preferences_opencellid_api_key_summary, (apiKeyPreference.getText().length() > 0 ? apiKeyPreference.getText() : getString(R.string.preferences_value_undefined))));
        customMlsUrlPreference.setVisible(customMlsEnabledPreference.isChecked());
        customMlsUrlPreference.setSummary(formatValueString(R.string.preferences_custom_mls_url_summary, (customMlsUrlPreference.getText().length() > 0 ? customMlsUrlPreference.getText() : getString(R.string.preferences_value_undefined))));
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Timber.d("onSharedPreferenceChanged(): Preference value changed: %s", key);
        if (key.equals(getString(R.string.preferences_opencellid_api_key_key))) {
            String apiKeyValue = apiKeyPreference.getText();
            Timber.d("onSharedPreferenceChanged(): User set API key = \"%s\"", apiKeyValue);
            boolean isApiKeyEmpty = TextUtils.isEmpty(apiKeyValue);
            apiKeyPreference.setSummary(formatValueString(R.string.preferences_opencellid_api_key_summary, (!isApiKeyEmpty ? apiKeyValue : getString(R.string.preferences_value_undefined))));
            if (!isApiKeyEmpty && !OpenCellIdUtils.isApiKeyValid(apiKeyValue)) {
                Timber.d("onSharedPreferenceChanged(): User defined invalid API key = \"%s\"", apiKeyValue);
                Timber.i("onSharedPreferenceChanged(): User defined invalid API key");
                Toast.makeText(getActivity(), getString(R.string.preferences_opencellid_api_key_invalid), Toast.LENGTH_LONG).show();
            }
        } else if (key.equals(getString(R.string.preferences_custom_mls_enabled_key))) {
            boolean isCustomMlsEnabled = customMlsEnabledPreference.isChecked();
            customMlsUrlPreference.setVisible(isCustomMlsEnabled);
        } else if (key.equals(getString(R.string.preferences_custom_mls_url_key))) {
            String customMlsUrlValue = customMlsUrlPreference.getText();
            Timber.d("onSharedPreferenceChanged(): User set custom MLS url = \"%s\"", customMlsUrlValue);
            boolean isCustomMlsUrlEmpty = TextUtils.isEmpty(customMlsUrlValue);
            boolean isCustomMlsUrlValid = NetworkUtils.isValidUrl(customMlsUrlValue);
            customMlsUrlPreference.setSummary(formatValueString(R.string.preferences_custom_mls_url_summary, (!isCustomMlsUrlEmpty ? customMlsUrlValue : getString(R.string.preferences_value_undefined))));
            if (!isCustomMlsUrlEmpty && !isCustomMlsUrlValid) {
                Timber.d("onSharedPreferenceChanged(): User defined invalid custom MLS url = \"%s\"", customMlsUrlValue);
                Timber.i("onSharedPreferenceChanged(): User defined invalid custom MLS url");
                Toast.makeText(getActivity(), getString(R.string.preferences_custom_mls_url_invalid), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupRegisterApiKeyLink() {
        setupOpenInDefaultWebBrowser(R.string.preferences_opencellid_api_key_link_key, R.string.preferences_opencellid_org_sign_up_link);
    }

    private void setupApiKeyFormatDialog() {
        setupDialog(R.string.preferences_about_opencellid_api_key_key, R.string.info_about_api_key_title, R.raw.info_about_api_key_content);
    }

    private void setupAboutOpenCellIdDialog() {
        setupDialog(R.string.preferences_about_opencellid_project_key, R.string.info_about_opencellid_project_title, R.raw.info_about_opencellid_project_content);
    }

    private void setupAboutMlsDialog() {
        setupDialog(R.string.preferences_about_mls_project_key, R.string.info_about_mls_project_title, R.raw.info_about_mls_project_content);
    }
}
