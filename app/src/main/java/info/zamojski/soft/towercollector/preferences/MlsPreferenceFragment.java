/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import android.os.Bundle;

import info.zamojski.soft.towercollector.R;

public class MlsPreferenceFragment extends DialogEnabledPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_mls);

        setupAboutMlsDialog();
    }

    private void setupAboutMlsDialog() {
        setupDialog(R.string.preferences_about_mls_project_key, R.string.info_about_mls_project_title, R.raw.info_about_mls_project_content);
    }
}
