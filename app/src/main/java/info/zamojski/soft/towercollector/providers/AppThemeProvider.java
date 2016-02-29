/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers;

import info.zamojski.soft.towercollector.R;

import android.content.Context;

public class AppThemeProvider {

    private String lightThemeName, darkThemeName;

    public AppThemeProvider(Context context) {
        this.lightThemeName = context.getString(R.string.preferences_app_theme_mode_entries_value_light);
        this.darkThemeName = context.getString(R.string.preferences_app_theme_mode_entries_value_dark);
    }

    public int getTheme(String themeName) {
        if (themeName.equals(lightThemeName)) {
            return R.style.LightAppTheme;
        } else if (themeName.equals(darkThemeName)) {
            return R.style.DarkAppTheme;
        } else {
            // default
            return R.style.LightAppTheme;
        }
    }
}
