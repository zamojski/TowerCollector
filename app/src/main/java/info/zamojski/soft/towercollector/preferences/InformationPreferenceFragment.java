/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.BuildConfig;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.utils.ApkUtils;
import info.zamojski.soft.towercollector.utils.DateUtils;
import info.zamojski.soft.towercollector.utils.ResourceUtils;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

public class InformationPreferenceFragment extends DialogEnabledPreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_information);

        setupWebsiteLink();
        setupDonateLink();
        setupGitHubLink();
        setupFacebookCommunityLink();
        setupTwitterCommunityLink();
        setupTranslationToolLink();

        setupAboutDialog();
        setupContactEmailLink();
        setupPrivacyStatementDialog();
        setupOpenSourceLicensesDialog();
    }

    private void setupAboutDialog() {
        String versionName = ApkUtils.getApkVersionNameWithSuffix();
        String content = ResourceUtils.getRawString(getActivity(), R.raw.info_about_application_content)
                .replace("%VERSION_NAME%", versionName)
                .replace("%CURRENT_YEAR%", String.valueOf(DateUtils.getCurrentYear()));
        setupDialog(R.string.preferences_about_link_key, R.string.info_about_application_title, content);
    }

    private void setupContactEmailLink() {
        PreferenceScreen preference = findPreference(getString(R.string.preferences_contact_email_link_key));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent mail = new Intent(Intent.ACTION_SEND);
                mail.setType("message/rfc822");
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{BuildConfig.CONTACT_EMAIL});
                mail.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.preferences_email_title));
                mail.putExtra(Intent.EXTRA_TEXT, ApkUtils.getInstallationInfo(MyApplication.getApplication()));
                startActivity(Intent.createChooser(mail, getString(R.string.dialog_select_email_app)));
                return true;
            }
        });
    }

    private void setupWebsiteLink() {
        setupOpenInDefaultWebBrowser(R.string.preferences_website_link_key, R.string.preferences_website_link);
    }

    private void setupDonateLink() {
        setupOpenInDefaultWebBrowser(R.string.preferences_donate_link_key, R.string.preferences_donate_link);
    }

    private void setupGitHubLink() {
        setupOpenInDefaultWebBrowser(R.string.preferences_github_link_key, R.string.preferences_github_link);
    }

    private void setupFacebookCommunityLink() {
        setupOpenInDefaultWebBrowser(R.string.preferences_facebook_community_link_key, R.string.preferences_facebook_community_link);
    }

    private void setupTwitterCommunityLink() {
        setupOpenInDefaultWebBrowser(R.string.preferences_twitter_community_link_key, R.string.preferences_twitter_community_link);
    }

    private void setupTranslationToolLink() {
        setupOpenInDefaultWebBrowser(R.string.preferences_translate_link_key, R.string.preferences_translation_tool_link);
    }

    private void setupPrivacyStatementDialog() {
        setupDialog(R.string.preferences_privacy_statement_link_key, R.string.info_privacy_statement_title, R.raw.info_privacy_statement_content);
    }

    private void setupOpenSourceLicensesDialog() {
        setupDialog(R.string.preferences_open_source_licenses_link_key, R.string.info_open_source_licenses_title, R.raw.info_open_source_licenses_content);
    }
}
