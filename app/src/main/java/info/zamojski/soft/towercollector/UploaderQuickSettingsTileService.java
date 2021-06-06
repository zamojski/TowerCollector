/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.N)
public class UploaderQuickSettingsTileService extends QuickSettingsTileServiceBase {

    private static boolean isActive = false;

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Timber.d("onTileAdded(): Tile added");
        requestTileUpdate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("onTileAdded(): Tile bound");
        requestTileUpdate();
        return super.onBind(intent);
    }

    @Override
    public void onStartListening() {
        super.onStartListening(isActive,
                R.drawable.ic_quicksettings_uploader_start, R.drawable.ic_quicksettings_uploader_stop,
                R.string.quicksettings_upload_start, R.string.quicksettings_upload_stop);
    }

    @Override
    public void onClick() {
        super.onClick(SplashActivity.UPLOADER_TOGGLE_ACTION, isActive);
    }

    public static void requestTileUpdate(boolean isActive) {
        UploaderQuickSettingsTileService.isActive = isActive;
        requestTileUpdate();
    }

    private static void requestTileUpdate() {
        requestTileUpdate(UploaderQuickSettingsTileService.class, isActive);
    }
}
