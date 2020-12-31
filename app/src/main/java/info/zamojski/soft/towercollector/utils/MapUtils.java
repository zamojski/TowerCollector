/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.SqlTileWriter;

import java.io.File;

import info.zamojski.soft.towercollector.R;

public class MapUtils {
    public static void configureMap(Context context) {
        Configuration.getInstance().setOsmdroidBasePath(getMapBasePath(context));
        Configuration.getInstance().setOsmdroidTileCache(getMapCachePath(context));
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setTileFileSystemCacheMaxBytes(150 * 1024 * 1024);
        Configuration.getInstance().setTileFileSystemCacheTrimBytes(100 * 1024 * 1024);
    }

    public static void clearMapCache(Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SqlTileWriter sqlTileWriter = new SqlTileWriter();
                boolean success = sqlTileWriter.purgeCache();
                sqlTileWriter.onDetach();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, success ? R.string.clear_map_cache_finished : R.string.clear_map_cache_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private static File getMapBasePath(Context context) {
        return new File(FileUtils.getCacheDir(context), "MapData");
    }

    private static File getMapCachePath(Context context) {
        return new File(getMapBasePath(context), "Cache");
    }
}
