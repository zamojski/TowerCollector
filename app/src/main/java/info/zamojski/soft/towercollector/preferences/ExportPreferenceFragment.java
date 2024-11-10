/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import info.zamojski.soft.towercollector.R;
import timber.log.Timber;

public class ExportPreferenceFragment extends PreferenceFragmentBase implements OnSharedPreferenceChangeListener {

    private ListPreference compressionFormatPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_export);

        compressionFormatPreference = findPreference(getString(R.string.preferences_export_compression_format_key));
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        // set summaries
        setupListPreferenceSummary(compressionFormatPreference, R.string.preferences_export_compression_format_summary);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.preferences_export_compression_format_key).equals(key)) {
            String compressionFormatValue = compressionFormatPreference.getValue();
            CharSequence compressionFormatLabel = compressionFormatPreference.getEntry();
            Timber.d("onSharedPreferenceChanged(): User set compression format = \"%s\"", compressionFormatValue);
            compressionFormatPreference.setSummary(formatValueString(R.string.preferences_export_compression_format_summary, compressionFormatLabel));
        }
    }
}
