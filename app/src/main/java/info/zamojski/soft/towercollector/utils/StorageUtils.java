/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.os.Environment;

public class StorageUtils {

    public static boolean isExternalMemoryPresent() {
        String externalStorageState = Environment.getExternalStorageState();
        return externalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    public static boolean isExternalMemoryWritable() {
        String externalStorageState = Environment.getExternalStorageState();
        return externalStorageState.equals(Environment.MEDIA_MOUNTED);
    }
}
