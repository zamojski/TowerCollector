/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Locale;

import info.zamojski.soft.towercollector.CollectorService;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.enums.GpsStatus;
import info.zamojski.soft.towercollector.enums.Validity;
import info.zamojski.soft.towercollector.events.AirplaneModeChangedEvent;
import info.zamojski.soft.towercollector.events.BatteryOptimizationsChangedEvent;
import info.zamojski.soft.towercollector.events.CollectorStateChangedEvent;
import info.zamojski.soft.towercollector.events.GpsStatusChangedEvent;
import info.zamojski.soft.towercollector.events.PowerSaveModeChangedEvent;
import info.zamojski.soft.towercollector.events.SystemTimeChangedEvent;
import info.zamojski.soft.towercollector.utils.BatteryUtils;
import info.zamojski.soft.towercollector.utils.NetworkUtils;
import info.zamojski.soft.towercollector.utils.UnitConverter;
import timber.log.Timber;

public abstract class MainFragmentBase extends Fragment {

    private TableRow gpsStatusTableRow;
    private TextView gpsStatusLabelTextView;
    private TextView gpsStatusValueTextView;

    private LinearLayout collectorStatusWrapperRow;
    private TextView collectorStatusValueTextView;

    private TableRow invalidSystemTimeTableRow;
    private TextView invalidSystemTimeValueTextView;
    private TableRow batteryOptimizationsTableRow;
    private TextView batteryOptimizationsValueTextView;
    private TableRow powerSaveModeTableRow;
    private TextView powerSaveModeValueTextView;
    private TableRow airplaneModeTableRow;
    private TextView airplaneModeValueTextView;

    protected boolean useImperialUnits;
    protected String preferredLengthUnit;

    protected SimpleDateFormat dateTimeFormatStandard;

