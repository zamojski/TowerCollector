/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import info.zamojski.soft.towercollector.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.os.Process;

public class Log {

    private static final String TAG = Log.class.getSimpleName();

    private static File logDir = new File(Environment.getExternalStorageDirectory().getPath(), "TowerCollector");
    private static File logFile = new File(logDir, FileUtils.getCurrentDateFilename("log"));
    private static OutputStreamWriter osw = null;

    private static SimpleDateFormat shortFormat = new SimpleDateFormat("MM-dd-HH:mm:ss.SSS", Locale.getDefault());

    private static Object lock = new Object();

    private static UncaughtExceptionHandler defaultHandler;

    static {
        // set exception handler
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                logCrash(TAG, "CRASHED", ex);
                defaultHandler.uncaughtException(thread, ex);
            }
        });
        try {
            if (!logDir.exists())
                logDir.mkdirs();
            FileOutputStream fis = new FileOutputStream(logFile);
            osw = new OutputStreamWriter(fis);
            // write identification data
            d(TAG, ApkUtils.getDeviceName());
            d(TAG, ApkUtils.getApkVersionName(MyApplication.getApplication()));
        } catch (Exception ex) {
            android.util.Log.e(TAG, "Failed to open log file!", ex);
        }
    }

    public static File getLogDir() {
        return logDir;
    }

    public static int d(String t, String m) {
        synchronized (lock) {
            writeLog("D", t, m);
        }
        return android.util.Log.d(t, m);
    }

    public static int wtf(String t, String m) {
        synchronized (lock) {
            writeLog("F", t, m);
        }
        return android.util.Log.wtf(t, m);
    }

    public static int wtf(String t, String m, Throwable tr) {
        synchronized (lock) {
            writeLog("F", t, m + "\r\n" + android.util.Log.getStackTraceString(tr));
        }
        return android.util.Log.wtf(t, m, tr);
    }

    public static int e(String t, String m, Throwable tr) {
        synchronized (lock) {
            writeLog("E", t, m + "\r\n" + android.util.Log.getStackTraceString(tr));
        }
        return android.util.Log.e(t, m, tr);
    }

    public static int e(String t, String m) {
        synchronized (lock) {
            writeLog("E", t, m);
        }
        return android.util.Log.e(t, m);
    }

    public static int i(String t, String m) {
        synchronized (lock) {
            writeLog("I", t, m);
        }
        return android.util.Log.i(t, m);
    }

    public static int w(String t, String m, Throwable tr) {
        synchronized (lock) {
            writeLog("W", t, m + "\r\n" + android.util.Log.getStackTraceString(tr));
        }
        return android.util.Log.w(t, m, tr);
    }

    public static int w(String t, String m) {
        synchronized (lock) {
            writeLog("W", t, m);
        }
        return android.util.Log.w(t, m);
    }

    public static int v(String t, String m) {
        synchronized (lock) {
            writeLog("V", t, m);
        }
        return android.util.Log.v(t, m);
    }

    private static void logCrash(String t, String m, Throwable tr) {
        synchronized (lock) {
            writeLog("E", t, m + "\r\n" + android.util.Log.getStackTraceString(tr));
        }
    }

    private static void writeLog(String type, String tag, String m) {
        writeLog(String.format(Locale.ENGLISH, "%s %s/%s(% 5d): %s\r\n", shortFormat.format(new Date()), type, tag, Process.myPid(), m));
        //writeLog(String.format("%s\t%s\t%s.%s\r\n", type, shortFormat.format(new Date()), tag, m));
    }

    private static void writeLog(String s) {
        try {
            //s = rot13(s);
            osw.write(s);
            osw.flush();
        } catch (Exception ex) {
            android.util.Log.e(TAG, "Cannot write custom log file", ex);
        }
    }

    private static String rot13(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'a' && c <= 'm')
                c += 13;
            else if (c >= 'A' && c <= 'M')
                c += 13;
            else if (c >= 'n' && c <= 'z')
                c -= 13;
            else if (c >= 'N' && c <= 'Z')
                c -= 13;
            else if (c >= '0' && c <= '4')
                c += 5;
            else if (c >= '5' && c <= '9')
                c -= 5;
            sb.append(c);
        }
        return sb.toString();
    }
}
