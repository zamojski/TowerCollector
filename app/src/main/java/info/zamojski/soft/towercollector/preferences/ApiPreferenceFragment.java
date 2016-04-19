/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.controls.TrimmedEditTextPreference;
import info.zamojski.soft.towercollector.utils.Validator;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import trikita.log.Log;
import android.widget.Toast;

public class ApiPreferenceFragment extends HelpfulPreferenceFragment implements OnSharedPreferenceChangeListener {

    private static final String TAG = ApiPreferenceFragment.class.getSimpleName();

    private TrimmedEditTextPreference apiKeyPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_api);

        apiKeyPreference = (TrimmedEditTextPreference) findPreference(getString(R.string.preferences_api_key_key));

        setupRegisterApiKeyLink();

        setupApiKeyFormatDialog();
        setupAboutOpenCellIdDialog();
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
        Log.d(TAG, "onSharedPreferenceChanged(): Preference value changed: " + key);
        if (key.equals(getString(R.string.preferences_api_key_key))) {
            String apiKeyValue = apiKeyPreference.getText();
            Log.d(TAG, "onSharedPreferenceChanged(): User set API key = \"" + apiKeyValue + "\"");
            boolean isApiKeyEmpty = TextUtils.isEmpty(apiKeyValue);
            apiKeyPreference.setSummary(formatValueString(R.string.preferences_api_key_summary, (!isApiKeyEmpty ? apiKeyValue : getString(R.string.preferences_value_undefined))));
            if (!isApiKeyEmpty && !Validator.isOpenCellIdApiKeyValid(apiKeyValue)) {
                Log.d(TAG, "onSharedPreferenceChanged(): User defined invalid API key = \"" + apiKeyValue + "\"");
                Log.i(TAG, "onSharedPreferenceChanged(): User defined invalid API key");
                Toast.makeText(getActivity(), getString(R.string.preferences_api_key_invalid), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupRegisterApiKeyLink() {
        setupOpenInDefault(R.string.preferences_api_key_link_key, R.string.preferences_opencellid_org_sign_up_link);
    }

    private void setupApiKeyFormatDialog() {
        setupDialog(R.string.preferences_about_api_key_key, R.string.info_about_api_key_title, R.raw.info_about_api_key_content);
    }

    private void setupAboutOpenCellIdDialog() {
        setupDialog(R.string.preferences_about_opencellid_project_key, R.string.info_about_opencellid_project_title, R.raw.info_about_opencellid_project_content);
    }
}
