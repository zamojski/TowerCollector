/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.common.util.concurrent.ListenableFuture;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import info.zamojski.soft.towercollector.analytics.IntentSource;
import info.zamojski.soft.towercollector.broadcast.AirplaneModeBroadcastReceiver;
import info.zamojski.soft.towercollector.broadcast.BatterySaverBroadcastReceiver;
import info.zamojski.soft.towercollector.controls.DialogManager;
import info.zamojski.soft.towercollector.controls.NonSwipeableViewPager;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.enums.ExportAction;
import info.zamojski.soft.towercollector.enums.FileType;
import info.zamojski.soft.towercollector.enums.MeansOfTransport;
import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.enums.Validity;
import info.zamojski.soft.towercollector.events.AirplaneModeChangedEvent;
import info.zamojski.soft.towercollector.events.BatteryOptimizationsChangedEvent;
import info.zamojski.soft.towercollector.events.CollectorStartedEvent;
import info.zamojski.soft.towercollector.events.GpsStatusChangedEvent;
import info.zamojski.soft.towercollector.events.MapEnabledChangedEvent;
import info.zamojski.soft.towercollector.events.PowerSaveModeChangedEvent;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.events.SystemTimeChangedEvent;
import info.zamojski.soft.towercollector.export.ExportProgressDialogFragment;
import info.zamojski.soft.towercollector.export.ExportWorker;
import info.zamojski.soft.towercollector.model.ChangelogInfo;
import info.zamojski.soft.towercollector.model.UpdateInfo;
import info.zamojski.soft.towercollector.model.UpdateInfo.DownloadLink;
import info.zamojski.soft.towercollector.providers.ChangelogProvider;
import info.zamojski.soft.towercollector.providers.HtmlChangelogFormatter;
import info.zamojski.soft.towercollector.providers.preferences.PreferencesProvider;
import info.zamojski.soft.towercollector.tasks.UpdateCheckAsyncTask;
import info.zamojski.soft.towercollector.uploader.UploaderProgressDialogFragment;
import info.zamojski.soft.towercollector.uploader.UploaderWorker;
import info.zamojski.soft.towercollector.utils.ApkUtils;
import info.zamojski.soft.towercollector.utils.BackgroundTaskHelper;
import info.zamojski.soft.towercollector.utils.BatteryUtils;
import info.zamojski.soft.towercollector.utils.DateUtils;
import info.zamojski.soft.towercollector.utils.FileUtils;
import info.zamojski.soft.towercollector.utils.GpsUtils;
import info.zamojski.soft.towercollector.utils.MapUtils;
import info.zamojski.soft.towercollector.utils.NetworkUtils;
import info.zamojski.soft.towercollector.utils.OpenCellIdUtils;
import info.zamojski.soft.towercollector.utils.PermissionUtils;
import info.zamojski.soft.towercollector.utils.StorageUtils;
import info.zamojski.soft.towercollector.utils.StringUtils;
import info.zamojski.soft.towercollector.utils.UpdateDialogArrayAdapter;
import info.zamojski.soft.towercollector.views.MainActivityPagerAdapter;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

@RuntimePermissions
public class MainActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener, ExportProgressDialogFragment.OnExportCancelledListener, UploaderProgressDialogFragment.OnUploaderCancelledListener {

    private static final int BATTERY_OPTIMIZATIONS_ACTIVITY_RESULT = 'B';
    private static final int BATTERY_SAVER_ACTIVITY_RESULT = 'S';
    private static final int AIRPLANE_MODE_ACTIVITY_RESULT = 'A';

    private AtomicBoolean isCollectorServiceRunning = new AtomicBoolean(false);
    private boolean isGpsEnabled = false;
    private boolean showAskForLocationSettingsDialog = false;
    private boolean showNotCompatibleDialog = true;

    private BroadcastReceiver airplaneModeBroadcastReceiver = new AirplaneModeBroadcastReceiver();
    private BroadcastReceiver batterySaverBroadcastReceiver = new BatterySaverBroadcastReceiver();

    private ExportProgressDialogFragment exportProgressDialog;
    private String exportedDirAbsolutePath;
    private String[] exportedFilePaths;

    private UploaderProgressDialogFragment uploaderProgressDialog;

    private Boolean canStartNetworkTypeSystemActivityResult = null;

    private Menu mainMenu;
    private MenuItem startMenu;
    private MenuItem stopMenu;
    private MenuItem networkTypeMenu;

    private TabLayout tabLayout;

    public ICollectorService collectorServiceBinder;

    private BackgroundTaskHelper backgroundTaskHelper;

    private NonSwipeableViewPager viewPager;

    private View activityView;

    private boolean isFirstStart = true;

