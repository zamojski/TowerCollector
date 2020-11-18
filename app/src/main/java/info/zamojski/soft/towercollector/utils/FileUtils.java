/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import info.zamojski.soft.towercollector.files.DeviceOperationException;
import timber.log.Timber;

public class FileUtils {

    public static String combinePath(File path1, String path2) {
        return new File(path1, path2).getPath();
    }

    public static File getExternalStorageAppDir() {
        return new File(Environment.getExternalStorageDirectory(), "TowerCollector");
    }

    public static String getCurrentDateFileName(String extension) {
        return getCurrentDateFileName(new Date(), "", extension);
    }

    public static String getCurrentDateFileName(Date dateTime, String nameSuffix, String extension) {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH).format(dateTime) + nameSuffix + "." + extension;
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

    public static String getFileMimeType(Context context, String... paths) {
        if (paths.length == 1)
            return context.getContentResolver().getType(Uri.parse(paths[0]));
        for (String path : paths) {
            String fileExtension = getFileExtension(path).toLowerCase();
            if ("gz".equals(fileExtension) || "zip".equals(fileExtension) || "kmz".equals(fileExtension))
                return "application/octet-stream";
        }
        return "text/*";
    }

    public static String changeExtension(String path, String newExtension) {
        String extension = getFileExtension(path);
        return TextUtils.substring(path, 0, path.length() - extension.length()) + newExtension;
    }

    public static boolean copyFile(File src, File dst) {
        FileChannel srcChannel = null, dstChannel = null;
        try {
            srcChannel = new FileInputStream(src).getChannel();
            dstChannel = new FileOutputStream(dst).getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
            return true;
        } catch (IOException ex) {
            Timber.e(ex, "copyFile(): Failed to copy file \"%s\" to \"%s\"", src, dst);
            return false;
        } finally {
            if (srcChannel != null) {
                try {
                    srcChannel.close();
                } catch (IOException ex) {
                    Timber.e(ex, "copyFile(): Failed to close source channel");
                }
            }
            if (dstChannel != null) {
                try {
                    dstChannel.close();
                } catch (IOException ex) {
                    Timber.e(ex, "copyFile(): Failed to close destination channel");
                }
            }
        }
    }

    public static void checkAccess(File file) throws DeviceOperationException {
        File dir = file.getParentFile();
        // check dirs
        if (!dir.exists() && !dir.mkdirs()) {
            throw new DeviceOperationException("Cannot create directory: " + dir.getAbsolutePath(), DeviceOperationException.Reason.LocationNotExists);
        }
        if (!dir.canWrite() && !dir.setWritable(true)) {
            throw new DeviceOperationException("Cannot make directory writable: " + dir.getAbsolutePath(), DeviceOperationException.Reason.LocationNotWritable);
        }
        // check file
        if (file.exists() && !file.canWrite() && !file.setWritable(true)) {
            throw new DeviceOperationException("Cannot make existing file writable: " + file.getAbsolutePath(), DeviceOperationException.Reason.DeviceNotWritable);
        }
    }
}
