/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;

import trikita.log.Log;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    public static String combinePath(String path1, String path2) {
        return new File(new File(path1), path2).getPath();
    }

    public static String combinePath(File path1, String path2) {
        return new File(path1, path2).getPath();
    }

    public static File getExternalStorageAppDir() {
        return new File(Environment.getExternalStorageDirectory(), "TowerCollector");
    }

    public static String getCurrentDateFilename(String extension) {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH).format(new Date()) + "." + extension;
    }

    public static String getFileExtension(File file) {
        return getFileExtension(file.getPath());
    }

    public static String getFileExtension(String path) {
        // 'archive.tar.gz' -> 'gz'
        // '/path/to.a/file' -> ''
        // '/root/case/g.txt' -> 'txt'
        // '/root/case/g.txt.gg' -> 'gg'
        // '/root/case/g.txt.gg/' -> ''
        // '.htaccess' -> 'htaccess'
        // '/.htaccess' -> 'htaccess'
        // '/s/.htaccess' -> 'htaccess'

        String extension = null;

        int i = path.lastIndexOf('.');
        int p = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));

        if (i > p) {
            extension = path.substring(i + 1);
        }
        return extension;
    }

    public static boolean copyFile(File src, File dst) {
        FileChannel srcChannel = null, dstChannel = null;
        try {
            srcChannel = new FileInputStream(src).getChannel();
            dstChannel = new FileOutputStream(dst).getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
            return true;
        } catch (IOException ex) {
            Log.e(TAG, "copyFile(): Failed to copy file \"" + src + "\" to \"" + dst + "\"", ex);
            return false;
        } finally {
            if (srcChannel != null) {
                try {
                    srcChannel.close();
                } catch (IOException ex) {
                    Log.e(TAG, "copyFile(): Failed to close source channel", ex);
                }
            }
            if (dstChannel != null) {
                try {
                    dstChannel.close();
                } catch (IOException ex) {
                    Log.e(TAG, "copyFile(): Failed to close destination channel", ex);
                }
            }
        }
    }
}