    // ========== ACTIVITY ========== //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MyApplication.getCurrentAppTheme());
        super.onCreate(savedInstanceState);
        Timber.d("onCreate(): Creating activity");
        // set fixed screen orientation
        if (!ApkUtils.isRunningOnBuggyOreoSetRequestedOrientation(this))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);
        activityView = findViewById(R.id.main_root);
        //setup toolbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setPopupTheme(MyApplication.getCurrentPopupTheme());
        setSupportActionBar(toolbar);
        // setup tabbed layout
        MainActivityPagerAdapter pageAdapter = new MainActivityPagerAdapter(getSupportFragmentManager(), getApplication());
        viewPager = findViewById(R.id.main_pager);
        viewPager.setAdapter(pageAdapter);
        tabLayout = findViewById(R.id.main_tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(this);

        backgroundTaskHelper = new BackgroundTaskHelper(this);

        displayNotCompatibleDialog();

        // show latest developer's messages
        displayDevelopersMessages();

        // show introduction
        displayIntroduction();

        processOnStartIntent(getIntent());

        // check for availability of new version
        checkForNewVersionAvailability();

        registerReceiver(airplaneModeBroadcastReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        registerReceiver(batterySaverBroadcastReceiver, new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy(): Unbinding from service");
        if (isCollectorServiceRunning.get())
            unbindService(collectorServiceConnection);
        tabLayout.removeOnTabSelectedListener(this);

        if (airplaneModeBroadcastReceiver != null)
            unregisterReceiver(airplaneModeBroadcastReceiver);
        if (batterySaverBroadcastReceiver != null)
            unregisterReceiver(batterySaverBroadcastReceiver);

        if (exportProgressDialog != null && exportProgressDialog.isVisible()) {
            exportProgressDialog.dismiss();
            exportProgressDialog = null;
        }

        if (uploaderProgressDialog != null && uploaderProgressDialog.isVisible()) {
            uploaderProgressDialog.dismiss();
            uploaderProgressDialog = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("onStart(): Binding to service");
        isCollectorServiceRunning.set(MyApplication.isBackgroundTaskRunning(CollectorService.class));
        if (isCollectorServiceRunning.get()) {
            bindService(new Intent(this, CollectorService.class), collectorServiceConnection, 0);
        }
        EventBus.getDefault().register(this);

        String appThemeName = MyApplication.getPreferencesProvider().getAppTheme();
        MyApplication.getAnalytics().sendPrefsAppTheme(appThemeName);

        if (isFirstStart && MyApplication.getPreferencesProvider().getStartCollectorAtStartup()) {
            isFirstStart = false;
            startCollectorServiceWithCheck();
        }

        restoreExportProgress();
        restoreUploaderProgress();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume(): Resuming");
        // print on UI
        EventBus.getDefault().post(new PrintMainWindowEvent());
        // restore recent tab
        int recentTabIndex = MyApplication.getPreferencesProvider().getMainWindowRecentTab();
        viewPager.setCurrentItem(recentTabIndex);
        // if coming back from Android settings re-run the action
        if (showAskForLocationSettingsDialog) {
            startCollectorServiceWithCheck();
        }
        // if keep on is checked
        if (MyApplication.getPreferencesProvider().getMainKeepScreenOnMode()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("onPause(): Pausing");
        // remember current tab
        int currentTabIndex = viewPager.getCurrentItem();
        MyApplication.getPreferencesProvider().setMainWindowRecentTab(currentTabIndex);
    }

    // ========== MENU ========== //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Timber.d("onCreateOptionsMenu(): Loading action bar");
        getMenuInflater().inflate(R.menu.main, menu);
        // save references
        startMenu = menu.findItem(R.id.main_menu_start);
        stopMenu = menu.findItem(R.id.main_menu_stop);
        networkTypeMenu = menu.findItem(R.id.main_menu_network_type);
        mainMenu = menu;// store the menu in an local variable for hardware key
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isRunning = isCollectorServiceRunning.get();
        Timber.d("onPrepareOptionsMenu(): Preparing action bar menu for running = %s", isRunning);
        // toggle visibility
        startMenu.setVisible(!isRunning);
        stopMenu.setVisible(isRunning);
        boolean networkTypeAvailable = canStartNetworkTypeSystemActivity();
        networkTypeMenu.setVisible(networkTypeAvailable);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // start action
        int itemId = item.getItemId();
        if (itemId == R.id.main_menu_start) {
            startCollectorServiceWithCheck();
            return true;
        } else if (itemId == R.id.main_menu_stop) {
            stopCollectorService();
            return true;
        } else if (itemId == R.id.main_menu_upload) {
            startUploaderTaskWithCheck();
            return true;
        } else if (itemId == R.id.main_menu_export) {
            startExportTaskWithCheck();
            return true;
        } else if (itemId == R.id.main_menu_clear) {
            startCleanup();
            return true;
        } else if (itemId == R.id.main_menu_preferences) {
            startPreferencesActivity();
            return true;
        } else if (itemId == R.id.main_menu_network_type) {
            startNetworkTypeSystemActivity();
            return true;
        } else if (itemId == R.id.main_menu_donate) {
            startDonateWebBrowserActivity();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BATTERY_OPTIMIZATIONS_ACTIVITY_RESULT) {
            // don't check resultCode because it's always 0 (user needs to manually navigate back)
            boolean batteryOptimizationsEnabled = BatteryUtils.areBatteryOptimizationsEnabled(MyApplication.getApplication());
            EventBus.getDefault().postSticky(new BatteryOptimizationsChangedEvent(batteryOptimizationsEnabled));
        } else if (requestCode == BATTERY_SAVER_ACTIVITY_RESULT) {
            // don't check resultCode because it's always 0 (user needs to manually navigate back)
            boolean powerSaveModeEnabled = BatteryUtils.isPowerSaveModeEnabled(MyApplication.getApplication());
            EventBus.getDefault().postSticky(new PowerSaveModeChangedEvent(powerSaveModeEnabled));
        } else if (requestCode == AIRPLANE_MODE_ACTIVITY_RESULT) {
            // don't check resultCode because it's always 0 (user needs to manually navigate back)
            boolean airplaneModeEnabled = NetworkUtils.isInAirplaneMode(MyApplication.getApplication());
            EventBus.getDefault().postSticky(new AirplaneModeChangedEvent(airplaneModeEnabled));
        } else if (requestCode == StorageUtils.OPEN_DOCUMENT_ACTIVITY_RESULT) {
            StorageUtils.persistStorageUri(this, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.d("onNewIntent(): New intent received: %s", intent);
        processOnStartIntent(intent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (event.getAction() == KeyEvent.ACTION_UP && mainMenu != null) {
                Timber.i("onKeyUp(): Hardware menu key pressed");
                mainMenu.performIdentifierAction(R.id.main_menu_more, 0);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onTabSelected(Tab tab) {
        Timber.d("onTabSelected() Switching to tab %s", tab.getPosition());
        // switch to page when tab is selected
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab) {
        // nothing
    }

    @Override
    public void onTabReselected(Tab tab) {
        // nothing
    }

    // ========== UI ========== //

    private void printInvalidSystemTime(ICollectorService collectorServiceBinder) {
        Validity valid = collectorServiceBinder.isSystemTimeValid();
        EventBus.getDefault().postSticky(new SystemTimeChangedEvent(valid));
    }

    private void hideInvalidSystemTime() {
        EventBus.getDefault().postSticky(new SystemTimeChangedEvent(Validity.Valid));
    }

    private void refreshTabs() {
        viewPager.getAdapter().notifyDataSetChanged();
    }

    // have to be public to prevent Force Close
    public void displayHelpOnClick(View view) {
        int titleId = View.NO_ID;
        int messageId = View.NO_ID;
        int additionalMessageId = View.NO_ID;
        int viewId = view.getId();
        if (viewId == R.id.main_gps_status_tablerow) {
            titleId = R.string.main_help_gps_status_title;
            messageId = R.string.main_help_gps_status_description;
        } else if (viewId == R.id.main_invalid_system_time_tablerow) {
            titleId = R.string.main_help_invalid_system_time_title;
            messageId = R.string.main_help_invalid_system_time_description;
        } else if (viewId == R.id.main_stats_today_locations_tablerow) {
            titleId = R.string.main_help_today_locations_title;
            messageId = R.string.main_help_today_locations_description;
        } else if (viewId == R.id.main_stats_today_cells_tablerow) {
            titleId = R.string.main_help_today_cells_title;
            messageId = R.string.main_help_today_cells_description;
        } else if (viewId == R.id.main_stats_local_locations_tablerow) {
            titleId = R.string.main_help_local_locations_title;
            messageId = R.string.main_help_local_locations_description;
        } else if (viewId == R.id.main_stats_local_cells_tablerow) {
            titleId = R.string.main_help_local_cells_title;
            messageId = R.string.main_help_local_cells_description;
        } else if (viewId == R.id.main_stats_global_locations_tablerow) {
            titleId = R.string.main_help_global_locations_title;
            messageId = R.string.main_help_global_locations_description;
        } else if (viewId == R.id.main_stats_global_cells_tablerow) {
            titleId = R.string.main_help_global_cells_title;
            messageId = R.string.main_help_global_cells_description;
        } else if (viewId == R.id.main_last_number_of_cells_tablerow) {
            titleId = R.string.mail_help_last_number_of_cells_title;
            messageId = R.string.mail_help_last_number_of_cells_description;
        } else if (viewId == R.id.main_last_network_type_tablerow1 || viewId == R.id.main_last_network_type_tablerow2) {
            titleId = R.string.main_help_last_network_type_title;
            messageId = R.string.main_help_last_network_type_description;
        } else if (viewId == R.id.main_last_long_cell_id_tablerow1 || viewId == R.id.main_last_long_cell_id_tablerow2) {
            titleId = R.string.main_help_last_long_cell_id_title;
            messageId = R.string.main_help_last_long_cell_id_description;
            NetworkGroup tag = (NetworkGroup) view.getTag();
            if (tag == NetworkGroup.Lte) {
                additionalMessageId = R.string.main_help_last_long_cell_id_description_lte;
            } else if (tag == NetworkGroup.Wcdma) {
                additionalMessageId = R.string.main_help_last_long_cell_id_description_umts;
            }
        } else if (viewId == R.id.main_last_cell_id_rnc_tablerow1 || viewId == R.id.main_last_cell_id_rnc_tablerow2) {
            titleId = R.string.main_help_last_cell_id_rnc_title;
            messageId = R.string.main_help_last_cell_id_rnc_description;
        } else if (viewId == R.id.main_last_cell_id_tablerow1 || viewId == R.id.main_last_cell_id_tablerow2) {
            titleId = R.string.main_help_last_cell_id_title;
            messageId = R.string.main_help_last_cell_id_description;
            NetworkGroup tag = (NetworkGroup) view.getTag();
            if (tag == NetworkGroup.Cdma) {
                titleId = R.string.main_help_last_bid_title;
            }
        } else if (viewId == R.id.main_last_lac_tablerow1 || viewId == R.id.main_last_lac_tablerow2) {
            titleId = R.string.main_help_last_lac_title;
            messageId = R.string.main_help_last_lac_description;
            NetworkGroup tag = (NetworkGroup) view.getTag();
            if (tag == NetworkGroup.Lte || tag == NetworkGroup.Nr) {
                titleId = R.string.main_help_last_tac_title;
            } else if (tag == NetworkGroup.Cdma) {
                titleId = R.string.main_help_last_nid_title;
                messageId = R.string.main_help_last_nid_description;
            }
        } else if (viewId == R.id.main_last_mcc_tablerow1 || viewId == R.id.main_last_mcc_tablerow2) {
            titleId = R.string.main_help_last_mcc_title;
            messageId = R.string.main_help_last_mcc_description;
        } else if (viewId == R.id.main_last_mnc_tablerow1 || viewId == R.id.main_last_mnc_tablerow2) {
            titleId = R.string.main_help_last_mnc_title;
            messageId = R.string.main_help_last_mnc_description;
            NetworkGroup tag = (NetworkGroup) view.getTag();
            if (tag == NetworkGroup.Cdma) {
                titleId = R.string.main_help_last_sid_title;
                messageId = R.string.main_help_last_sid_description;
            }
        } else if (viewId == R.id.main_last_signal_strength_tablerow1 || viewId == R.id.main_last_signal_strength_tablerow2) {
            titleId = R.string.main_help_last_signal_strength_title;
            messageId = R.string.main_help_last_signal_strength_description;
        } else if (viewId == R.id.main_last_latitude_tablerow) {
            titleId = R.string.main_help_last_latitude_title;
            messageId = R.string.main_help_last_latitude_description;
        } else if (viewId == R.id.main_last_longitude_tablerow) {
            titleId = R.string.main_help_last_longitude_title;
            messageId = R.string.main_help_last_longitude_description;
        } else if (viewId == R.id.main_last_gps_accuracy_tablerow) {
            titleId = R.string.main_help_last_gps_accuracy_title;
            messageId = R.string.main_help_last_gps_accuracy_description;
        } else if (viewId == R.id.main_last_date_time_tablerow) {
            titleId = R.string.main_help_last_date_time_title;
            messageId = R.string.main_help_last_date_time_description;
        } else if (viewId == R.id.main_stats_to_upload_ocid_locations_tablerow) {
            titleId = R.string.main_help_to_upload_common_locations_title;
            messageId = R.string.main_help_to_upload_ocid_locations_description;
        } else if (viewId == R.id.main_stats_to_upload_mls_locations_tablerow) {
            titleId = R.string.main_help_to_upload_common_locations_title;
            messageId = R.string.main_help_to_upload_mls_locations_description;
        }
        Timber.d("displayHelpOnClick(): Displaying help for title: %s", titleId);
        if (titleId != View.NO_ID && messageId != View.NO_ID) {
            String message = getString(messageId);
            if (additionalMessageId != View.NO_ID) {
                message += "\n\n" + getString(additionalMessageId);
            }
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle(titleId).setMessage(message).setPositiveButton(R.string.dialog_ok, null).create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    // have to be public to prevent Force Close
    public void displayBatteryOptimizationsHelpOnClick(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.main_help_battery_optimizations_title)
                .setMessage(R.string.main_help_battery_optimizations_description)
                .setPositiveButton(R.string.dialog_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startBatteryOptimizationsSystemActivity();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    // have to be public to prevent Force Close
    public void displayPowerSaveModeHelpOnClick(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.main_help_power_save_mode_title)
                .setMessage(R.string.main_help_power_save_mode_description)
                .setPositiveButton(R.string.dialog_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startBatterySaverSystemActivity();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    // have to be public to prevent Force Close
    public void displayAirplaneModeHelpOnClick(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.main_help_airplane_mode_title)
                .setMessage(R.string.main_help_airplane_mode_description)
                .setPositiveButton(R.string.dialog_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAirplaneModeSystemActivity();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void displayNewVersionDownloadOptions(UpdateInfo updateInfo) {
        // display dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogLayout = inflater.inflate(R.layout.new_version, null);
        dialogBuilder.setView(dialogLayout);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);
        alertDialog.setTitle(R.string.updater_dialog_new_version_available);
        // load data
        ArrayAdapter<UpdateInfo.DownloadLink> adapter = new UpdateDialogArrayAdapter(alertDialog.getContext(), inflater, updateInfo.getDownloadLinks());
        ListView listView = (ListView) dialogLayout.findViewById(R.id.download_options_list);
        listView.setAdapter(adapter);
        // bind events
        final CheckBox disableAutoUpdateCheckCheckbox = (CheckBox) dialogLayout.findViewById(R.id.download_options_disable_auto_update_check_checkbox);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DownloadLink downloadLink = (DownloadLink) parent.getItemAtPosition(position);
                Timber.d("displayNewVersionDownloadOptions(): Selected position: %s", downloadLink.getLabel());
                boolean disableAutoUpdateCheckCheckboxChecked = disableAutoUpdateCheckCheckbox.isChecked();
                Timber.d("displayNewVersionDownloadOptions(): Disable update check checkbox checked = %s", disableAutoUpdateCheckCheckboxChecked);
                if (disableAutoUpdateCheckCheckboxChecked) {
                    MyApplication.getPreferencesProvider().setUpdateCheckEnabled(false);
                }
                MyApplication.getAnalytics().sendUpdateAction(downloadLink.getLabel());
                String[] links = downloadLink.getLinks();
                boolean startActivityFailed = false;
                for (String link : links) {
                    startActivityFailed = false;
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                        break;
                    } catch (ActivityNotFoundException ex) {
                        startActivityFailed = true;
                    }
                }
                if (startActivityFailed)
                    Toast.makeText(getApplication(), R.string.web_browser_missing, Toast.LENGTH_LONG).show();
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean disableAutoUpdateCheckCheckboxChecked = disableAutoUpdateCheckCheckbox.isChecked();
                Timber.d("displayNewVersionDownloadOptions(): Disable update check checkbox checked = %s", disableAutoUpdateCheckCheckboxChecked);
                if (disableAutoUpdateCheckCheckboxChecked) {
                    MyApplication.getPreferencesProvider().setUpdateCheckEnabled(false);
                }
            }
        });
        alertDialog.show();
    }

    private void displayNotCompatibleDialog() {
        // check if displayed in this app run
        if (showNotCompatibleDialog) {
            // check if not disabled in preferences
            boolean showCompatibilityWarningEnabled = MyApplication.getPreferencesProvider().getShowCompatibilityWarning();
            if (showCompatibilityWarningEnabled) {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                // check if device contains telephony hardware (some tablets doesn't report even if have)
                // NOTE: in the future this may need to be expanded when new specific features appear
                PackageManager packageManager = getPackageManager();
                boolean noRadioDetected = !(packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) && (packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_GSM) || packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CDMA)));
                // show dialog if something is not supported
                if (noRadioDetected) {
                    Timber.d("displayNotCompatibleDialog(): Not compatible because of radio: %s, phone type: %s", noRadioDetected, telephonyManager.getPhoneType());
                    //use custom layout to show "don't show this again" checkbox
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View dialogLayout = inflater.inflate(R.layout.dont_show_again_dialog, null);
                    final CheckBox dontShowAgainCheckbox = (CheckBox) dialogLayout.findViewById(R.id.dont_show_again_dialog_checkbox);
                    dialogBuilder.setView(dialogLayout);
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.setCancelable(true);
                    alertDialog.setTitle(R.string.main_dialog_not_compatible_title);
                    StringBuilder stringBuilder = new StringBuilder(getString(R.string.main_dialog_not_compatible_begin));
                    if (noRadioDetected) {
                        stringBuilder.append(getString(R.string.main_dialog_no_compatible_mobile_radio_message));
                    }
                    // text set this way to prevent checkbox from disappearing when text is too long
                    TextView messageTextView = (TextView) dialogLayout.findViewById(R.id.dont_show_again_dialog_textview);
                    messageTextView.setText(stringBuilder.toString());
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            boolean dontShowAgainCheckboxChecked = dontShowAgainCheckbox.isChecked();
                            Timber.d("displayNotCompatibleDialog(): Don't show again checkbox checked = %s", dontShowAgainCheckboxChecked);
                            if (dontShowAgainCheckboxChecked) {
                                MyApplication.getPreferencesProvider().setShowCompatibilityWarning(false);
                            }
                        }
                    });
                    alertDialog.show();
                }
            }
            showNotCompatibleDialog = false;
        }
    }

    private void displayIntroduction() {
        if (MyApplication.getPreferencesProvider().getShowIntroduction()) {
            Timber.d("displayIntroduction(): Showing introduction");
            DialogManager.createHtmlInfoDialog(this, R.string.info_introduction_title, R.raw.info_introduction_content, false, false).show();
            MyApplication.getPreferencesProvider().setShowIntroduction(false);
        }
    }

    private void displayDevelopersMessages() {
        int previousVersionCode = MyApplication.getPreferencesProvider().getPreviousDeveloperMessagesVersion();
        int currentVersionCode = ApkUtils.getApkVersionCode();
        if (previousVersionCode != currentVersionCode) {
            MyApplication.getPreferencesProvider().setRecentDeveloperMessagesVersion(currentVersionCode);
            // skip recent changes for first startup
            if (MyApplication.getPreferencesProvider().getShowIntroduction()) {
                return;
            }
            Timber.d("displayDevelopersMessages(): Showing changelog between %s and %s", previousVersionCode, currentVersionCode);
            ChangelogProvider provider = new ChangelogProvider(getApplication(), R.raw.changelog);
            ChangelogInfo changelog = provider.getChangelog(previousVersionCode);
            if (changelog.isEmpty())
                return;
            HtmlChangelogFormatter formatter = new HtmlChangelogFormatter();
            String message = formatter.formatChangelog(changelog);
            DialogInterface.OnClickListener remindLaterAction = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // reset setting for next cold start
                    MyApplication.getPreferencesProvider().setRecentDeveloperMessagesVersion(previousVersionCode);
                }
            };
            DialogManager.createHtmlInfoDialog(this, R.string.dialog_what_is_new, message, false, false, R.string.dialog_remind_later, remindLaterAction).show();
        }
    }

    public void displayExportFinishedDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogLayout = inflater.inflate(R.layout.export_finished_dialog, null);
        TextView messageTextView = dialogLayout.findViewById(R.id.export_finished_dialog_textview);
        messageTextView.setText(getString(R.string.export_dialog_finished_message, exportedDirAbsolutePath));
        ExportAction recentAction = MyApplication.getPreferencesProvider().getExportAction();
        boolean singleFile = exportedFilePaths.length == 1;
        if (!singleFile && recentAction == ExportAction.Open)
            recentAction = ExportAction.Keep;
        final RadioButton keepRadioButton = dialogLayout.findViewById(R.id.export_finished_dialog_keep_radiobutton);
        final RadioButton openRadioButton = dialogLayout.findViewById(R.id.export_finished_dialog_open_radiobutton);
        final RadioButton shareRadioButton = dialogLayout.findViewById(R.id.export_finished_dialog_share_radiobutton);
        openRadioButton.setEnabled(singleFile);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.export_dialog_finished_title);
        builder.setView(dialogLayout);
        builder.setCancelable(true);
        if (recentAction == ExportAction.Share) {
            builder.setPositiveButton(R.string.dialog_share, (dialog, which) -> {
                exportShareAction();
                dismissExportFinishedDialog(dialog);
            });
        } else if (recentAction == ExportAction.Open) {
            builder.setPositiveButton(R.string.dialog_open, (dialog, which) -> {
                exportOpenAction();
                dismissExportFinishedDialog(dialog);
            });
        } else {
            builder.setPositiveButton(R.string.dialog_keep, (dialog, which) -> {
                exportKeepAction();
                dismissExportFinishedDialog(dialog);
            });
        }
        builder.setNeutralButton(R.string.dialog_upload, (dialog, which) -> {
            MyApplication.getAnalytics().sendExportUploadAction();
            startUploaderTaskWithCheck();
        });
        builder.setNegativeButton(R.string.dialog_delete, (dialog, which) -> {
            // show dialog that runs async task
            AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(this);
            deleteBuilder.setTitle(R.string.delete_dialog_title);
            deleteBuilder.setMessage(R.string.delete_dialog_message);
            deleteBuilder.setPositiveButton(R.string.dialog_ok, (positiveDialog, positiveWhich) -> {
                MeasurementsDatabase.getInstance(MyApplication.getApplication()).deleteAllMeasurements();
                EventBus.getDefault().post(new PrintMainWindowEvent());
                MyApplication.getAnalytics().sendExportDeleteAction();
            });
            deleteBuilder.setNegativeButton(R.string.dialog_cancel, (negativeDialog, id) -> {
                // cancel
            });
            AlertDialog deleteDialog = deleteBuilder.create();
            deleteDialog.setCanceledOnTouchOutside(true);
            deleteDialog.setCancelable(true);
            deleteDialog.show();
        });
        builder.setOnCancelListener(dialog -> {
            exportKeepAction();
            exportedFilePaths = null;
        });

        AlertDialog dialog = builder.create();
        ExportAction finalRecentAction = recentAction;
        dialog.setOnShowListener(dialog1 -> {
            keepRadioButton.setChecked(finalRecentAction == ExportAction.Keep);
            openRadioButton.setChecked(finalRecentAction == ExportAction.Open);
            shareRadioButton.setChecked(finalRecentAction == ExportAction.Share);
            CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (isChecked) {
                    ExportAction newRecentAction;
                    if (buttonView == openRadioButton) {
                        newRecentAction = ExportAction.Open;
                        positiveButton.setText(R.string.dialog_open);
                        positiveButton.setOnClickListener(v -> {
                            exportOpenAction();
                            dismissExportFinishedDialog(dialog);
                        });
                    } else if (buttonView == shareRadioButton) {
                        newRecentAction = ExportAction.Share;
                        positiveButton.setText(R.string.dialog_share);
                        positiveButton.setOnClickListener(v -> {
                            exportShareAction();
                            dismissExportFinishedDialog(dialog);
                        });
                    } else {
                        newRecentAction = ExportAction.Keep;
                        positiveButton.setText(R.string.dialog_keep);
                        positiveButton.setOnClickListener(v -> {
                            exportKeepAction();
                            dismissExportFinishedDialog(dialog);
                        });
                    }
                    MyApplication.getPreferencesProvider().setExportAction(newRecentAction);
                }
            };
            keepRadioButton.setOnCheckedChangeListener(listener);
            openRadioButton.setOnCheckedChangeListener(listener);
            shareRadioButton.setOnCheckedChangeListener(listener);
        });

        dialog.show();

        exportedDirAbsolutePath = null;
    }

    private void dismissExportFinishedDialog(DialogInterface dialog) {
        dialog.dismiss();
        exportedFilePaths = null;
    }

    private void exportKeepAction() {
        MyApplication.getAnalytics().sendExportKeepAction();
    }

    private void exportOpenAction() {
        MyApplication.getAnalytics().sendExportOpenAction();
        if (exportedFilePaths == null)
            return;
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = Uri.parse(exportedFilePaths[0]);
        String calculatedMimeType = getApplication().getContentResolver().getType(fileUri);
        openIntent.setDataAndType(fileUri, calculatedMimeType);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(openIntent);
        } catch (Exception ex) {
            Toast.makeText(getApplication(), R.string.system_toast_no_handler_for_operation, Toast.LENGTH_LONG).show();
        }
    }

    private void exportShareAction() {
        MyApplication.getAnalytics().sendExportShareAction();
        if (exportedFilePaths == null)
            return;
        ShareCompat.IntentBuilder shareIntent = new ShareCompat.IntentBuilder(this);
        for (String filePath : exportedFilePaths) {
            Uri fileUri = Uri.parse(filePath);
            shareIntent.addStream(fileUri);
        }
        String calculatedMimeType = FileUtils.getFileMimeType(exportedFilePaths);
        shareIntent.setType(calculatedMimeType);
        Intent intent = shareIntent.getIntent();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, null));
    }

    // ========== MENU START/STOP METHODS ========== //

    private void startCollectorServiceWithCheck() {
        String runningTaskClassName = MyApplication.getBackgroundTaskName();
        if (runningTaskClassName != null) {
            Timber.d("startCollectorServiceWithCheck(): Another task is running in background: %s", runningTaskClassName);
            backgroundTaskHelper.showTaskRunningMessage(runningTaskClassName);
            return;
        }
        if (PermissionUtils.isNotificationPermissionRequired()) {
            MainActivityPermissionsDispatcher.startCollectorServiceApi33WithPermissionCheck(MainActivity.this);
        } else {
            MainActivityPermissionsDispatcher.startCollectorServiceWithPermissionCheck(MainActivity.this);
        }
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.FOREGROUND_SERVICE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void startCollectorServiceApi33() {
        startCollectorServiceInternal();
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void startCollectorService() {
        startCollectorServiceInternal();
    }

    private void startCollectorServiceInternal() {
        askAndSetGpsEnabled();
        if (isGpsEnabled) {
            Timber.d("startCollectorService(): Air plane mode off, starting service");
            // create intent
            final Intent intent = new Intent(this, CollectorService.class);
            // pass means of transport inside intent
            boolean gpsOptimizationsEnabled = MyApplication.getPreferencesProvider().getGpsOptimizationsEnabled();
            MeansOfTransport selectedType = (gpsOptimizationsEnabled ? MeansOfTransport.Universal : MeansOfTransport.Fixed);
            intent.putExtra(CollectorService.INTENT_KEY_TRANSPORT_MODE, selectedType);
            // pass screen on mode
            final String keepScreenOnMode = MyApplication.getPreferencesProvider().getCollectorKeepScreenOnMode();
            intent.putExtra(CollectorService.INTENT_KEY_KEEP_SCREEN_ON_MODE, keepScreenOnMode);
            // pass analytics data
            intent.putExtra(CollectorService.INTENT_KEY_START_INTENT_SOURCE, IntentSource.User);
            // start service
            ContextCompat.startForegroundService(this, intent);
            EventBus.getDefault().post(new CollectorStartedEvent(intent));
            ApkUtils.reportShortcutUsage(MyApplication.getApplication(), R.string.shortcut_id_collector_toggle);
        }
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @OnShowRationale({Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.FOREGROUND_SERVICE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartCollectorShowRationaleApi33(PermissionRequest request) {
        onStartCollectorShowRationaleInternal(request);
    }

    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartCollectorShowRationale(PermissionRequest request) {
        onStartCollectorShowRationaleInternal(request);
    }

    private void onStartCollectorShowRationaleInternal(PermissionRequest request) {
        onShowRationale(request, GpsUtils.isPreciseLocationAware() ? R.string.permission_collector_rationale_api31_message : R.string.permission_collector_rationale_message);
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @OnPermissionDenied({Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.FOREGROUND_SERVICE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartCollectorPermissionDeniedApi33() {
        onStartCollectorPermissionDeniedInternal();
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartCollectorPermissionDenied() {
        onStartCollectorPermissionDeniedInternal();
    }

    private void onStartCollectorPermissionDeniedInternal() {
        onPermissionDenied(R.string.permission_collector_denied_message);
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @OnNeverAskAgain({Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.FOREGROUND_SERVICE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartCollectorNeverAskAgainApi33() {
        onStartCollectorNeverAskAgainInternal();
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartCollectorNeverAskAgain() {
        onStartCollectorNeverAskAgainInternal();
    }

    private void onStartCollectorNeverAskAgainInternal() {
        onNeverAskAgain(R.string.permission_collector_never_ask_again_message);
    }

    private void stopCollectorService() {
        stopService(new Intent(this, CollectorService.class));
        ApkUtils.reportShortcutUsage(MyApplication.getApplication(), R.string.shortcut_id_collector_toggle);
    }

    private void startUploaderTaskWithCheck() {
        String runningTaskClassName = MyApplication.getBackgroundTaskName();
        if (runningTaskClassName != null) {
            Timber.d("startUploaderTaskWithCheck(): Another task is running in background: %s", runningTaskClassName);
            backgroundTaskHelper.showTaskRunningMessage(runningTaskClassName);
            return;
        }

        if (PermissionUtils.isNotificationPermissionRequired()) {
            MainActivityPermissionsDispatcher.startUploaderTaskInternalApi33WithPermissionCheck(MainActivity.this);
        } else {
            startUploaderTaskInternal();
        }
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({Manifest.permission.POST_NOTIFICATIONS})
    void startUploaderTaskInternalApi33() {
        startUploaderTaskInternal();
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @OnShowRationale({Manifest.permission.POST_NOTIFICATIONS})
    void onNotificationRationaleApi33(final PermissionRequest request) {
        onNotificationRationaleInternal(request);
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @OnPermissionDenied({Manifest.permission.POST_NOTIFICATIONS})
    void onNotificationPermissionDeniedApi33() {
        onNotificationPermissionDeniedInternal();
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @OnNeverAskAgain({Manifest.permission.POST_NOTIFICATIONS})
    void onNotificationNeverAskAgainApi33() {
        onNotificationNeverAskAgainInternal();
    }

    private void startUploaderTaskInternal() {
        if (!NetworkUtils.isNetworkAvailable(MyApplication.getApplication())) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.uploader_no_internet_title)
                    .setMessage(R.string.uploader_no_internet_message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .show();
            return;
        }
        final PreferencesProvider preferencesProvider = MyApplication.getPreferencesProvider();
        final boolean isOcidUploadEnabled = preferencesProvider.isOpenCellIdUploadEnabled();
        final boolean isUseSharedOpenCellIdApiKeyEnabled = preferencesProvider.isUseSharedOpenCellIdApiKeyEnabled();
        final boolean isMlsUploadEnabled = preferencesProvider.isMlsUploadEnabled();
        final boolean isCustomMlsUploadEnabled = preferencesProvider.isCustomMlsUploadEnabled();
        final boolean isReuploadIfUploadFailsEnabled = preferencesProvider.isReuploadIfUploadFailsEnabled();
        Timber.i("startUploaderTaskWithCheck(): Upload for OCID = " + isOcidUploadEnabled + ", MLS = " + isMlsUploadEnabled);
        boolean showConfigurator = preferencesProvider.getShowConfiguratorBeforeUpload();
        if (showConfigurator) {
            Timber.d("startUploaderTaskWithCheck(): Showing upload configurator");
            // check API key
            boolean isApiKeyValid = OpenCellIdUtils.isApiKeyValid(OpenCellIdUtils.getApiKey());
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogLayout = inflater.inflate(R.layout.configure_uploader_dialog, null);
            final CheckBox ocidUploadCheckbox = dialogLayout.findViewById(R.id.ocid_upload_dialog_checkbox);
            final CheckBox ocidAnonymousUploadCheckbox = dialogLayout.findViewById(R.id.ocid_anonymous_upload_dialog_checkbox);
            final TextView invalidApiKeyTextView = dialogLayout.findViewById(R.id.ocid_invalid_api_key_upload_dialog_textview);
            ocidUploadCheckbox.setChecked(isOcidUploadEnabled);
            ocidUploadCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ocidAnonymousUploadCheckbox.setEnabled(isChecked);
                }
            });
            ocidAnonymousUploadCheckbox.setChecked(isUseSharedOpenCellIdApiKeyEnabled);
            ocidAnonymousUploadCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    invalidApiKeyTextView.setVisibility(isChecked || isApiKeyValid ? View.GONE : View.VISIBLE);
                }
            });
            invalidApiKeyTextView.setVisibility(isUseSharedOpenCellIdApiKeyEnabled || isApiKeyValid ? View.GONE : View.VISIBLE);
            final CheckBox mlsUploadCheckbox = dialogLayout.findViewById(R.id.mls_upload_dialog_checkbox);
            mlsUploadCheckbox.setChecked(isMlsUploadEnabled);
            final CheckBox reuploadCheckbox = dialogLayout.findViewById(R.id.reupload_if_upload_fails_upload_dialog_checkbox);
            reuploadCheckbox.setChecked(isReuploadIfUploadFailsEnabled);
            final CheckBox dontShowAgainCheckbox = dialogLayout.findViewById(R.id.dont_show_again_dialog_checkbox);
            AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dialogLayout).create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.setCancelable(true);
            alertDialog.setTitle(R.string.upload_configurator_dialog_title);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_upload), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean isOcidUploadCheckedTemp = ocidUploadCheckbox.isChecked();
                    boolean isOcidAnonymousUploadCheckedTemp = ocidAnonymousUploadCheckbox.isChecked();
                    boolean isMlsUploadCheckedTemp = mlsUploadCheckbox.isChecked();
                    boolean isReuploadIfUploadFailsCheckedTemp = reuploadCheckbox.isChecked();
                    if (dontShowAgainCheckbox.isChecked()) {
                        preferencesProvider.setOpenCellIdUploadEnabled(isOcidUploadCheckedTemp);
                        preferencesProvider.setUseSharedOpenCellIdApiKeyEnabled(isOcidAnonymousUploadCheckedTemp);
                        preferencesProvider.setMlsUploadEnabled(isMlsUploadCheckedTemp);
                        preferencesProvider.setReuploadIfUploadFailsEnabled(isReuploadIfUploadFailsCheckedTemp);
                        preferencesProvider.setShowConfiguratorBeforeUpload(false);
                    }
                    if (!isOcidUploadCheckedTemp && !isMlsUploadCheckedTemp) {
                        showAllProjectsDisabledMessage();
                    } else {
                        startUploaderTask(isOcidUploadCheckedTemp, isOcidAnonymousUploadCheckedTemp, isMlsUploadCheckedTemp, isCustomMlsUploadEnabled, isReuploadIfUploadFailsCheckedTemp);
                    }
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.main_menu_preferences_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startPreferencesActivity();
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog.show();
        } else {
            Timber.d("startUploaderTaskWithCheck(): Using upload configuration from preferences");
            if (!isOcidUploadEnabled && !isMlsUploadEnabled) {
                showAllProjectsDisabledMessage();
            } else {
                startUploaderTask(isOcidUploadEnabled, isUseSharedOpenCellIdApiKeyEnabled, isMlsUploadEnabled, isCustomMlsUploadEnabled, isReuploadIfUploadFailsEnabled);
            }
        }
    }

    private void onNotificationRationaleInternal(PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.permission_notification_rationale_message)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_proceed, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .show();
    }

    private void onNotificationPermissionDeniedInternal() {
        Toast.makeText(this, R.string.permission_notification_denied_message, Toast.LENGTH_LONG).show();
    }

    private void onNotificationNeverAskAgainInternal() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_denied)
                .setMessage(R.string.permission_notification_never_ask_again_message)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtils.openAppSettings(MyApplication.getApplication());
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showAllProjectsDisabledMessage() {
        Snackbar snackbar = Snackbar.make(activityView, R.string.uploader_all_projects_disabled, Snackbar.LENGTH_LONG)
                .setAction(R.string.main_menu_preferences_button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPreferencesActivity();
                    }
                });
        // hack for black text on dark grey background
        View view = snackbar.getView();
        TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private void startUploaderTask(boolean isOcidUploadEnabled, boolean isOcidAnonymousUploadEnabled, boolean isMlsUploadEnabled, boolean isCustomMlsUploadEnabled, boolean isReuploadIfUploadFailsEnabled) {
        // start task
        if (!MyApplication.isBackgroundTaskRunning(UploaderWorker.class)) {
            WorkRequest uploaderWorkRequest = new OneTimeWorkRequest.Builder(UploaderWorker.class)
                    .setInputData(new Data.Builder()
                            .putBoolean(UploaderWorker.INTENT_KEY_UPLOAD_TO_OCID, isOcidUploadEnabled)
                            .putBoolean(UploaderWorker.INTENT_KEY_UPLOAD_TO_OCID_SHARED, isOcidAnonymousUploadEnabled)
                            .putBoolean(UploaderWorker.INTENT_KEY_UPLOAD_TO_MLS, isMlsUploadEnabled)
                            .putBoolean(UploaderWorker.INTENT_KEY_UPLOAD_TO_CUSTOM_MLS, isCustomMlsUploadEnabled)
                            .putBoolean(UploaderWorker.INTENT_KEY_UPLOAD_TRY_REUPLOAD, isReuploadIfUploadFailsEnabled)
                            .putString(UploaderWorker.INTENT_KEY_START_INTENT_SOURCE, IntentSource.User.name())
                            .build())
                    .addTag(UploaderWorker.WORKER_TAG)
                    .build();
            showUploaderProgress(uploaderWorkRequest.getId());
            WorkManager.getInstance(MyApplication.getApplication())
                    .enqueue(uploaderWorkRequest);
            ApkUtils.reportShortcutUsage(MyApplication.getApplication(), R.string.shortcut_id_uploader_toggle);
        } else
            Toast.makeText(getApplication(), R.string.uploader_already_running, Toast.LENGTH_LONG).show();
    }

    private void startExportTaskWithCheck() {
        String runningTaskClassName = MyApplication.getBackgroundTaskName();
        if (runningTaskClassName != null) {
            Timber.d("startExportTask(): Another task is running in background: %s", runningTaskClassName);
            backgroundTaskHelper.showTaskRunningMessage(runningTaskClassName);
            return;
        }

        if (PermissionUtils.isNotificationPermissionRequired()) {
            MainActivityPermissionsDispatcher.startExportTaskInternalApi33WithPermissionCheck(MainActivity.this);
        } else {
            startExportTaskInternal();
        }
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({Manifest.permission.POST_NOTIFICATIONS})
    void startExportTaskInternalApi33() {
        startExportTaskInternal();
    }

    private void startExportTaskInternal() {
        startExportTask();
    }

    private void startExportTask() {
        Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
        if (StorageUtils.canWriteStorageUri(storageUri)) {
            final PreferencesProvider preferencesProvider = MyApplication.getPreferencesProvider();
            List<FileType> recentFileTypes = preferencesProvider.getEnabledExportFileTypes();
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogLayout = inflater.inflate(R.layout.configure_exporter_dialog, null);
            final CheckBox csvExportCheckbox = dialogLayout.findViewById(R.id.csv_export_dialog_checkbox);
            csvExportCheckbox.setChecked(recentFileTypes.contains(FileType.Csv));
            final CheckBox gpxExportCheckbox = dialogLayout.findViewById(R.id.gpx_export_dialog_checkbox);
            gpxExportCheckbox.setChecked(recentFileTypes.contains(FileType.Gpx));
            final CheckBox kmlExportCheckbox = dialogLayout.findViewById(R.id.kml_export_dialog_checkbox);
            kmlExportCheckbox.setChecked(recentFileTypes.contains(FileType.Kml));
            final CheckBox kmzExportCheckbox = dialogLayout.findViewById(R.id.kmz_export_dialog_checkbox);
            kmzExportCheckbox.setChecked(recentFileTypes.contains(FileType.Kmz));
            final CheckBox csvOcidExportCheckbox = dialogLayout.findViewById(R.id.csv_ocid_export_dialog_checkbox);
            csvOcidExportCheckbox.setChecked(recentFileTypes.contains(FileType.CsvOcid));
            final CheckBox jsonMlsExportCheckbox = dialogLayout.findViewById(R.id.json_mls_export_dialog_checkbox);
            jsonMlsExportCheckbox.setChecked(recentFileTypes.contains(FileType.JsonMls));
            final CheckBox compressExportCheckbox = dialogLayout.findViewById(R.id.compress_export_dialog_checkbox);
            compressExportCheckbox.setChecked(recentFileTypes.contains(FileType.Compress));
            AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dialogLayout).create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.setCancelable(true);
            alertDialog.setTitle(R.string.export_dialog_format_selection_title);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_export), (dialog, which) -> {
                List<FileType> selectedFileTypes = new ArrayList<>();
                if (csvExportCheckbox.isChecked())
                    selectedFileTypes.add(FileType.Csv);
                if (gpxExportCheckbox.isChecked())
                    selectedFileTypes.add(FileType.Gpx);
                if (kmlExportCheckbox.isChecked())
                    selectedFileTypes.add(FileType.Kml);
                if (kmzExportCheckbox.isChecked())
                    selectedFileTypes.add(FileType.Kmz);
                if (csvOcidExportCheckbox.isChecked())
                    selectedFileTypes.add(FileType.CsvOcid);
                if (jsonMlsExportCheckbox.isChecked())
                    selectedFileTypes.add(FileType.JsonMls);
                if (compressExportCheckbox.isChecked())
                    selectedFileTypes.add(FileType.Compress);
                preferencesProvider.setEnabledExportFileTypes(selectedFileTypes);
                Timber.d("startExportTask(): User selected positions: %s", TextUtils.join(",", selectedFileTypes));
                if (selectedFileTypes.isEmpty()) {
                    Toast.makeText(getApplication(), R.string.export_toast_no_file_types_selected, Toast.LENGTH_LONG).show();
                } else {
                    WorkRequest exportWorkRequest = new OneTimeWorkRequest.Builder(ExportWorker.class)
                            .setInputData(new Data.Builder()
                                    .putStringArray(ExportWorker.SELECTED_FILE_TYPES, FileType.toNames(selectedFileTypes).toArray(new String[0]))
                                    .putString(ExportWorker.INTENT_SOURCE, IntentSource.User.name())
                                    .build())
                            .addTag(ExportWorker.WORKER_TAG)
                            .build();
                    showExportProgress(storageUri, exportWorkRequest.getId());
                    WorkManager.getInstance(MyApplication.getApplication())
                            .enqueue(exportWorkRequest);
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_cancel), (dialog, which) -> {
                // empty
            });
            alertDialog.show();
        } else {
            StorageUtils.requestStorageUri(this);
        }
    }

    private void restoreExportProgress() {
        ListenableFuture<List<WorkInfo>> statuses = WorkManager.getInstance(MyApplication.getApplication())
                .getWorkInfosByTag(ExportWorker.WORKER_TAG);
        Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
        try {
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                    showExportProgress(storageUri, workInfo.getId());
                    break;
                } else if (state == WorkInfo.State.SUCCEEDED || state == WorkInfo.State.FAILED) {
                    showExportFinished(workInfo);
                    break;
                }
            }

        } catch (ExecutionException | InterruptedException ex) {
            Timber.e(ex, "restoreExportProgress(): Failed to show export progress");
            Toast.makeText(MainActivity.this, getString(R.string.export_toast_failed, ex.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void showExportProgress(Uri storageUri, UUID workId) {
        WorkManager.getInstance(MyApplication.getApplication())
                .getWorkInfoByIdLiveData(workId)
                .observe(this, new Observer<WorkInfo>() {
                    private final String INNER_TAG = MainActivity.class.getSimpleName() + "." + ExportWorker.class.getSimpleName();

                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo == null) {
                            Timber.tag(INNER_TAG).w("onChanged(): WorkInfo is null");
                            // hide loading indicator
                            if (exportProgressDialog != null) {
                                exportProgressDialog.dismiss();
                            }
                            exportProgressDialog = null;
                            return;
                        }
                        int currentPercent = workInfo.getProgress().getInt(ExportWorker.PROGRESS, ExportWorker.PROGRESS_MIN_VALUE);
                        int maxPercent = workInfo.getProgress().getInt(ExportWorker.PROGRESS_MAX, ExportWorker.PROGRESS_MAX_VALUE);
                        Timber.tag(INNER_TAG).d("onChanged(): Updating progress: %s %s", currentPercent, maxPercent);
                        if (exportProgressDialog == null) {
                            // show loading indicator
                            exportProgressDialog = ExportProgressDialogFragment.createInstance(storageUri, currentPercent, maxPercent);
                            exportProgressDialog.setCancelListener(MainActivity.this);
                            exportProgressDialog.show(getSupportFragmentManager(), ExportProgressDialogFragment.FRAGMENT_TAG);
                        } else {
                            currentPercent = Math.min(currentPercent, maxPercent);
                            exportProgressDialog.setProgress(currentPercent);
                        }
                        if (workInfo.getState().isFinished()) {
                            // hide loading indicator
                            exportProgressDialog.dismiss();
                            exportProgressDialog = null;
                            // perform finish action
                            showExportFinished(workInfo);
                        }
                    }
                });
    }

    private void showExportFinished(WorkInfo workInfo) {
        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
            exportedDirAbsolutePath = workInfo.getOutputData().getString(ExportWorker.DIR_PATH);
            exportedFilePaths = workInfo.getOutputData().getStringArray(ExportWorker.FILE_PATHS);
            displayExportFinishedDialog();
        } else if (workInfo.getState() == WorkInfo.State.CANCELLED) {
            Toast.makeText(MainActivity.this, R.string.export_toast_cancelled, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, workInfo.getOutputData().getString(ExportWorker.MESSAGE), Toast.LENGTH_LONG).show();
        }
        WorkManager.getInstance(MyApplication.getApplication()).pruneWork();
    }

    private void cancelExport() {
        // cancel generation and it will return that task should be cancelled
        WorkManager.getInstance(MyApplication.getApplication())
                .cancelAllWorkByTag(ExportWorker.WORKER_TAG);
    }

    private void restoreUploaderProgress() {
        ListenableFuture<List<WorkInfo>> statuses = WorkManager.getInstance(MyApplication.getApplication())
                .getWorkInfosByTag(UploaderWorker.WORKER_TAG);
        try {
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                    showUploaderProgress(workInfo.getId());
                    break;
                } else if (state == WorkInfo.State.SUCCEEDED || state == WorkInfo.State.FAILED) {
                    showUploaderFinished(workInfo);
                    break;
                }
            }

        } catch (ExecutionException | InterruptedException ex) {
            Timber.e(ex, "restoreUploaderProgress(): Failed to show uploader progress");
            Toast.makeText(MainActivity.this, getString(R.string.uploader_toast_failed, ex.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void showUploaderProgress(UUID workId) {
        WorkManager.getInstance(MyApplication.getApplication())
                .getWorkInfoByIdLiveData(workId)
                .observe(this, new Observer<WorkInfo>() {
                    private final String INNER_TAG = MainActivity.class.getSimpleName() + "." + UploaderWorker.class.getSimpleName();

                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo == null) {
                            Timber.tag(INNER_TAG).w("onChanged(): WorkInfo is null");
                            // hide loading indicator
                            if (uploaderProgressDialog != null) {
                                uploaderProgressDialog.dismiss();
                            }
                            uploaderProgressDialog = null;
                            return;
                        }
                        int currentPercent = workInfo.getProgress().getInt(UploaderWorker.PROGRESS, UploaderWorker.PROGRESS_MIN_VALUE);
                        int maxPercent = workInfo.getProgress().getInt(UploaderWorker.PROGRESS_MAX, UploaderWorker.PROGRESS_MAX_VALUE);
                        Timber.tag(INNER_TAG).d("onChanged(): Updating progress: %s %s", currentPercent, maxPercent);
                        if (uploaderProgressDialog == null) {
                            // show loading indicator
                            uploaderProgressDialog = UploaderProgressDialogFragment.createInstance(currentPercent, maxPercent);
                            uploaderProgressDialog.setCancelListener(MainActivity.this);
                            uploaderProgressDialog.show(getSupportFragmentManager(), UploaderProgressDialogFragment.FRAGMENT_TAG);
                        } else {
                            currentPercent = Math.min(currentPercent, maxPercent);
                            uploaderProgressDialog.setProgress(currentPercent);
                        }
                        if (workInfo.getState().isFinished()) {
                            // hide loading indicator
                            uploaderProgressDialog.dismiss();
                            uploaderProgressDialog = null;
                            // perform finish action
                            showUploaderFinished(workInfo);
                        }
                    }
                });
    }

    private void showUploaderFinished(WorkInfo workInfo) {
        if (workInfo.getState() == WorkInfo.State.CANCELLED) {
            Toast.makeText(MainActivity.this, R.string.uploader_toast_cancelled, Toast.LENGTH_LONG).show();
        } else {
            displayUploaderFinishedDialog(workInfo.getOutputData().getString(UploaderWorker.MESSAGE));
        }
        WorkManager.getInstance(MyApplication.getApplication()).pruneWork();
    }

    private void cancelUploader() {
        // cancel generation and it will return that task should be cancelled
        WorkManager.getInstance(MyApplication.getApplication())
                .cancelAllWorkByTag(UploaderWorker.WORKER_TAG);
    }

    public void displayUploaderFinishedDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.uploader_result_dialog_title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_ok, null);
        builder.show();
    }

    @Override
    public void onExportCancelled() {
        cancelExport();
        exportProgressDialog = null;
    }

    @Override
    public void onUploadCancelled() {
        cancelUploader();
        uploaderProgressDialog = null;
    }

    private void startCleanup() {
        // show dialog that runs async task
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.clear_dialog_title);
        builder.setMessage(R.string.clear_dialog_message);
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MeasurementsDatabase.getInstance(MyApplication.getApplication()).clearAllData();
                Toast.makeText(MainActivity.this, R.string.clear_toast_finished, Toast.LENGTH_SHORT).show();
                MapUtils.clearMapCache(MainActivity.this);
                EventBus.getDefault().post(new PrintMainWindowEvent());
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // cancel
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void startPreferencesActivity() {
        startActivity(new Intent(this, PreferencesActivity.class));
    }

    private boolean canStartNetworkTypeSystemActivity() {
        if (canStartNetworkTypeSystemActivityResult == null) {
            canStartNetworkTypeSystemActivityResult = (createDataRoamingSettingsIntent().resolveActivity(getPackageManager()) != null);
        }
        return canStartNetworkTypeSystemActivityResult;
    }

    private void startNetworkTypeSystemActivity() {
        try {
            startActivity(createDataRoamingSettingsIntent());
        } catch (ActivityNotFoundException ex) {
            Timber.w(ex, "startNetworkTypeSystemActivity(): Could not open Settings to change network type");
            MyApplication.handleSilentException(ex);
            showCannotOpenAndroidSettingsDialog();
        }
    }

    private void startDonateWebBrowserActivity() {
        try {
            Intent open = new Intent(Intent.ACTION_VIEW);
            open.setData(Uri.parse(getString(R.string.preferences_donate_link)));
            startActivity(open);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getApplication(), R.string.web_browser_missing, Toast.LENGTH_LONG).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void startBatteryOptimizationsSystemActivity() {
        try {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivityForResult(intent, BATTERY_OPTIMIZATIONS_ACTIVITY_RESULT);
        } catch (ActivityNotFoundException ex) {
            Timber.w(ex, "startBatteryOptimizationsSystemActivity(): Could not open Settings to change battery optimizations");
            showCannotOpenAndroidSettingsDialog();
        }
    }

    private void startBatterySaverSystemActivity() {
        try {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
            } else {
                intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$BatterySaverSettingsActivity"));
            }
            startActivityForResult(intent, BATTERY_SAVER_ACTIVITY_RESULT);
        } catch (ActivityNotFoundException ex) {
            Timber.w(ex, "startBatterySaverSystemActivity(): Could not open Settings to disable battery saver");
            MyApplication.handleSilentException(ex);
            showCannotOpenAndroidSettingsDialog();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void startAirplaneModeSystemActivity() {
        try {
            Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            startActivityForResult(intent, AIRPLANE_MODE_ACTIVITY_RESULT);
        } catch (ActivityNotFoundException ex) {
            Timber.w(ex, "startAirplaneModeSystemActivity(): Could not open Settings to change airplane mode");
            MyApplication.handleSilentException(ex);
            showCannotOpenAndroidSettingsDialog();
        }
    }

    private void showCannotOpenAndroidSettingsDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setMessage(R.string.dialog_could_not_open_android_settings).setPositiveButton(R.string.dialog_ok, null).create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    private Intent createDataRoamingSettingsIntent() {
        Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    // ========== SERVICE CONNECTIONS ========== //

    private ServiceConnection collectorServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("onServiceConnected(): Service connection created for %s", name);
            isCollectorServiceRunning.set(true);
            // refresh menu status
            invalidateOptionsMenu();
            if (binder instanceof ICollectorService) {
                collectorServiceBinder = (ICollectorService) binder;
                // display invalid system time if necessary
                printInvalidSystemTime(collectorServiceBinder);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("onServiceDisconnected(): Service connection destroyed of %s", name);
            unbindService(collectorServiceConnection);
            isCollectorServiceRunning.set(false);
            // refresh menu status
            invalidateOptionsMenu();
            // hide invalid system time message
            hideInvalidSystemTime();
            // release reference to service
            collectorServiceBinder = null;
        }
    };

    // ========== PERMISSION REQUEST HANDLING ========== //
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void onShowRationale(final PermissionRequest request, @StringRes int messageResId) {
        onShowRationale(request, getString(messageResId));
    }

    private void onShowRationale(final PermissionRequest request, String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_proceed, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .show();
    }

    private void onPermissionDenied(@StringRes int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_LONG).show();
    }

    private void onNeverAskAgain(@StringRes int messageResId) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_denied)
                .setMessage(messageResId)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtils.openAppSettings(MyApplication.getApplication());
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    // ========== MISCELLANEOUS ========== //

    private void askAndSetGpsEnabled() {
        if (GpsUtils.isGpsEnabled(getApplication())) {
            Timber.d("askAndSetGpsEnabled(): GPS enabled");
            isGpsEnabled = true;
            showAskForLocationSettingsDialog = false;
        } else {
            Timber.d("askAndSetGpsEnabled(): GPS disabled, asking user");
            isGpsEnabled = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_want_enable_gps).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Timber.d("askAndSetGpsEnabled(): display settings");
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    try {
                        startActivity(intent);
                        showAskForLocationSettingsDialog = true;
                    } catch (ActivityNotFoundException ex1) {
                        intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                        try {
                            startActivity(intent);
                            showAskForLocationSettingsDialog = true;
                        } catch (ActivityNotFoundException ex2) {
                            Timber.w("askAndSetGpsEnabled(): Could not open Settings to enable GPS");
                            MyApplication.handleSilentException(ex2);
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setMessage(R.string.dialog_could_not_open_android_settings).setPositiveButton(R.string.dialog_ok, null).create();
                            alertDialog.setCanceledOnTouchOutside(true);
                            alertDialog.setCancelable(true);
                            alertDialog.show();
                        }
                    } finally {
                        dialog.dismiss();
                    }
                }
            }).setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Timber.d("askAndSetGpsEnabled(): cancel");
                    dialog.cancel();
                    if (GpsUtils.isGpsEnabled(getApplication())) {
                        Timber.d("askAndSetGpsEnabled(): provider enabled in the meantime");
                        startCollectorServiceWithCheck();
                    } else {
                        isGpsEnabled = false;
                        showAskForLocationSettingsDialog = false;
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    private void checkForNewVersionAvailability() {
        boolean isAutoUpdateEnabled = MyApplication.getPreferencesProvider().getUpdateCheckEnabled();
        MyApplication.getAnalytics().sendPrefsUpdateCheckEnabled(isAutoUpdateEnabled);
        if (isAutoUpdateEnabled) {
            long currentDate = DateUtils.getCurrentDateWithoutTime();
            long lastUpdateCheckDate = MyApplication.getPreferencesProvider().getLastUpdateCheckDate();
            long diffInDays = DateUtils.getTimeDiff(currentDate, lastUpdateCheckDate);
            Timber.d("checkForNewVersionAvailability(): Last update check performed on: %s, diff to current in days: %s", new Date(lastUpdateCheckDate), diffInDays);
            // if currently is at least one day after last check (day, not 24 hrs)
            if (diffInDays >= 1) {
                // check if network available
                if (NetworkUtils.isNetworkAvailable(getApplication())) {
                    int currentVersion = ApkUtils.getApkVersionCode();
                    String updateFeedUrl = String.format(Locale.ENGLISH, BuildConfig.UPDATE_CHECK_FEED_URI, currentVersion);
                    if (!StringUtils.isNullEmptyOrWhitespace(updateFeedUrl)) {
                        UpdateCheckAsyncTask updateCheckTask = new UpdateCheckAsyncTask(getApplication(), currentVersion);
                        updateCheckTask.execute(updateFeedUrl);
                    }
                    MyApplication.getPreferencesProvider().setLastUpdateCheckDate(currentDate);
                } else {
                    Timber.d("checkForNewVersionAvailability(): No active network connection");
                }
            }
        }
    }

    private void processOnStartIntent(Intent intent) {
        if (intent != null) {
            if (intent.hasExtra(UpdateCheckAsyncTask.INTENT_KEY_UPDATE_INFO)) {
                try {
                    // display dialog with download options
                    displayNewVersionDownloadOptions((UpdateInfo) intent.getSerializableExtra(UpdateCheckAsyncTask.INTENT_KEY_UPDATE_INFO));
                } catch (Exception ex) {
                    Timber.e(ex, "processOnStartIntent(): Failed to deserialize new version extra, possibly after app upgrade");
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CollectorStartedEvent event) {
        bindService(event.getIntent(), collectorServiceConnection, 0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(GpsStatusChangedEvent event) {
        if (!event.isActive()) {
            isCollectorServiceRunning.set(false);
            invalidateOptionsMenu();
            hideInvalidSystemTime();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(MapEnabledChangedEvent event) {
        refreshTabs();
    }
}
