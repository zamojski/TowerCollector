/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.tasks.DatabaseUpgradeTask;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import trikita.log.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private DatabaseUpgradeTask databaseMigrationTask;

    private boolean databaseUpgradeRunning = false;

    private HandlerThread asyncHandlerThread;
    private Handler asyncHandler;
    private Handler mainHandler;

    private TextView detailsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(): Creating activity");
        // set fixed screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.splash);
        this.setFinishOnTouchOutside(false);
        // get UI controls
        detailsTextView = (TextView) findViewById(R.id.splash_details_textview);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): Starting splash screen");
        startAsync();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(): Stopping splash screen");
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed(): Preventing close if db upgrade is running");
        if (databaseUpgradeRunning) {
            Toast.makeText(this, R.string.splash_toast_database_upgrade_running, Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    private void startAsync() {
        Log.d(TAG, "startAsync(): Creating handler");
        getAsyncHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ensureDatabaseUpToDate();
                startMainActivity();
                Log.d(TAG, "startAsync(): Closing splash screen window");
                finish();
            }
        }, 0);
    }

    private void ensureDatabaseUpToDate() {
        int currentDbVersion = MeasurementsDatabase.getDatabaseVersion(getApplication());
        if (currentDbVersion != MeasurementsDatabase.DATABASE_FILE_VERSION) {
            Log.d(TAG, "ensureDatabaseUpToDate(): Upgrading database");
            databaseUpgradeRunning = true;
            showDetailsMessage();
            // show progress dialog only when migrating database
            databaseMigrationTask = new DatabaseUpgradeTask(currentDbVersion);
            databaseMigrationTask.upgrade();
            MyApplication.getAnalytics().sendMigrationStarted();
            hideDetailsMessage();
            databaseUpgradeRunning = false;
        }
    }

    private void startMainActivity() {
        Log.d(TAG, "startMainActivity(): Starting main window");
        Intent mainActivityIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(mainActivityIntent);
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
            asyncHandlerThread = new HandlerThread(TAG + ".AsyncHandler");
            asyncHandlerThread.start();
        }
        if (asyncHandler == null) {
            asyncHandler = new Handler(asyncHandlerThread.getLooper());
        }
        return asyncHandler;
    }
}