    protected Locale locale;
    private Resources resourcesForLocale;

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume(): Registering broadcast receiver");
        EventBus.getDefault().register(this);
        configureOnResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause(): Unregistering broadcast receiver");
        EventBus.getDefault().unregister(this);
        configureOnPause();
    }

    protected void configureOnResume() {
        locale = new Locale(getString(R.string.locale));
        Configuration configuration = new Configuration(getContext().getResources().getConfiguration());
        configuration.setLocale(locale);
        resourcesForLocale = getContext().createConfigurationContext(configuration).getResources();

        boolean showCollectorStatusBarEnabled = MyApplication.getPreferencesProvider().isShowCollectorStatusBarEnabled();
        setCollectorStatusBarVisible(collectorStatusWrapperRow, showCollectorStatusBarEnabled);
    }

    protected void configureOnPause() {
    }

    protected void configureControls(View view) {
        gpsStatusTableRow = view.findViewById(R.id.main_gps_status_tablerow);
        gpsStatusLabelTextView = view.findViewById(R.id.main_gps_status_label_textview);
        gpsStatusValueTextView = view.findViewById(R.id.main_gps_status_value_textview);
        collectorStatusWrapperRow = view.findViewById(R.id.main_collector_status_wrapper_row);
        collectorStatusValueTextView = view.findViewById(R.id.main_collector_status_value_textview);
        boolean isCollectorServiceActive = MyApplication.isBackgroundTaskRunning(CollectorService.class);
        showCollectorStatus(collectorStatusWrapperRow, collectorStatusValueTextView, isCollectorServiceActive);
        invalidSystemTimeTableRow = view.findViewById(R.id.main_invalid_system_time_tablerow);
        invalidSystemTimeValueTextView = view.findViewById(R.id.main_invalid_system_time_value_textview);
        batteryOptimizationsTableRow = view.findViewById(R.id.main_battery_optimizations_tablerow);
        batteryOptimizationsValueTextView = view.findViewById(R.id.main_battery_optimizations_value_textview);
        boolean batteryOptimizationsEnabled = BatteryUtils.areBatteryOptimizationsEnabled(MyApplication.getApplication());
        showWarning(batteryOptimizationsTableRow, batteryOptimizationsValueTextView, batteryOptimizationsEnabled);
        powerSaveModeTableRow = view.findViewById(R.id.main_power_save_mode_tablerow);
        powerSaveModeValueTextView = view.findViewById(R.id.main_power_save_mode_value_textview);
        boolean powerSaveModeEnabled = BatteryUtils.isPowerSaveModeEnabled(MyApplication.getApplication());
        showWarning(powerSaveModeTableRow, powerSaveModeValueTextView, powerSaveModeEnabled);
        airplaneModeTableRow = view.findViewById(R.id.main_airplane_mode_tablerow);
        airplaneModeValueTextView = view.findViewById(R.id.main_airplane_mode_value_textview);
        boolean airplaneModeEnabled = NetworkUtils.isInAirplaneMode(MyApplication.getApplication());
        showWarning(airplaneModeTableRow, airplaneModeValueTextView, airplaneModeEnabled);
        // reload preferences
        useImperialUnits = MyApplication.getPreferencesProvider().getUseImperialUnits();
        // cache units
        preferredLengthUnit = getString(useImperialUnits ? R.string.unit_length_imperial : R.string.unit_length_metric);
        // date format
        dateTimeFormatStandard = new SimpleDateFormat(getString(R.string.date_time_format_standard), new Locale(getString(R.string.locale)));
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(CollectorStateChangedEvent event) {
        showCollectorStatus(collectorStatusWrapperRow, collectorStatusValueTextView, event.isActive());
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(SystemTimeChangedEvent event) {
        showWarning(invalidSystemTimeTableRow, invalidSystemTimeValueTextView, event.isValid() == Validity.Invalid);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(BatteryOptimizationsChangedEvent event) {
        showWarning(batteryOptimizationsTableRow, batteryOptimizationsValueTextView, event.isEnabled());
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(PowerSaveModeChangedEvent event) {
        showWarning(powerSaveModeTableRow, powerSaveModeValueTextView, event.isEnabled());
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(AirplaneModeChangedEvent event) {
        showWarning(airplaneModeTableRow, airplaneModeValueTextView, event.isEnabled());
    }

    private void showWarning(TableRow tableRow, TextView label, boolean show) {
        Timber.d("showWarning(): Setting warning visible = %s", show);
        label.setTextColor(getResources().getColor(R.color.text_light));
        tableRow.setBackgroundColor(getResources().getColor(R.color.background_needs_attention));
        tableRow.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(GpsStatusChangedEvent event) {
        if (event.isActive()) {
            GpsStatus status = event.getStatus();
            float accuracy = event.getAccuracy();
            printGpsStatus(status, accuracy);
        } else {
            hideGpsStatus();
        }
    }

    protected String getStringForLocale(@StringRes int resId) {
        return resourcesForLocale.getString(resId);
    }

    private void printGpsStatus(GpsStatus status, float lastAccuracy) {
        Timber.d("printGpsStatus(): Showing status %s and accuracy %s", status, lastAccuracy);
        String statusString;
        int textColorResId;
        int backgroundColorResId;
        switch (status) {
            case Ok:
                statusString = getString(R.string.status_ok, (useImperialUnits ? UnitConverter.convertMetersToFeet(lastAccuracy) : lastAccuracy), preferredLengthUnit);
                textColorResId = R.color.text_dark;
                backgroundColorResId = R.color.background_valid;
                break;
            case LowAccuracy:
                statusString = getString(R.string.status_low_gps_accuracy, (useImperialUnits ? UnitConverter.convertMetersToFeet(lastAccuracy) : lastAccuracy), preferredLengthUnit);
                textColorResId = R.color.text_dark;
                backgroundColorResId = R.color.background_invalid;
                break;
            case NoLocation:
                statusString = getString(R.string.status_no_gps_location);
                textColorResId = R.color.text_dark;
                backgroundColorResId = R.color.background_invalid;
                break;
            case Disabled:
                statusString = getString(R.string.status_disabled);
                textColorResId = R.color.text_dark;
                backgroundColorResId = R.color.background_invalid;
                break;
            case Initializing:
            default:
                statusString = getString(R.string.status_initializing);
                textColorResId = R.color.text_light;
                backgroundColorResId = R.color.background_needs_attention;
                break;
        }
        int textColor = getResources().getColor(textColorResId);
        int backgroundColor = getResources().getColor(backgroundColorResId);
        gpsStatusValueTextView.setText(statusString);
        gpsStatusValueTextView.setTextColor(textColor);
        gpsStatusLabelTextView.setTextColor(textColor);
        gpsStatusTableRow.setBackgroundColor(backgroundColor);
        gpsStatusTableRow.setVisibility(View.VISIBLE);
    }

    private void hideGpsStatus() {
        Timber.d("hideGpsStatus(): Hiding status");
        gpsStatusTableRow.setVisibility(View.GONE);
    }

    private void setCollectorStatusBarVisible(LinearLayout wrapperRow, boolean show) {
        Timber.d("setCollectorStatusBarVisible(): Setting status bar visible = %s", show);
        wrapperRow.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showCollectorStatus(LinearLayout wrapperRow, TextView value, boolean active) {
        Timber.d("showCollectorStatus(): Setting status active = %s", active);
        wrapperRow.setBackgroundColor(active ? getResources().getColor(R.color.background_valid) : getResources().getColor(R.color.background_needs_attention));
        value.setTextColor(getResources().getColor(active ? R.color.text_dark : R.color.text_light));
        value.setText(active ? R.string.collector_status_active : R.string.collector_status_inactive);
    }
}
