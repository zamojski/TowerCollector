/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.R;

import android.os.Bundle;

public class HelpPreferenceFragment extends DialogEnabledPreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_help);

        setupIntroductionDialog();
        setupBestPracticesDialog();
        setupExternalIntegrationDialog();
    }

    private void setupIntroductionDialog() {
        setupDialog(R.string.preferences_introduction_link_key, R.string.info_introduction_title, R.raw.info_introduction_content);
    }

    private void setupBestPracticesDialog() {
        setupDialog(R.string.preferences_best_practices_link_key, R.string.info_best_practices_title, R.raw.info_best_practices_content);
    }

    private void setupExternalIntegrationDialog() {
        setupDialog(R.string.preferences_external_integration_link_key, R.string.info_external_integration_title, R.raw.info_external_integration_content, true);
    }
}
