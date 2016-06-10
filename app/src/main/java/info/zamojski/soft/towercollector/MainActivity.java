/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import info.zamojski.soft.towercollector.analytics.IntentSource;
import info.zamojski.soft.towercollector.enums.FileType;
import info.zamojski.soft.towercollector.enums.MeansOfTransport;
import info.zamojski.soft.towercollector.enums.Validity;
import info.zamojski.soft.towercollector.events.CollectorStartedEvent;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.events.SystemTimeChangedEvent;
import info.zamojski.soft.towercollector.controls.DialogManager;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.model.ChangelogInfo;
import info.zamojski.soft.towercollector.model.UpdateInfo;
import info.zamojski.soft.towercollector.model.UpdateInfo.DownloadLink;
import info.zamojski.soft.towercollector.providers.ChangelogProvider;
import info.zamojski.soft.towercollector.providers.HtmlChangelogFormatter;
import info.zamojski.soft.towercollector.tasks.ExportFileAsyncTask;
import info.zamojski.soft.towercollector.tasks.UpdateCheckAsyncTask;
import info.zamojski.soft.towercollector.utils.ApkUtils;
import info.zamojski.soft.towercollector.utils.BackgroundTaskHelper;
import info.zamojski.soft.towercollector.utils.DateUtils;
import info.zamojski.soft.towercollector.utils.FileUtils;
import info.zamojski.soft.towercollector.utils.GpsUtils;
import info.zamojski.soft.towercollector.utils.NetworkUtils;
import info.zamojski.soft.towercollector.utils.StorageUtils;
import info.zamojski.soft.towercollector.utils.UpdateDialogArrayAdapter;
import info.zamojski.soft.towercollector.utils.StringUtils;
import info.zamojski.soft.towercollector.utils.Validator;
import info.zamojski.soft.towercollector.views.MainActivityPagerAdapter;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.acra.ACRA;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;

