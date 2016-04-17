/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.BuildConfig;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.utils.ApkUtils;
import info.zamojski.soft.towercollector.utils.ResourceUtils;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

public class InformationPreferenceFragment extends HelpfulPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_information);

        setupWebsiteLink();
        setupGooglePlusCommunityLink();
        setupTranslationToolLink();

        setupAboutDialog();
        setupContactEmailLink();
        setupPrivacyStatementDialog();
        setupOpenSourceLicensesDialog();
    }

    private void setupAboutDialog() {
        String versionName = ApkUtils.getApkVersionName(getActivity());
        String content = ResourceUtils.getRawResource(getActivity(), R.raw.info_about_application_content).replace("%VERSION_NAME%", versionName);
        setupDialog(R.string.preferences_about_link_key, R.string.info_about_application_title, content);
    }

    private void setupContactEmailLink() {
        PreferenceScreen preference = (PreferenceScreen) findPreference(getString(R.string.preferences_contact_email_link_key));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent mail = new Intent(Intent.ACTION_SEND);
                mail.setType("message/rfc822");
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{BuildConfig.CONTACT_EMAIL});
                mail.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.preferences_email_title));
                mail.putExtra(Intent.EXTRA_TEXT, ApkUtils.getInstallationInfo(getActivity()));
                startActivity(Intent.createChooser(mail, getString(R.string.dialog_select_email_app)));
                return true;
            }
        });
    }

    private void setupWebsiteLink() {
        setupOpenInDefault(R.string.preferences_website_link_key, R.string.preferences_website_link);
    }

    private void setupGooglePlusCommunityLink() {
        setupOpenInDefault(R.string.preferences_google_plus_community_link_key, R.string.preferences_google_plus_community_link);
    }

    private void setupTranslationToolLink() {
        setupOpenInDefault(R.string.preferences_translate_link_key, R.string.preferences_translation_tool_link);
    }

    private void setupPrivacyStatementDialog() {
        setupDialog(R.string.preferences_privacy_statement_link_key, R.string.info_privacy_statement_title, R.raw.info_privacy_statement_content);
    }

    private void setupOpenSourceLicensesDialog() {
        setupDialog(R.string.preferences_open_source_licenses_link_key, R.string.info_open_source_licenses_title, R.raw.info_open_source_licenses_content);
    }
}
