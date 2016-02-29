/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import info.zamojski.soft.towercollector.PreferencesActivity;

import android.content.Intent;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

public class PreferenceFragmentBase extends PreferenceFragment {

    /**
     * This is to be called when PreferenceFragment is opened directly,
     * not through PreferencesActivity.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), PreferencesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
