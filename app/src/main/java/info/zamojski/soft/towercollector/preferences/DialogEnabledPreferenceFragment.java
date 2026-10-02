/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.controls.DialogManager;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import info.zamojski.soft.towercollector.controls.ManagedHtmlInfoDialogFragment;
import info.zamojski.soft.towercollector.controls.ManagedConfirmationDialogFragment;

public abstract class DialogEnabledPreferenceFragment extends PreferenceFragmentBase implements ManagedConfirmationDialogFragment.ConfirmationListener {

    @Override
    public void onConfirmed(int actionId) {
        // Default empty implementation. Subclasses can override to handle confirmations.
    }

    protected void setupDialog(int preferenceKey, final int title, final int content) {
        setupDialog(preferenceKey, title, content, false);
    }

    protected void setupDialog(final int preferenceKey, final int title, final int content, final boolean textIsSelectable) {
        PreferenceScreen preference = findPreference(getString(preferenceKey));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ManagedHtmlInfoDialogFragment fragment = ManagedHtmlInfoDialogFragment.newInstance(title, content, false, textIsSelectable);
                fragment.show(getChildFragmentManager(), ManagedHtmlInfoDialogFragment.TAG);
                MyApplication.getAnalytics().sendHelpDialogOpened(getString(preferenceKey));
                return true;
            }
        });
    }

    protected void setupOpenInDefaultWebBrowser(int preferenceKey, final int urlResourceId) {
        PreferenceScreen preference = findPreference(getString(preferenceKey));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    Intent open = new Intent(Intent.ACTION_VIEW);
                    open.setData(Uri.parse(getString(urlResourceId)));
                    startActivity(Intent.createChooser(open, null));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), R.string.web_browser_missing, Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }

    protected void setupDialog(final int preferenceKey, final int title, final String content) {
        PreferenceScreen preference = findPreference(getString(preferenceKey));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ManagedHtmlInfoDialogFragment fragment = ManagedHtmlInfoDialogFragment.newInstance(title, content, false, false);
                fragment.show(getChildFragmentManager(), ManagedHtmlInfoDialogFragment.TAG);
                MyApplication.getAnalytics().sendHelpDialogOpened(getString(preferenceKey));
                return true;
            }
        });
    }

    protected void showConfirmationDialog(final int preferenceKey, final int title, final int content, final int actionId) {
        PreferenceScreen preference = findPreference(getString(preferenceKey));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ManagedConfirmationDialogFragment fragment = ManagedConfirmationDialogFragment.newInstance(title, content, actionId);
                fragment.show(getChildFragmentManager(), ManagedConfirmationDialogFragment.TAG);
                return true;
            }
        });
    }

    protected void setupOnClick(final int preferenceKey, final Preference.OnPreferenceClickListener clickAction) {
        PreferenceScreen preference = findPreference(getString(preferenceKey));
        preference.setOnPreferenceClickListener(clickAction);
    }
}
