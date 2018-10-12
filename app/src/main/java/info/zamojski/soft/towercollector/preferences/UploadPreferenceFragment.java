/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.controls.TrimmedEditTextPreference;
import info.zamojski.soft.towercollector.utils.Validator;
import timber.log.Timber;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

public class UploadPreferenceFragment extends DialogEnabledPreferenceFragment implements OnSharedPreferenceChangeListener {

    private TrimmedEditTextPreference apiKeyPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_upload);

        apiKeyPreference = (TrimmedEditTextPreference) findPreference(getString(R.string.preferences_api_key_key));

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
        apiKeyPreference.setSummary(formatValueString(R.string.preferences_api_key_summary, (apiKeyPreference.getText().length() > 0 ? apiKeyPreference.getText() : getString(R.string.preferences_value_undefined))));
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Timber.d("onSharedPreferenceChanged(): Preference value changed: %s", key);
        if (key.equals(getString(R.string.preferences_api_key_key))) {
            String apiKeyValue = apiKeyPreference.getText();
            Timber.d("onSharedPreferenceChanged(): User set API key = \"%s\"", apiKeyValue);
            boolean isApiKeyEmpty = TextUtils.isEmpty(apiKeyValue);
            apiKeyPreference.setSummary(formatValueString(R.string.preferences_api_key_summary, (!isApiKeyEmpty ? apiKeyValue : getString(R.string.preferences_value_undefined))));
            if (!isApiKeyEmpty && !Validator.isOpenCellIdApiKeyValid(apiKeyValue)) {
                Timber.d("onSharedPreferenceChanged(): User defined invalid API key = \"%s\"", apiKeyValue);
                Timber.i("onSharedPreferenceChanged(): User defined invalid API key");
                Toast.makeText(getActivity(), getString(R.string.preferences_api_key_invalid), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupRegisterApiKeyLink() {
        setupOpenInDefaultWebBrowser(R.string.preferences_api_key_link_key, R.string.preferences_opencellid_org_sign_up_link);
    }

    private void setupApiKeyFormatDialog() {
        setupDialog(R.string.preferences_about_api_key_key, R.string.info_about_api_key_title, R.raw.info_about_api_key_content);
    }

    private void setupAboutOpenCellIdDialog() {
        setupDialog(R.string.preferences_about_opencellid_project_key, R.string.info_about_opencellid_project_title, R.raw.info_about_opencellid_project_content);
    }

    private void setupAboutMlsDialog() {
        setupDialog(R.string.preferences_about_mls_project_key, R.string.info_about_mls_project_title, R.raw.info_about_mls_project_content);
    }
}
