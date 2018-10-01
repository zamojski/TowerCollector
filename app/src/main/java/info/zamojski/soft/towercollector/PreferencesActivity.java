/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import info.zamojski.soft.towercollector.preferences.AdvancedPreferenceFragment;
import info.zamojski.soft.towercollector.preferences.ApiPreferenceFragment;
import info.zamojski.soft.towercollector.preferences.CollectorPreferenceFragment;
import info.zamojski.soft.towercollector.preferences.DisplayPreferenceFragment;
import info.zamojski.soft.towercollector.preferences.GeneralPreferenceFragment;
import info.zamojski.soft.towercollector.preferences.HelpPreferenceFragment;
import info.zamojski.soft.towercollector.preferences.InformationPreferenceFragment;
import info.zamojski.soft.towercollector.utils.ApkUtils;

import java.util.List;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.example.android.supportv7.app.AppCompatPreferenceActivity;

public class PreferencesActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MyApplication.getCurrentAppTheme());
        super.onCreate(savedInstanceState);
        // set fixed screen orientation
        if (!ApkUtils.isRunningOnBuggyOreoSetRequestedOrientation(this))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupActionBar();
    }

    private void setupActionBar() {
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.preferences_toolbar, root, false);
        root.addView(toolbar, 0);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MyApplication.getAnalytics().sendPreferencesActivityStarted();
    }

    @Override
    public void onStop() {
        super.onStop();
        MyApplication.getAnalytics().sendPreferencesActivityStopped();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!super.onOptionsItemSelected(item)) {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return (AdvancedPreferenceFragment.class.getName().equals(fragmentName)
                || ApiPreferenceFragment.class.getName().equals(fragmentName)
                || CollectorPreferenceFragment.class.getName().equals(fragmentName)
                || DisplayPreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || HelpPreferenceFragment.class.getName().equals(fragmentName)
                || InformationPreferenceFragment.class.getName().equals(fragmentName));
    }
}
