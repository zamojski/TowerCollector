/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.os.Process;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.utils.ApkUtils;
import info.zamojski.soft.towercollector.utils.FileUtils;
import trikita.log.Log;

public class AndroidFilePrinter implements Log.Printer {

    private static final String TAG = AndroidFilePrinter.class.getSimpleName();

    private final static String[] LEVELS = new String[]{"V", "D", "I", "W", "E"};

    private final static SimpleDateFormat shortFormat = new SimpleDateFormat("MM-dd-HH:mm:ss.SSS", Locale.getDefault());

    private OutputStreamWriter osw;

    private boolean firstRun = true;

    private static AndroidFilePrinter instance = new AndroidFilePrinter();

    private AndroidFilePrinter() {
        // Singleton
    }

    public static AndroidFilePrinter getInstance() {
        return instance;
    }

    @Override
    public void print(int level, String tag, String msg) {
        if (firstRun) {
            Class clazz = AndroidFilePrinter.class;
            synchronized (clazz) {
                if (firstRun) {
                    initialize();
                    firstRun = false;
                    // write identification data once initialization finished
                    print(Log.D, TAG, ApkUtils.getDeviceName());
                    print(Log.D, TAG, ApkUtils.getApkVersionName(MyApplication.getApplication()));
                }
            }
        }
        // Skip if storage is unavailable or initialization failed
        if (osw == null) {
            return;
        }
        try {
            osw.write(String.format(Locale.ENGLISH, "%s %s/%s(% 5d): %s\r\n", shortFormat.format(new Date()), LEVELS[level], tag, Process.myPid(), msg));
            osw.flush();
        } catch (Exception ex) {
            android.util.Log.e("Failed to write log file!", ex);
        }
    }

    private void initialize() {
        try {
            File logDir = new File(Environment.getExternalStorageDirectory().getPath(), "TowerCollector");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            File logFile = new File(logDir, FileUtils.getCurrentDateFilename("log"));
            FileOutputStream fis = new FileOutputStream(logFile);
            osw = new OutputStreamWriter(fis);
        } catch (Exception ex) {
            android.util.Log.e("Failed to open log file!", ex);
        }
    }
}
