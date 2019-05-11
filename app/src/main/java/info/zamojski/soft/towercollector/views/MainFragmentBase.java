/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.enums.GpsStatus;
import info.zamojski.soft.towercollector.enums.Validity;
import info.zamojski.soft.towercollector.events.AirplaneModeChangedEvent;
import info.zamojski.soft.towercollector.events.BatteryOptimizationsChangedEvent;
import info.zamojski.soft.towercollector.events.GpsStatusChangedEvent;
import info.zamojski.soft.towercollector.events.SystemTimeChangedEvent;
import info.zamojski.soft.towercollector.utils.BatteryUtils;
import info.zamojski.soft.towercollector.utils.NetworkUtils;
import info.zamojski.soft.towercollector.utils.UnitConverter;
import timber.log.Timber;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

public abstract class MainFragmentBase extends Fragment {

    private TableRow gpsStatusTableRow;
    private TextView gpsStatusLabelTextView;
    private TextView gpsStatusValueTextView;

    private TableRow invalidSystemTimeTableRow;
    private TextView invalidSystemTimeValueTextView;
    private TableRow batteryOptimizationsTableRow;
    private TextView batteryOptimizationsValueTextView;
    private TableRow airplaneModeTableRow;
    private TextView airplaneModeValueTextView;

    protected boolean useImperialUnits;
    protected String preferredLengthUnit;

    protected SimpleDateFormat dateTimeFormatStandard;

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
    }

    protected void configureOnPause() {
    }

    protected void configureControls(View view) {
        gpsStatusTableRow = view.findViewById(R.id.main_gps_status_tablerow);
        gpsStatusLabelTextView = view.findViewById(R.id.main_gps_status_label_textview);
        gpsStatusValueTextView = view.findViewById(R.id.main_gps_status_value_textview);
        invalidSystemTimeTableRow = view.findViewById(R.id.main_invalid_system_time_tablerow);
        invalidSystemTimeValueTextView = view.findViewById(R.id.main_invalid_system_time_value_textview);
        batteryOptimizationsTableRow = view.findViewById(R.id.main_battery_optimizations_tablerow);
        batteryOptimizationsValueTextView = view.findViewById(R.id.main_battery_optimizations_value_textview);
        boolean batteryOptimizationsEnabled = BatteryUtils.areBatteryOptimizationsEnabled(MyApplication.getApplication());
        showWarning(batteryOptimizationsTableRow, batteryOptimizationsValueTextView, batteryOptimizationsEnabled);
        airplaneModeTableRow = view.findViewById(R.id.main_airplane_mode_tablerow);
        airplaneModeValueTextView = view.findViewById(R.id.main_airplane_mode_value_textview);
        boolean airplaneModeEnabled = NetworkUtils.isInAirplaneMode(MyApplication.getApplication());
        showWarning(airplaneModeTableRow, airplaneModeValueTextView, airplaneModeEnabled);
        // reload preferences
        useImperialUnits = MyApplication.getPreferencesProvider().getUseImperialUnits();
        // cache units
        if (useImperialUnits) {
            // preferredSpeedUnit = getString(R.string.unit_speed_imperial);
            preferredLengthUnit = getString(R.string.unit_length_imperial);
        } else {
            // preferredSpeedUnit = getString(R.string.unit_speed_metric);
            preferredLengthUnit = getString(R.string.unit_length_metric);
        }
        // date format
        dateTimeFormatStandard = new SimpleDateFormat(getString(R.string.date_time_format_standard), new Locale(getString(R.string.locale)));
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
        if (event.isEnabled()) {
            GpsStatus status = event.getStatus();
            float accuracy = event.getAccuracy();
            printGpsStatus(status, accuracy);
        } else {
            hideGpsStatus();
        }
    }

    private void printGpsStatus(GpsStatus status, float lastAccuracy) {
        Timber.d("printGpsStatus(): Showing status %s and accuracy %s", status, lastAccuracy);
        String statusString;
        int textColorResId;
        int backgroundColorResId;
        switch (status) {
            case Ok:
                statusString = getString(R.string.status_ok);
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
}