import trikita.log.Log;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private AtomicBoolean isCollectorServiceRunning = new AtomicBoolean(false);
    private boolean isGpsEnabled = false;
    private boolean showAskForLocationSettingsDialog = false;
    private boolean showNotCompatibleDialog = true;

    private String exportedFileAbsolutePath;
    private boolean showExportFinishedDialog = false;

    private Boolean canStartNetworkTypeSystemActivityResult = null;

    private Menu mainMenu;
    private MenuItem startMenu;
    private MenuItem stopMenu;
    private MenuItem networkTypeMenu;

    private boolean isMinimized = false;

    public ICollectorService collectorServiceBinder;

    private BackgroundTaskHelper backgroundTaskHelper;

    private MainActivityPagerAdapter pageAdapter;
    private ViewPager viewPager;

    // ========== ACTIVITY ========== //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MyApplication.getCurrentAppTheme());
        super.onCreate(savedInstanceState);
        Log.d("onCreate(): Creating activity");
        // set fixed screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);
        //setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        // setup tabbed layout
        pageAdapter = new MainActivityPagerAdapter(getSupportFragmentManager(), getApplication());
        viewPager = (ViewPager) findViewById(R.id.main_pager);
        viewPager.setAdapter(pageAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                Log.d("onTabSelected() Switching to tab %s", tab.getPosition());
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
        });

        backgroundTaskHelper = new BackgroundTaskHelper(this);

        displayNotCompatibleDialog();

        // show latest developer's messages
        displayDevelopersMessages();

        // show introduction
        displayIntroduction();

        processOnStartIntent(getIntent());

        // check for availability of new version
        checkForNewVersionAvailability();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy(): Unbinding from service");
        if (isCollectorServiceRunning.get())
            unbindService(collectorServiceConnection);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart(): Binding to service");
        isCollectorServiceRunning.set(isServiceRunning(CollectorService.SERVICE_FULL_NAME));
        if (isCollectorServiceRunning.get()) {
            bindService(new Intent(this, CollectorService.class), collectorServiceConnection, 0);
        }
        if (isMinimized && showExportFinishedDialog) {
            displayExportFinishedDialog();
        }
        isMinimized = false;
        EventBus.getDefault().register(this);
        MyApplication.getAnalytics().sendMainActivityStarted();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isMinimized = true;
        EventBus.getDefault().unregister(this);
        MyApplication.getAnalytics().sendMainActivityStopped();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume(): Resuming");
        // print on UI
        EventBus.getDefault().post(new PrintMainWindowEvent());
        // restore recent tab
        int recentTabIndex = MyApplication.getPreferencesProvider().getMainWindowRecentTab();
        viewPager.setCurrentItem(recentTabIndex);
        // if coming back from Android settings rerun the action
        if (showAskForLocationSettingsDialog) {
            startCollectorService();
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
        Log.d("onPause(): Pausing");
        // remember current tab
        int currentTabIndex = viewPager.getCurrentItem();
        MyApplication.getPreferencesProvider().setMainWindowRecentTab(currentTabIndex);
    }

    // ========== MENU ========== //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("onCreateOptionsMenu(): Loading action bar");
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
        Log.d("onPrepareOptionsMenu(): Preparing action bar menu for running = %s", isRunning);
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
        switch (item.getItemId()) {
            case R.id.main_menu_start:
                startCollectorService();
                return true;
            case R.id.main_menu_stop:
                stopCollectorService();
                return true;
            case R.id.main_menu_upload:
                startUploaderServiceWithCheck();
                return true;
            case R.id.main_menu_export:
                startExportAsyncTask();
                return true;
            case R.id.main_menu_preferences:
                startPreferencesActivity();
                return true;
            case R.id.main_menu_network_type:
                startNetworkTypeSystemActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("onNewIntent(): New intent received: %s", intent);
        processOnStartIntent(intent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (event.getAction() == KeyEvent.ACTION_UP && mainMenu != null) {
                Log.i("onKeyUp(): Hardware menu key pressed");
                mainMenu.performIdentifierAction(R.id.main_menu_more, 0);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    // ========== UI ========== //

    private void printInvalidSystemTime(ICollectorService collectorServiceBinder) {
        Validity valid = collectorServiceBinder.isSystemTimeValid();
        EventBus.getDefault().postSticky(new SystemTimeChangedEvent(valid));
    }

    private void hideInvalidSystemTime() {
        EventBus.getDefault().postSticky(new SystemTimeChangedEvent(Validity.Valid));
    }

    // have to be public to prevent Force Close
    public void displayHelpOnClick(View view) {
        int titleId = View.NO_ID;
        int messageId = View.NO_ID;
        switch (view.getId()) {
            case R.id.main_gps_status_tablerow:
                titleId = R.string.main_help_gps_status_title;
                messageId = R.string.main_help_gps_status_description;
                break;
            case R.id.main_invalid_system_time_tablerow:
                titleId = R.string.main_help_invalid_system_time_title;
                messageId = R.string.main_help_invalid_system_time_description;
                break;
            case R.id.main_stats_today_locations_tablerow:
                titleId = R.string.main_help_today_locations_title;
                messageId = R.string.main_help_today_locations_description;
                break;
            case R.id.main_stats_today_cells_tablerow:
                titleId = R.string.main_help_today_cells_title;
                messageId = R.string.main_help_today_cells_description;
                break;
            case R.id.main_stats_local_locations_tablerow:
                titleId = R.string.main_help_local_locations_title;
                messageId = R.string.main_help_local_locations_description;
                break;
            case R.id.main_stats_local_cells_tablerow:
                titleId = R.string.main_help_local_cells_title;
                messageId = R.string.main_help_local_cells_description;
                break;
            case R.id.main_stats_global_locations_tablerow:
                titleId = R.string.main_help_global_locations_title;
                messageId = R.string.main_help_global_locations_description;
                break;
            case R.id.main_stats_global_cells_tablerow:
                titleId = R.string.main_help_global_cells_title;
                messageId = R.string.main_help_global_cells_description;
                break;
            case R.id.main_last_network_type_tablerow:
                titleId = R.string.main_help_last_network_type_title;
                messageId = R.string.main_help_last_network_type_description;
                break;
            case R.id.main_last_cell_id_tablerow:
                titleId = R.string.main_help_last_cell_id_title;
                messageId = R.string.main_help_last_cell_id_description;
                break;
            case R.id.main_last_lac_tablerow:
                titleId = R.string.main_help_last_lac_title;
                messageId = R.string.main_help_last_lac_description;
                break;
            case R.id.main_last_mcc_tablerow:
                titleId = R.string.main_help_last_mcc_title;
                messageId = R.string.main_help_last_mcc_description;
                break;
            case R.id.main_last_mnc_tablerow:
                titleId = R.string.main_help_last_mnc_title;
                messageId = R.string.main_help_last_mnc_description;
                break;
            case R.id.main_last_signal_strength_tablerow:
                titleId = R.string.main_help_last_signal_strength_title;
                messageId = R.string.main_help_last_signal_strength_description;
                break;
            case R.id.main_last_latitude_tablerow:
                titleId = R.string.main_help_last_latitude_title;
                messageId = R.string.main_help_last_latitude_description;
                break;
            case R.id.main_last_longitude_tablerow:
                titleId = R.string.main_help_last_longitude_title;
                messageId = R.string.main_help_last_longitude_description;
                break;
            case R.id.main_last_gps_accuracy_tablerow:
                titleId = R.string.main_help_last_gps_accuracy_title;
                messageId = R.string.main_help_last_gps_accuracy_description;
                break;
            case R.id.main_last_date_time_tablerow:
                titleId = R.string.main_help_last_date_time_title;
                messageId = R.string.main_help_last_date_time_description;
                break;
        }
        Log.d("displayHelpOnClick(): Displaying help for title: %s", titleId);
        if (titleId != View.NO_ID && messageId != View.NO_ID) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle(titleId).setMessage(messageId).setPositiveButton(R.string.dialog_ok, null).create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    private void displayUploadResultDialog(Bundle extras) {
        int descriptionId = extras.getInt(UploaderService.INTENT_KEY_RESULT_DESCRIPTION);
        try {
            String descriptionContent = getString(descriptionId);
            Log.d("displayUploadResultDialog(): Received extras: %s", descriptionId);
            // display dialog
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.setCancelable(true);
            alertDialog.setTitle(R.string.uploader_result_dialog_title);
            alertDialog.setMessage(descriptionContent);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog.show();
        } catch (NotFoundException ex) {
            Log.w("displayUploadResultDialog(): Invalid string id received with intent extras: %s", descriptionId);
            MyApplication.getAnalytics().sendException(ex, Boolean.FALSE);
            ACRA.getErrorReporter().handleSilentException(ex);
        }
    }

    private void displayNewVersionDownloadOptions(Bundle extras) {
        UpdateInfo updateInfo = (UpdateInfo) extras.getSerializable(UpdateCheckAsyncTask.INTENT_KEY_UPDATE_INFO);
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
                Log.d("displayNewVersionDownloadOptions(): Selected position: %s", downloadLink.getLabel());
                boolean disableAutoUpdateCheckCheckboxChecked = disableAutoUpdateCheckCheckbox.isChecked();
                Log.d("displayNewVersionDownloadOptions(): Disable update check checkbox checked = %s", disableAutoUpdateCheckCheckboxChecked);
                if (disableAutoUpdateCheckCheckboxChecked) {
                    MyApplication.getPreferencesProvider().setUpdateCheckEnabled(false);
                }
                MyApplication.getAnalytics().sendUpdateAction(downloadLink.getLabel());
                String link = downloadLink.getLink();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getApplication(), R.string.web_browser_missing, Toast.LENGTH_LONG).show();
                }
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean disableAutoUpdateCheckCheckboxChecked = disableAutoUpdateCheckCheckbox.isChecked();
                Log.d("displayNewVersionDownloadOptions(): Disable update check checkbox checked = %s", disableAutoUpdateCheckCheckboxChecked);
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
                    Log.d("displayNotCompatibleDialog(): Not compatible because of radio: %s, phone type: %s", noRadioDetected, telephonyManager.getPhoneType());
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
                            Log.d("displayNotCompatibleDialog(): Don't show again checkbox checked = %s", dontShowAgainCheckboxChecked);
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

    private boolean displayInAirplaneModeDialog() {
        // check if in airplane mode
        boolean inAirplaneMode = NetworkUtils.isInAirplaneMode(getApplication());
        if (inAirplaneMode) {
            Log.d("displayInAirplaneModeDialog(): Device is in airplane mode");
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.setCancelable(true);
            alertDialog.setTitle(R.string.main_dialog_in_airplane_mode_title);
            alertDialog.setMessage(getString(R.string.main_dialog_in_airplane_mode_message));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog.show();
        }
        return inAirplaneMode;
    }

    private void displayIntroduction() {
        if (MyApplication.getPreferencesProvider().getShowIntroduction()) {
            Log.d("displayIntroduction(): Showing introduction");
            DialogManager.createHtmlInfoDialog(this, R.string.info_introduction_title, R.raw.info_introduction_content, false, false).show();
            MyApplication.getPreferencesProvider().setShowIntroduction(false);
        }
    }

    private void displayDevelopersMessages() {
        int previousVersionCode = MyApplication.getPreferencesProvider().getPreviousDeveloperMessagesVersion();
        int currentVersionCode = ApkUtils.getApkVersionCode(getApplication());
        if (previousVersionCode != currentVersionCode) {
            MyApplication.getPreferencesProvider().setRecentDeveloperMessagesVersion(currentVersionCode);
            // skip recent changes for first startup
            if (MyApplication.getPreferencesProvider().getShowIntroduction()) {
                return;
            }
            Log.d("displayDevelopersMessages(): Showing changelog between %s and %s", previousVersionCode, currentVersionCode);
            ChangelogProvider provider = new ChangelogProvider(getApplication(), R.raw.changelog);
            ChangelogInfo changelog = provider.getChangelog(previousVersionCode);
            HtmlChangelogFormatter formatter = new HtmlChangelogFormatter();
            String message = formatter.formatChangelog(changelog);
            DialogManager.createHtmlInfoDialog(this, R.string.dialog_what_is_new, message, false, false).show();
        }
    }

    public void displayExportFinishedDialog() {
        showExportFinishedDialog = false;
        // show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.export_dialog_finished_title);
        builder.setMessage(getString(R.string.export_dialog_finished_message, exportedFileAbsolutePath));
        builder.setPositiveButton(R.string.dialog_keep, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // cancel
                MyApplication.getAnalytics().sendExportKeepAction();
            }
        });
        builder.setNeutralButton(R.string.dialog_upload, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyApplication.getAnalytics().sendExportUploadAction();
                startUploaderServiceWithCheck();
            }
        });
        builder.setNegativeButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MeasurementsDatabase.getInstance(MainActivity.this).deleteAllMeasurements();
                EventBus.getDefault().post(new PrintMainWindowEvent());
                MyApplication.getAnalytics().sendExportDeleteAction();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        exportedFileAbsolutePath = null;
    }

    // ========== MENU START/STOP METHODS ========== //

    private void startCollectorService() {
        String runningTaskClassName = MyApplication.getBackgroundTaskName();
        if (runningTaskClassName != null) {
            Log.d("startCollectorService(): Another task is running in background: %s", runningTaskClassName);
            backgroundTaskHelper.showTaskRunningMessage(runningTaskClassName);
            return;
        }
        askAndSetGpsEnabled();
        if (isGpsEnabled) {
            // checking for airplane mode
            boolean inAirplaneMode = displayInAirplaneModeDialog();
            if (!inAirplaneMode) {
                Log.d("startCollectorService(): Air plane mode off, starting service");
                // create intent
                final Intent intent = new Intent(this, CollectorService.class);
                // pass means of transport inside intent
                boolean gpsOptimizationsEnabled = MyApplication.getPreferencesProvider().getGpsOptimizationsEnabled();
                MeansOfTransport selectedType = (gpsOptimizationsEnabled ? MeansOfTransport.Universal : MeansOfTransport.Fixed);
                intent.putExtra(CollectorService.INTENT_KEY_TRANSPORT_MODE, selectedType);
                // pass screen on mode
                final String keepScreenOnMode = MyApplication.getPreferencesProvider().getCollectorKeepScreenOnMode();
                intent.putExtra(CollectorService.INTENT_KEY_KEEP_SCREEN_ON_MODE, keepScreenOnMode);
                // start service
                startService(intent);
                EventBus.getDefault().post(new CollectorStartedEvent(intent));
                MyApplication.getAnalytics().sendCollectorStarted(IntentSource.User);
            }
        }
    }

    private void stopCollectorService() {
        stopService(new Intent(this, CollectorService.class));
    }

    private void startUploaderServiceWithCheck() {
        String runningTaskClassName = MyApplication.getBackgroundTaskName();
        if (runningTaskClassName != null) {
            Log.d("startUploaderService(): Another task is running in background: %s", runningTaskClassName);
            backgroundTaskHelper.showTaskRunningMessage(runningTaskClassName);
            return;
        }
        // check API key
        String apiKey = MyApplication.getPreferencesProvider().getApiKey();
        if (!Validator.isOpenCellIdApiKeyValid(apiKey)) {
            final String apiKeyLocal = apiKey;
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.setCancelable(true);
            if (StringUtils.isNullEmptyOrWhitespace(apiKey)) {
                alertDialog.setTitle(R.string.main_dialog_api_key_empty_title);
                alertDialog.setMessage(getString(R.string.main_dialog_api_key_empty_message));
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_register), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.preferences_opencellid_org_sign_up_link)));
                            startActivity(browserIntent);
                        } catch (ActivityNotFoundException ex) {
                            Toast.makeText(getApplication(), R.string.web_browser_missing, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                alertDialog.setTitle(R.string.main_dialog_api_key_invalid_title);
                alertDialog.setMessage(getString(R.string.main_dialog_api_key_invalid_message));
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_upload), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startUploaderService(apiKeyLocal);
                    }
                });
            }
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.dialog_enter_api_key), new DialogInterface.OnClickListener() {
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
            return;
        } else {
            startUploaderService(apiKey);
        }
    }

    private void startUploaderService(String apiKey) {
        // start task
        if (!isServiceRunning(UploaderService.SERVICE_FULL_NAME)) {
            Intent intent = new Intent(MainActivity.this, UploaderService.class);
            intent.putExtra(UploaderService.INTENT_KEY_APIKEY, apiKey);
            startService(intent);
            MyApplication.getAnalytics().sendUploadStarted(IntentSource.User);
        } else
            Toast.makeText(getApplication(), R.string.uploader_already_running, Toast.LENGTH_LONG).show();
    }

    private void startExportAsyncTask() {
        String runningTaskClassName = MyApplication.getBackgroundTaskName();
        if (runningTaskClassName != null) {
            Log.d("startExportAsyncTask(): Another task is running in background: %s", runningTaskClassName);
            backgroundTaskHelper.showTaskRunningMessage(runningTaskClassName);
            return;
        }
        if (StorageUtils.isExternalMemoryWritable()) {
            final Map<String, FileType> fileTypes = getFileTypes();
            final String[] fileTypeNames = fileTypes.keySet().toArray(new String[fileTypes.size()]);
            // show dialog that runs async task
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.export_dialog_format_selection_title);
            builder.setItems(fileTypeNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int itemIndex) {
                    String selectedItem = fileTypeNames[itemIndex];
                    Log.d("onCreateDialog(): User selected position: %s", selectedItem);
                    // parse response
                    FileType selectedType = FileType.Unknown;
                    // pass selected means of transport
                    if (fileTypes.containsKey(selectedItem)) {
                        selectedType = fileTypes.get(selectedItem);
                    }
                    String extension;
                    switch (selectedType) {
                        case Csv:
                            extension = "csv";
                            break;
                        case Gpx:
                            extension = "gpx";
                            break;
                        default:
                            // cancel
                            extension = null;
                            break;
                    }
                    if (selectedType != FileType.Unknown) {
                        String path = FileUtils.combinePath(FileUtils.getExternalStorageAppDir(), FileUtils.getCurrentDateFilename(extension));
                        ExportFileAsyncTask task = new ExportFileAsyncTask(MainActivity.this, new InternalMessageHandler(MainActivity.this), path, selectedType);
                        task.execute(new Void[0]);
                        MyApplication.getAnalytics().sendExportStarted();
                    }
                }
            }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // cancel
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.show();
            //ExportFormatSelectionDialogFragment dialog = new ExportFormatSelectionDialogFragment();
            //dialog.setContext(this);
            //dialog.setHandler(new InternalMessageHandler(this));
            //dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
        } else if (StorageUtils.isExternalMemoryPresent()) {
            Toast.makeText(getApplication(), R.string.export_toast_storage_read_only, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplication(), R.string.export_toast_no_storage, Toast.LENGTH_LONG).show();
        }
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
            Log.w("askAndSetGpsEnabled(): Could not open Settings to change network type", ex);
            MyApplication.getAnalytics().sendException(ex, Boolean.FALSE);
            ACRA.getErrorReporter().handleSilentException(ex);
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setMessage(R.string.dialog_could_not_open_network_type_settings).setPositiveButton(R.string.dialog_ok, null).create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.setCancelable(true);
            alertDialog.show();
        }
    }

    private Intent createDataRoamingSettingsIntent() {
        Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        // Theoretically this is not needed starting from 4.0.1 but it sometimes fail
        // bug https://code.google.com/p/android/issues/detail?id=13368
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            ComponentName componentName = new ComponentName("com.android.phone", "com.android.phone.Settings");
            intent.setComponent(componentName);
        }
        return intent;
    }

    // ========== SERVICE CONNECTIONS ========== //

    private ServiceConnection collectorServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d("onServiceConnected(): Service connection created for %s", name);
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
            Log.d("onServiceDisconnected(): Service connection destroyed of %s", name);
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

    // ========== MISCELLANEOUS ========== //

    private boolean isServiceRunning(String serviceClassName) {
        ActivityManager activityManager = (ActivityManager) getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }

    private void askAndSetGpsEnabled() {
        if (GpsUtils.isGpsEnabled(getApplication())) {
            Log.d("askAndSetGpsEnabled(): GPS enabled");
            isGpsEnabled = true;
            showAskForLocationSettingsDialog = false;
        } else {
            Log.d("askAndSetGpsEnabled(): GPS disabled, asking user");
            isGpsEnabled = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_want_enable_gps).setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d("askAndSetGpsEnabled(): display settings");
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
                            Log.w("askAndSetGpsEnabled(): Could not open Settings to enable GPS");
                            MyApplication.getAnalytics().sendException(ex2, Boolean.FALSE);
                            ACRA.getErrorReporter().handleSilentException(ex2);
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setMessage(R.string.dialog_could_not_open_gps_settings).setPositiveButton(R.string.dialog_ok, null).create();
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
                    Log.d("askAndSetGpsEnabled(): cancel");
                    dialog.cancel();
                    if (GpsUtils.isGpsEnabled(getApplication())) {
                        Log.d("askAndSetGpsEnabled(): provider enabled in the meantime");
                        startCollectorService();
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
        if (isAutoUpdateEnabled) {
            long currentDate = DateUtils.getCurrentDateWithoutTime();
            long lastUpdateCheckDate = MyApplication.getPreferencesProvider().getLastUpdateCheckDate();
            long diffInDays = DateUtils.getTimeDiff(currentDate, lastUpdateCheckDate);
            Log.d("checkForNewVersionAvailability(): Last update check performed on: %s, diff to current in days: %s", new Date(lastUpdateCheckDate), diffInDays);
            // if currently is at least one day after last check (day, not 24 hrs)
            if (diffInDays >= 1) {
                // check if network available
                if (NetworkUtils.isNetworkAvailable(getApplication())) {
                    int currentVersion = ApkUtils.getApkVersionCode(getApplication());
                    String updateFeedUrl = String.format(BuildConfig.UPDATE_CHECK_FEED_URI, currentVersion);
                    if (!StringUtils.isNullEmptyOrWhitespace(updateFeedUrl)) {
                        UpdateCheckAsyncTask updateCheckTask = new UpdateCheckAsyncTask(getApplication(), currentVersion);
                        updateCheckTask.execute(updateFeedUrl);
                    }
                    MyApplication.getPreferencesProvider().setLastUpdateCheckDate(currentDate);
                } else {
                    Log.d("checkForNewVersionAvailability(): No active network connection");
                }
            }
        }
    }

    private void processOnStartIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(UploaderService.INTENT_KEY_RESULT_DESCRIPTION)) {
                // display upload result
                displayUploadResultDialog(extras);
            } else if (extras.containsKey(UpdateCheckAsyncTask.INTENT_KEY_UPDATE_INFO)) {
                // display dialog with download options
                displayNewVersionDownloadOptions(extras);
            }
        }
    }

    private Map<String, FileType> getFileTypes() {
        Log.d("getFileTypes(): Loading file types");
        final String[] resLabels = getResources().getStringArray(R.array.export_formats_labels);
        final String[] resValues = getResources().getStringArray(R.array.export_formats_values);
        Map<String, FileType> fileTypes = new LinkedHashMap<String, FileType>();
        for (int i = 0; i < resLabels.length; i++) {
            String label = resLabels[i];
            String value = resValues[i];
            fileTypes.put(label, FileType.valueOf(value));
        }
        return fileTypes;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CollectorStartedEvent event) {
        bindService(event.getIntent(), collectorServiceConnection, 0);
    }

    // ========== INNER OBJECTS ========== //

    public static class InternalMessageHandler extends Handler {
        public static final int EXPORT_FINISHED_UI_REFRESH = 0;

        private MainActivity mainActivity;

        public InternalMessageHandler(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EXPORT_FINISHED_UI_REFRESH:
                    mainActivity.exportedFileAbsolutePath = msg.getData().getString(ExportFileAsyncTask.ABSOLUTE_PATH);
                    if (!mainActivity.isMinimized)
                        mainActivity.displayExportFinishedDialog();
                    else
                        mainActivity.showExportFinishedDialog = true;
                    break;
            }
        }
    }
}
