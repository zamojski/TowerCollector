/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import info.zamojski.soft.towercollector.analytics.IntentSource;
import info.zamojski.soft.towercollector.broadcast.ExternalBroadcastReceiver;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.export.ExportWorker;
import info.zamojski.soft.towercollector.providers.preferences.PreferencesProvider;
import info.zamojski.soft.towercollector.tasks.DatabaseUpgradeTask;
import info.zamojski.soft.towercollector.tasks.PreferencesUpgradeTask;
import info.zamojski.soft.towercollector.uploader.UploaderWorker;
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

import org.acra.ACRA;

public class SplashActivity extends Activity {

    public static final String SHORTCUT_ACTION = "shortcut_action";
    public static final String ACTION_SOURCE = "action_source";
    public static final String COLLECTOR_TOGGLE_ACTION = "COLLECTOR_TOGGLE";
    public static final String UPLOADER_TOGGLE_ACTION = "UPLOADER_TOGGLE";
    public static final String EXPORT_TOGGLE_ACTION = "EXPORT_TOGGLE";

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
                IntentSource actionSource = getActionSourceOrDefault(startupIntent, IntentSource.Shortcut);
                switch (action) {
                    case COLLECTOR_TOGGLE_ACTION:
                        ACRA.getErrorReporter().putCustomData("STARTUP_INTENT", startupIntent.toString());
                        toggleCollectorAsync(actionSource);
                        return;
                    case UPLOADER_TOGGLE_ACTION:
                        toggleUploaderAsync(actionSource);
                        return;
                    case EXPORT_TOGGLE_ACTION:
                        toggleExportAsync(actionSource);
                        return;
                }
            }
        }
        // default
        startMainActivityAsync();
    }

    private IntentSource getActionSourceOrDefault(Intent intent, IntentSource source) {
        if (intent.hasExtra(ACTION_SOURCE)) {
            try {
                String actionSourceName = intent.getStringExtra(ACTION_SOURCE);
                return IntentSource.valueOf(actionSourceName);
            } catch (Exception ex) {
                Timber.w(ex, "onStart(): Failed to parse action source");
            }
        }
        return source;
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
                ensurePreferencesUpToDate();
                ensureDatabaseUpToDate();
                startMainActivity();
                Timber.d("startMainActivityAsync(): Closing splash screen window");
                finish();
            }
        }, 0);
    }

    private void toggleCollectorAsync(IntentSource source) {
        Timber.d("toggleCollectorAsync(): Toggle collector from %s", source);
        getAsyncHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ensurePreferencesUpToDate();
                ensureDatabaseUpToDate();
                if (MyApplication.isBackgroundTaskRunning(CollectorService.class)) {
                    new ExternalBroadcastReceiver().stopCollectorService(MyApplication.getApplication());
                } else {
                    new ExternalBroadcastReceiver().startCollectorServiceFromForeground(MyApplication.getApplication(), source);
                }
                Timber.d("startCollectorAsync(): Closing splash screen window");
                finish();
            }
        }, 0);
    }

    private void toggleUploaderAsync(IntentSource source) {
        Timber.d("toggleUploaderAsync(): Toggle uploader from %s", source);
        getAsyncHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ensurePreferencesUpToDate();
                ensureDatabaseUpToDate();
                if (MyApplication.isBackgroundTaskRunning(UploaderWorker.class)) {
                    new ExternalBroadcastReceiver().stopUploaderWorker(MyApplication.getApplication());
                } else {
                    new ExternalBroadcastReceiver().startUploaderWorker(MyApplication.getApplication(), source);
                }
                Timber.d("toggleUploaderAsync(): Closing splash screen window");
                finish();
            }
        }, 0);
    }

    private void toggleExportAsync(IntentSource source) {
        Timber.d("toggleExportAsync(): Toggle export from %s", source);
        getAsyncHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ensurePreferencesUpToDate();
                ensureDatabaseUpToDate();
                if (MyApplication.isBackgroundTaskRunning(ExportWorker.class)) {
                    new ExternalBroadcastReceiver().stopExportWorker(MyApplication.getApplication());
                } else {
                    new ExternalBroadcastReceiver().startExportWorker(MyApplication.getApplication(), source);
                }
                Timber.d("toggleExportAsync(): Closing splash screen window");
                finish();
            }
        }, 0);
    }

    private void ensureDatabaseUpToDate() {
        int currentDbVersion = MeasurementsDatabase.getDatabaseVersion(MyApplication.getApplication());
        if (currentDbVersion != MeasurementsDatabase.DATABASE_FILE_VERSION) {
            Timber.d("ensureDatabaseUpToDate(): Upgrading database");
            databaseUpgradeRunning = true;
            showDetailsMessage();
            // show progress dialog only when migrating database
            DatabaseUpgradeTask databaseMigrationTask = new DatabaseUpgradeTask(this, currentDbVersion);
            databaseMigrationTask.upgrade();
            hideDetailsMessage();
            databaseUpgradeRunning = false;
        }
        // Load last measurement and stats into cache
        MeasurementsDatabase.getInstance(MyApplication.getApplication()).getLastMeasurement();
        MeasurementsDatabase.getInstance(MyApplication.getApplication()).getMeasurementsStatistics();
    }

    private void ensurePreferencesUpToDate() {
        int currentPreferencesVersion = MyApplication.getPreferencesProvider().getPreferencesVersion();
        if (currentPreferencesVersion != PreferencesProvider.PREFERENCES_VERSION) {
            Timber.d("ensurePreferencesUpToDate(): Upgrading preferences");
            databaseUpgradeRunning = true; // reuse intentionally as it should be fast and is executed in series
            showDetailsMessage();
            // show progress dialog only when migrating preferences
            PreferencesUpgradeTask preferencesUpgradeTask = new PreferencesUpgradeTask(MyApplication.getApplication(), currentPreferencesVersion);
            preferencesUpgradeTask.upgrade();
            hideDetailsMessage();
            databaseUpgradeRunning = false;
        }
    }

    private void startMainActivity() {
        Timber.d("startMainActivity(): Starting main window");
        Intent intent = new Intent(this, MainActivity.class);
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
