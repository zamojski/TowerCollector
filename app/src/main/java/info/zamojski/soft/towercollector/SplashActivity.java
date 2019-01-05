/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import info.zamojski.soft.towercollector.analytics.IntentSource;
import info.zamojski.soft.towercollector.broadcast.ExternalBroadcastReceiver;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.tasks.DatabaseUpgradeTask;
import info.zamojski.soft.towercollector.utils.ApkUtils;
import timber.log.Timber;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {

    static final String SHORTCUT_ACTION = "shortcut_action";
    static final String COLLECTOR_TOGGLE_ACTION = "COLLECTOR_TOGGLE";
    static final String UPLOADER_TOGGLE_ACTION = "UPLOADER_TOGGLE";

    private boolean databaseUpgradeRunning = false;

    private HandlerThread asyncHandlerThread;
    private Handler asyncHandler;
    private Handler mainHandler;

    private TextView detailsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate(): Creating activity");
        // set fixed screen orientation
        if (!ApkUtils.isRunningOnBuggyOreoSetRequestedOrientation(this))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.splash);
        this.setFinishOnTouchOutside(false);
        // get UI controls
        detailsTextView = findViewById(R.id.splash_details_textview);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("onStart(): Starting splash screen");
        Intent startupIntent = getIntent();
        if (startupIntent != null) {
            String action = startupIntent.getStringExtra(SHORTCUT_ACTION);
            if (action != null) {
                switch (action) {
                    case COLLECTOR_TOGGLE_ACTION:
                        toggleCollectorAsync();
                        return;
                    case UPLOADER_TOGGLE_ACTION:
                        toggleUploaderAsync();
                        return;
                }
            }
        }
        // default
        startMainActivityAsync();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.d("onStop(): Stopping splash screen");
    }

    @Override
    public void onBackPressed() {
        Timber.d("onBackPressed(): Preventing close if db upgrade is running");
        if (databaseUpgradeRunning) {
            Toast.makeText(this, R.string.splash_toast_database_upgrade_running, Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    private void startMainActivityAsync() {
        Timber.d("startMainActivityAsync(): Starting main screen");
        getAsyncHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ensureDatabaseUpToDate();
                startMainActivity();
                Timber.d("startMainActivityAsync(): Closing splash screen window");
                finish();
            }
        }, 0);
    }

    private void toggleCollectorAsync() {
        Timber.d("toggleCollectorAsync(): Toggle collector");
        getAsyncHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ensureDatabaseUpToDate();
                if (MyApplication.isBackgroundTaskRunning(CollectorService.class)) {
                    new ExternalBroadcastReceiver().stopCollectorService(MyApplication.getApplication());
                } else {
                    new ExternalBroadcastReceiver().startCollectorService(MyApplication.getApplication(), IntentSource.Shortcut);
                }
                Timber.d("startCollectorAsync(): Closing splash screen window");
                finish();
            }
        }, 0);
    }

    private void toggleUploaderAsync() {
        Timber.d("toggleUploaderAsync(): Toggle uploader");
        getAsyncHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ensureDatabaseUpToDate();
                if (MyApplication.isBackgroundTaskRunning(UploaderService.class)) {
                    new ExternalBroadcastReceiver().stopUploaderService(MyApplication.getApplication());
                } else {
                    new ExternalBroadcastReceiver().startUploaderService(MyApplication.getApplication());
                }
                Timber.d("toggleUploaderAsync(): Closing splash screen window");
                finish();
            }
        }, 0);
    }

    private void ensureDatabaseUpToDate() {
        int currentDbVersion = MeasurementsDatabase.getDatabaseVersion(getApplication());
        if (currentDbVersion != MeasurementsDatabase.DATABASE_FILE_VERSION) {
            Timber.d("ensureDatabaseUpToDate(): Upgrading database");
            databaseUpgradeRunning = true;
            showDetailsMessage();
            // show progress dialog only when migrating database
            DatabaseUpgradeTask databaseMigrationTask = new DatabaseUpgradeTask(currentDbVersion);
            databaseMigrationTask.upgrade();
            MyApplication.getAnalytics().sendMigrationStarted();
            hideDetailsMessage();
            databaseUpgradeRunning = false;
        }
        // Load last measurement and stats into cache
        MeasurementsDatabase.getInstance(getApplication()).getLastMeasurement();
        MeasurementsDatabase.getInstance(getApplication()).getMeasurementsStatistics();
    }

    private void startMainActivity() {
        Timber.d("startMainActivity(): Starting main window");
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void showDetailsMessage() {
        setDetailsMessageVisibility(View.VISIBLE);
    }

    private void hideDetailsMessage() {
        setDetailsMessageVisibility(View.GONE);
    }

    private void setDetailsMessageVisibility(final int visibility) {
        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                detailsTextView.setVisibility(visibility);
            }
        });
    }

    private Handler getMainHandler() {
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    private Handler getAsyncHandler() {
        if (asyncHandlerThread == null) {
            asyncHandlerThread = new HandlerThread(SplashActivity.class.getSimpleName() + ".AsyncHandler");
            asyncHandlerThread.start();
        }
        if (asyncHandler == null) {
            asyncHandler = new Handler(asyncHandlerThread.getLooper());
        }
        return asyncHandler;
    }
}
