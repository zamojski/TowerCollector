/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

import info.zamojski.soft.towercollector.analytics.IntentSource;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.N)
public abstract class QuickSettingsTileServiceBase extends TileService {

    public void onStartListening(boolean isActive, @DrawableRes int startIcon, @DrawableRes int stopIcon, @StringRes int startLabel, @StringRes int stopLabel) {
        super.onStartListening();
        Timber.d("onStartListening(): Start to listen");
        Tile qsTile = getQsTile();
        if (qsTile == null) {
            Timber.d("onStartListening(): QS Tile is null");
            return;
        }

        Timber.d("onStartListening(): Updating tile to state %s", isActive);
        if (isActive) {
            qsTile.setLabel(getString(stopLabel));
            qsTile.setIcon(Icon.createWithResource(this, stopIcon));
            qsTile.setState(Tile.STATE_ACTIVE);
        } else {
            qsTile.setLabel(getString(startLabel));
            qsTile.setIcon(Icon.createWithResource(this, startIcon));
            qsTile.setState(Tile.STATE_INACTIVE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.setSubtitle(getString(R.string.app_name));
        }
        qsTile.updateTile();
    }

    public void onClick(String shortcutAction, boolean isActive) {
        super.onClick();
        Timber.d("onClick(): Toggling service status, currently %s", isActive);
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SplashActivity.SHORTCUT_ACTION, shortcutAction);
        intent.putExtra(SplashActivity.ACTION_SOURCE, IntentSource.QuickSettingsTile.name());
        startActivity(intent);
    }

    protected static void requestTileUpdate(Class<?> clazz, boolean isActive) {
        try {
            Timber.d("requestTileUpdate(): Requesting tile update to state %s", isActive);
            TileService.requestListeningState(MyApplication.getApplication(), new ComponentName(MyApplication.getApplication(), clazz));
        } catch (Exception ex) {
            Timber.w(ex, "requestTileUpdate(): Failed to process tile update request");
        }
    }
}
