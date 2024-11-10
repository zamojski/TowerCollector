/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import info.zamojski.soft.towercollector.CollectorService;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.utils.GpsUtils;
import info.zamojski.soft.towercollector.utils.PermissionUtils;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

@RuntimePermissions
public class CollectorPreferenceFragment extends DialogEnabledPreferenceFragment implements OnSharedPreferenceChangeListener {

    private ListPreference collectorKeepScreenOnPreference;
    private ListPreference collectorLowBatteryActionPreference;
    private SwitchPreferenceCompat startCollectorAtBootPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_collector);

        collectorKeepScreenOnPreference = findPreference(getString(R.string.preferences_collector_keep_screen_on_mode_key));
        collectorLowBatteryActionPreference = findPreference(getString(R.string.preferences_collector_low_battery_action_key));
        startCollectorAtBootPreference = findPreference(getString(R.string.preferences_start_collector_at_boot_key));

        setupCollectorAtBootPreference();
        setupNeighboringCellsDialog();
        setupCollectorKeepScreenOnDialog();
        setupNotifyMeasurementsCollectedDialog();

        setupHideCollectorNotificationAvailability();
    }

    private void setupCollectorAtBootPreference() {
        startCollectorAtBootPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.equals(false)) {
                    Timber.d("onStartCollectorAtBootChangeListener(): Disabling start at boot");
                } else {
                    Timber.d("onStartCollectorAtBootChangeListener(): Requesting permission");
                    // For R+ apps, background permissions must be requested after foreground permissions are already granted
                    if (GpsUtils.isBackgroundLocationPermissionHidden()
                            && !PermissionUtils.hasPermissions(MyApplication.getApplication(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                        CollectorPreferenceFragmentPermissionsDispatcher.requestStartAtBootWithPermissionCheck(CollectorPreferenceFragment.this);
                        return true;
                    }
                    // NOTE: delegate the permission handling to generated method
                    if (GpsUtils.isBackgroundLocationAware()) {
                        if (PermissionUtils.isNotificationPermissionRequired()) {
                            CollectorPreferenceFragmentPermissionsDispatcher.requestStartAtBootApi33WithPermissionCheck(CollectorPreferenceFragment.this);
                        } else {
                            CollectorPreferenceFragmentPermissionsDispatcher.requestStartAtBootApi29WithPermissionCheck(CollectorPreferenceFragment.this);
                        }
                    } else {
                        CollectorPreferenceFragmentPermissionsDispatcher.requestStartAtBootWithPermissionCheck(CollectorPreferenceFragment.this);
                    }
                }
                return true;
            }
        });
    }

    private void setupHideCollectorNotificationAvailability() {
        boolean available = Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
        if (!available) {
            PreferenceCategory settingsCategoryPreference = findPreference(getString(R.string.preferences_general_category_settings_key));
            SwitchPreferenceCompat hideCollectorNotificationPreference = findPreference(getString(R.string.preferences_hide_collector_notification_key));
            settingsCategoryPreference.removePreference(hideCollectorNotificationPreference);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        // set summaries
        setupListPreferenceSummary(collectorKeepScreenOnPreference, R.string.preferences_collector_keep_screen_on_summary);
        setupListPreferenceSummary(collectorLowBatteryActionPreference, R.string.preferences_collector_low_battery_action_summary);
        if (startCollectorAtBootPreference.isChecked() && GpsUtils.isBackgroundLocationAware() && !PermissionUtils.hasPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            startCollectorAtBootPreference.setChecked(false);
            if (GpsUtils.isBackgroundLocationPermissionHidden()) {
                if (GpsUtils.isBackgroundLocationAware()) {
                    if (PermissionUtils.isNotificationPermissionRequired()) {
                        CollectorPreferenceFragmentPermissionsDispatcher.requestStartAtBootApi33WithPermissionCheck(CollectorPreferenceFragment.this);
                    } else {
                        CollectorPreferenceFragmentPermissionsDispatcher.requestStartAtBootApi29WithPermissionCheck(CollectorPreferenceFragment.this);
                    }
                }
            } else {
                Toast.makeText(getActivity(), R.string.permission_collector_denied_background_location_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.preferences_gps_optimizations_enabled_key).equals(key)
                || getString(R.string.preferences_collect_neighboring_cells_key).equals(key)
                || getString(R.string.preferences_notify_measurements_collected_key).equals(key)
                || getString(R.string.preferences_hide_collector_notification_key).equals(key)) {
            if (MyApplication.isBackgroundTaskRunning(CollectorService.class)) {
                Toast.makeText(getActivity(), R.string.preferences_restart_collector, Toast.LENGTH_SHORT).show();
            }
        } else if (getString(R.string.preferences_collector_keep_screen_on_mode_key).equals(key)) {
            String collectorKeepScreenOnValue = collectorKeepScreenOnPreference.getValue();
            CharSequence collectorKeepScreenOnLabel = collectorKeepScreenOnPreference.getEntry();
            Timber.d("onSharedPreferenceChanged(): User set keep screen on = \"%s\"", collectorKeepScreenOnValue);
            collectorKeepScreenOnPreference.setSummary(formatValueString(R.string.preferences_collector_keep_screen_on_summary, collectorKeepScreenOnLabel));
            if (MyApplication.isBackgroundTaskRunning(CollectorService.class)) {
                Toast.makeText(getActivity(), R.string.preferences_restart_collector, Toast.LENGTH_SHORT).show();
            }
        } else if (getString(R.string.preferences_collector_low_battery_action_key).equals(key)) {
            String collectorLowBatteryActionValue = collectorLowBatteryActionPreference.getValue();
            CharSequence collectorLowBatteryActionLabel = collectorLowBatteryActionPreference.getEntry();
            Timber.d("onSharedPreferenceChanged(): User set low battery action = \"%s\"", collectorLowBatteryActionValue);
            collectorLowBatteryActionPreference.setSummary(formatValueString(R.string.preferences_collector_low_battery_action_summary, collectorLowBatteryActionLabel));
        }
    }

    private void setupNeighboringCellsDialog() {
        setupDialog(R.string.preferences_about_neighboring_cells_key, R.string.info_about_neighboring_cells_title, R.raw.info_about_neighboring_cells_content);
    }

    private void setupNotifyMeasurementsCollectedDialog() {
        setupDialog(R.string.preferences_about_collector_keep_screen_on_key, R.string.info_about_collector_keep_screen_on_title, R.raw.info_about_collector_keep_screen_on_content);
    }

    private void setupCollectorKeepScreenOnDialog() {
        setupDialog(R.string.preferences_about_notify_measurements_collected_key, R.string.info_about_notify_measurements_collected_title, R.raw.info_about_notify_measurements_collected_content, true);
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void requestStartAtBootApi33() {
        requestStartAtBootInternal();
    }

    @TargetApi(Build.VERSION_CODES.Q)
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void requestStartAtBootApi29() {
        requestStartAtBootInternal();
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void requestStartAtBoot() {
        requestStartAtBootInternal();
    }

    private void requestStartAtBootInternal() {
        Timber.d("requestStartAtBootInternal(): Called");
        // Do nothing but request needed permissions so the service can be started by system
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @OnShowRationale({Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartAtBootRationaleApi33(final PermissionRequest request) {
        onStartAtBootRationaleInternal(request);
    }

    @TargetApi(Build.VERSION_CODES.Q)
    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartAtBootRationaleApi29(final PermissionRequest request) {
        onStartAtBootRationaleInternal(request);
    }

    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartAtBootRationale(final PermissionRequest request) {
        onStartAtBootRationaleInternal(request);
    }

    private void onStartAtBootRationaleInternal(PermissionRequest request) {
        String message = getString(GpsUtils.isPreciseLocationAware() ? R.string.permission_collector_rationale_api31_message : R.string.permission_collector_rationale_message);
        if (GpsUtils.isBackgroundLocationAware()) {
            message += "\n\n" + getString(GpsUtils.isBackgroundLocationPermissionHidden()
                    ? (!PermissionUtils.hasPermission(MyApplication.getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) ? R.string.permission_collector_rationale_background_location_2_step_api30_message : R.string.permission_collector_rationale_background_location_api30_message)
                    : R.string.permission_collector_rationale_background_location_api29_message);
        }
        new AlertDialog.Builder(getActivity())
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

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @OnPermissionDenied({Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartAtBootPermissionDeniedApi33() {
        onStartAtBootPermissionDeniedInternal();
    }

    @TargetApi(Build.VERSION_CODES.Q)
    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartAtBootPermissionDeniedApi29() {
        onStartAtBootPermissionDeniedInternal();
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartAtBootPermissionDenied() {
        onStartAtBootPermissionDeniedInternal();
    }

    private void onStartAtBootPermissionDeniedInternal() {
        startCollectorAtBootPreference.setChecked(false);
        Toast.makeText(getActivity(), R.string.permission_collector_denied_message, Toast.LENGTH_LONG).show();
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @OnNeverAskAgain({Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartAtBootNeverAskAgainApi33() {
        onStartAtBootNeverAskAgainInternal();
    }

    @TargetApi(Build.VERSION_CODES.Q)
    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartAtBootNeverAskAgainApi29() {
        onStartAtBootNeverAskAgainInternal();
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    void onStartAtBootNeverAskAgain() {
        onStartAtBootNeverAskAgainInternal();
    }

    void onStartAtBootNeverAskAgainInternal() {
        startCollectorAtBootPreference.setChecked(false);
        String message = getString(GpsUtils.isBackgroundLocationAware() ? R.string.permission_collector_never_ask_again_background_location_api29_message : R.string.permission_collector_never_ask_again_message);
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.permission_denied)
                .setMessage(message)
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        CollectorPreferenceFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
