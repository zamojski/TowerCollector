/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.controls.ISwipingController;
import info.zamojski.soft.towercollector.utils.PermissionUtils;

import android.Manifest;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.jetbrains.annotations.NotNull;

public class MainActivityPagerAdapter extends FragmentStatePagerAdapter implements ISwipingController {

    private static final int MainLastFragmentIndex = 0;
    private static final int MainStatsFragmentIndex = 1;
    private static final int MainMapFragmentIndex = 2;

    private boolean wasMapFullyConfiguredRecently = false;

    private final Context context;

    public MainActivityPagerAdapter(FragmentManager fm, Context context) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case MainLastFragmentIndex:
                return new MainLastFragment();
            case MainStatsFragmentIndex:
                return new MainStatsFragment();
            case MainMapFragmentIndex:
                if (isMapFullyConfigured()) {
                    return new MainMapFragment();
                }
                return new MainMapConfigureFragment();
            default:
                throw new UnsupportedOperationException("Cannot find view at position " + position);
        }
    }

    private boolean isMapFullyConfigured() {
        return wasMapFullyConfiguredRecently = (MyApplication.getPreferencesProvider().isMainMapConfigured()
                && PermissionUtils.hasPermissions(MyApplication.getApplication(), Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    @Override
    public int getCount() {
        return 2 + (MyApplication.getPreferencesProvider().isMainMapEnabled() ? 1 : 0);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case MainLastFragmentIndex:
                return context.getString(R.string.main_tab_last_title);
            case MainStatsFragmentIndex:
                return context.getString(R.string.main_tab_stats_title);
            case MainMapFragmentIndex:
                return context.getString(R.string.main_tab_map_title);
            default:
                throw new UnsupportedOperationException("Cannot find view title at position " + position);
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        // reset position when invalidating
        return POSITION_NONE;
    }

    public boolean shouldEnableSwiping(int position) {
        return !(position == MainMapFragmentIndex && wasMapFullyConfiguredRecently) || position >= getCount();
    }
}
