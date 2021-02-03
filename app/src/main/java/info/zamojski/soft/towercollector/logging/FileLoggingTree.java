/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.logging;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.os.Process;

import androidx.annotation.NonNull;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import info.zamojski.soft.towercollector.utils.ApkUtils;
import info.zamojski.soft.towercollector.utils.FileUtils;
import timber.log.Timber;

public class FileLoggingTree extends Timber.DebugTree {

    private static final String TAG = FileLoggingTree.class.getSimpleName();

    private static final String[] LEVELS = new String[]{"disabled", "undefined", "V", "D", "I", "W", "E"};
    private static final int DISABLED = 0;
    private static final int FILE_EXISTENCE_CHECK_INTERVAL = 25;

    private final static SimpleDateFormat shortDateFormat = new SimpleDateFormat("MM-dd-HH:mm:ss.SSS", Locale.getDefault());

    private File logFile;
    private OutputStreamWriter osw;
    private boolean firstRun = true;
    private int priority = DISABLED;
    private int logCallsWithinInterval = 0;

    public final static FileLoggingTree INSTANCE = new FileLoggingTree();

    private FileLoggingTree() {
        // Avoid instantiation
    }

    public FileLoggingTree setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @SuppressLint("LogNotTimber")
    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        if (firstRun) {
            synchronized (FileLoggingTree.class) {
                if (firstRun) {
                    initialize();
                    firstRun = false;
                    // write identification data once initialization finished
                    log(Log.DEBUG, ApkUtils.getDeviceName());
                    log(Log.DEBUG, ApkUtils.getApkVersionNameWithSuffix());
                    log(Log.DEBUG, "Android version %s (%d)", Build.VERSION.RELEASE, Build.VERSION.SDK_INT);
                }
            }
        }
        // Skip if storage is unavailable or initialization failed
        if (osw == null) {
            return;
        }
        try {
            if (logCallsWithinInterval++ >= FILE_EXISTENCE_CHECK_INTERVAL) {
                logCallsWithinInterval = 0;
                if (!logFile.exists()) {
                    reinitialize();
                }
            }
            osw.write(String.format(Locale.ENGLISH, "%s %s/%s(% 5d): %s\r\n", shortDateFormat.format(new Date()), LEVELS[priority], tag, Process.myPid(), message));
            osw.flush();
        } catch (Exception ex) {
            Log.e(TAG, "Failed to write log file", ex);
            reinitialize();
        }
    }

    @Override
    protected boolean isLoggable(String tag, int priority) {
        return (this.priority > 0 && this.priority <= priority);
    }

    @SuppressLint("LogNotTimber")
    private void initialize() {
        try {
            File logDir = new File(Environment.getExternalStorageDirectory().getPath(), "TowerCollector");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            logFile = new File(logDir, FileUtils.getCurrentDateFileName("log"));
            FileOutputStream fis = new FileOutputStream(logFile);
            osw = new OutputStreamWriter(fis);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to open log file", ex);
        }
    }

    @SuppressLint("LogNotTimber")
    private void reinitialize() {
        try {
            Log.i(TAG, "Log file deleted, reinitializing");
            osw.close();
        } catch (Exception ex) {
            Log.e(TAG, "Failed to close log file", ex);
        }
        firstRun = true;
        initialize();
    }
}
