/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.enums.GpsStatus;
import info.zamojski.soft.towercollector.enums.Validity;
import info.zamojski.soft.towercollector.events.GpsStatusChangedEvent;
import info.zamojski.soft.towercollector.events.SystemTimeChangedEvent;
import info.zamojski.soft.towercollector.utils.UnitConverter;

import android.support.v4.app.Fragment;
import trikita.log.Log;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

public abstract class MainFragmentBase extends Fragment {

    private static final String TAG = MainFragmentBase.class.getSimpleName();

    private TableRow gpsStatusTableRow;
    private TextView gpsStatusLabelTextView;
    private TextView gpsStatusValueTextView;

    private TableRow invalidSystemTimeTableRow;
    private TextView invalidSystemTimeValueTextView;

    protected boolean useImperialUnits;
    protected String preferredLengthUnit;

    protected SimpleDateFormat dateTimeFormatStandard;

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getTag(), "onResume(): Registering broadcast receiver");
        EventBus.getDefault().registerSticky(this);
        configureOnResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(getTag(), "onPause(): Unregistering broadcast receiver");
        EventBus.getDefault().unregister(this);
        configureOnPause();
    }

    protected void configureOnResume() {
    }

    protected void configureOnPause() {
    }

    protected void configureControls(View view) {
        gpsStatusTableRow = (TableRow) view.findViewById(R.id.main_gps_status_tablerow);
        gpsStatusLabelTextView = (TextView) view.findViewById(R.id.main_gps_status_label_textview);
        gpsStatusValueTextView = (TextView) view.findViewById(R.id.main_gps_status_value_textview);
        invalidSystemTimeTableRow = (TableRow) view.findViewById(R.id.main_invalid_system_time_tablerow);
        invalidSystemTimeValueTextView = (TextView) view.findViewById(R.id.main_invalid_system_time_value_textview);
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

    public void onEventMainThread(SystemTimeChangedEvent event) {
        showInvalidSystemTime(event.isValid() == Validity.Invalid);
    }

    private void showInvalidSystemTime(boolean show) {
        Log.d(TAG, "showInvalidSystemTime(): Setting invalid system time visible = " + show);
        invalidSystemTimeValueTextView.setTextColor(getResources().getColor(R.color.text_light));
        invalidSystemTimeTableRow.setBackgroundColor(getResources().getColor(R.color.background_needs_attention));
        invalidSystemTimeTableRow.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void onEventMainThread(GpsStatusChangedEvent event) {
        if (event.isEnabled()) {
            GpsStatus status = event.getStatus();
            float accuracy = event.getAccuracy();
            printGpsStatus(status, accuracy);
        } else {
            hideGpsStatus();
        }
    }

    private void printGpsStatus(GpsStatus status, float lastAccuracy) {
        Log.d(TAG, "printGpsStatus(): Showing status " + status + " and accuracy " + lastAccuracy);
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
            case Unknown:
            default:
                statusString = getString(R.string.status_unknown);
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
        Log.d(TAG, "hideGpsStatus(): Hiding status");
        gpsStatusTableRow.setVisibility(View.GONE);
    }

}
