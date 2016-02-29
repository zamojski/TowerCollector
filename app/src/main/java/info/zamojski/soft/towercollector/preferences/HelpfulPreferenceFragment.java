/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.controls.DialogManager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public abstract class HelpfulPreferenceFragment extends PreferenceFragmentBase {

    protected void setupDialog(int preferenceKey, final int title, final int content) {
        PreferenceScreen preference = (PreferenceScreen) findPreference(getString(preferenceKey));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogManager.createHtmlInfoDialog(getActivity(), title, content, false, false).show();
                return true;
            }
        });
    }

    protected void setupDialog(int preferenceKey, final int title, final int content, final boolean textIsSelectable) {
        PreferenceScreen preference = (PreferenceScreen) findPreference(getString(preferenceKey));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogManager.createHtmlInfoDialog(getActivity(), title, content, false, textIsSelectable).show();
                return true;
            }
        });
    }

    protected void setupOpenInDefault(int preferenceKey, final int urlResourceId) {
        PreferenceScreen preference = (PreferenceScreen) findPreference(getString(preferenceKey));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    Intent open = new Intent(Intent.ACTION_VIEW);
                    open.setData(Uri.parse(getString(urlResourceId)));
                    startActivity(open);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), R.string.web_browser_missing, Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }

    protected void setupDialog(int preferenceKey, final int title, final String content) {
        PreferenceScreen preference = (PreferenceScreen) findPreference(getString(preferenceKey));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogManager.createHtmlInfoDialog(getActivity(), title, content, false, false).show();
                return true;
            }
        });
    }

    protected String formatValueString(int textId, CharSequence value) {
        String text = getString(textId);
        String formattedValue = getString(R.string.preferences_current_value_summary_format, value);
        return text + formattedValue;
    }
}
