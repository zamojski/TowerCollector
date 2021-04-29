/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FileUtils {

    private static final Map<String, String> ExtensionToMimeTypeMap = new HashMap<String, String>() {{
        put("csv", "text/csv");
        put("json", "application/json");
        put("gpx", "application/gpx+xml");
        put("kml", "application/vnd.google-earth.kml+xml");
        put("kmz", "application/vnd.google-earth.kmz");
        put("zip", "application/zip");
        put("gz", "application/gzip");
        put("xml", "application/xml");
        put("db", "application/x-sqlite3");
        put("*", "application/octet-stream");
    }};

    public static File getCacheDir(Context context) {
        String externalState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(externalState)) {
            return context.getExternalCacheDir();
        }
        return context.getCacheDir();
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

        String extension = "";

        int i = path.lastIndexOf('.');
        int p = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));

        if (i > p) {
            extension = path.substring(i + 1);
        }
        return extension;
    }

    public static String getFileMimeType(String... paths) {
        if (paths.length == 0)
            throw new IllegalArgumentException("Provide at least one path.");
        if (paths.length == 1)
            return getFileMimeType(paths[0]);
        for (String path : paths) {
            String fileExtension = getFileExtension(path).toLowerCase();
            if ("gz".equals(fileExtension) || "zip".equals(fileExtension) || "kmz".equals(fileExtension))
                return "application/octet-stream";
        }
        return "text/*";
    }

    private static String getFileMimeType(String path) {
        String extension = getFileExtension(path).toLowerCase();
        String mimeType = ExtensionToMimeTypeMap.get(extension);
        return mimeType != null ? mimeType : ExtensionToMimeTypeMap.get("*");
    }
}
