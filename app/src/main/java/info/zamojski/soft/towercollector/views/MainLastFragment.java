/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import java.util.Date;
import java.util.Locale;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;
import info.zamojski.soft.towercollector.utils.UnitConverter;

import android.os.Bundle;
import trikita.log.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainLastFragment extends MainFragmentBase {

    private static final String TAG = MainLastFragment.class.getSimpleName();

    private TextView lastNetworkTypeValueTextView;
    private TextView lastCellIdValueTextView;
    private TextView lastMccValueTextView;
    private TextView lastMncValueTextView;
    private TextView lastLacValueTextView;
    private TextView lastSignalStrengthValueTextView;
    private TextView lastLatitudeValueTextView;
    private TextView lastLongitudeValueTextView;
    private TextView lastGpsAccuracyValueTextView;
    private TextView lastDateTimeValueTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_last_fragment, container, false);
        configureControls(rootView);
        return rootView;
    }

    @Override
    protected void configureOnResume() {
        super.configureOnResume();
        getAndPrintOrClearMeasurement();
    }

    @Override
    protected void configureControls(View view) {
        super.configureControls(view);
        lastNetworkTypeValueTextView = (TextView) view.findViewById(R.id.main_last_network_type_value_textview);
        lastCellIdValueTextView = (TextView) view.findViewById(R.id.main_last_cell_id_value_textview);
        lastMccValueTextView = (TextView) view.findViewById(R.id.main_last_mcc_value_textview);
        lastMncValueTextView = (TextView) view.findViewById(R.id.main_last_mnc_value_textview);
        lastLacValueTextView = (TextView) view.findViewById(R.id.main_last_lac_value_textview);
        lastSignalStrengthValueTextView = (TextView) view.findViewById(R.id.main_last_signal_strength_value_textview);
        lastLatitudeValueTextView = (TextView) view.findViewById(R.id.main_last_latitude_value_textview);
        lastLongitudeValueTextView = (TextView) view.findViewById(R.id.main_last_longitude_value_textview);
        lastGpsAccuracyValueTextView = (TextView) view.findViewById(R.id.main_last_gps_accuracy_value_textview);
        lastDateTimeValueTextView = (TextView) view.findViewById(R.id.main_last_date_time_value_textview);
    }

    public void onEventMainThread(MeasurementSavedEvent event) {
        Measurement measurement = event.getMeasurement();
        printOrClearMeasurement(measurement);
    }

    public void onEventMainThread(PrintMainWindowEvent event) {
        getAndPrintOrClearMeasurement();
    }

    private void getAndPrintOrClearMeasurement() {
        Measurement measurement = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getLastMeasurement();
        printOrClearMeasurement(measurement);
    }

    private void printOrClearMeasurement(Measurement measurement) {
        if (measurement != null) {
            printMeasurement(measurement);
        } else {
            clearMeasurement();
        }
    }

    private void printMeasurement(Measurement measurement) {
        Log.d("printMeasurement(): Printing last measurement %s", measurement);
        int networkNameId = NetworkTypeUtils.getNetworkGroupNameResId(measurement.getNetworkType());
        lastNetworkTypeValueTextView.setText(getString(networkNameId));
        lastCellIdValueTextView.setText(String.valueOf(measurement.getCid()));
        lastMccValueTextView.setText((measurement.getMcc() != Measurement.UNKNOWN_CID ? String.valueOf(measurement.getMcc()) : ""));
        lastMncValueTextView.setText(String.valueOf(measurement.getMnc()));
        lastLacValueTextView.setText(String.valueOf(measurement.getLac()));
        if (measurement.getDbm() != Measurement.UNKNOWN_SIGNAL) {
            lastSignalStrengthValueTextView.setText(getString(R.string.main_last_signal_strength_value, measurement.getDbm()));
        } else {
            lastSignalStrengthValueTextView.setText(getString(R.string.main_signal_strength_not_available));
        }
        lastLatitudeValueTextView.setText(String.format(new Locale(getString(R.string.locale)), getString(R.string.main_last_latitude_value), measurement.getLatitude()));
        lastLongitudeValueTextView.setText(String.format(new Locale(getString(R.string.locale)), getString(R.string.main_last_longitude_value), measurement.getLongitude()));
        if (measurement.getGpsAccuracy() != Measurement.GPS_VALUE_NOT_AVAILABLE)
            lastGpsAccuracyValueTextView.setText(String.format(new Locale(getString(R.string.locale)), getString(R.string.main_last_gps_accuracy_value),
                    (useImperialUnits ? UnitConverter.convertMetersToFeet(measurement.getGpsAccuracy()) : measurement.getGpsAccuracy()), preferredLengthUnit));
        else
            lastGpsAccuracyValueTextView.setText(getString(R.string.main_gps_accuracy_not_available));
        lastDateTimeValueTextView.setText(dateTimeFormatStandard.format(new Date(measurement.getTimestamp())));
    }

    private void clearMeasurement() {
        Log.d("clearMeasurement(): Clearing last measurement");
        lastNetworkTypeValueTextView.setText("");
        lastCellIdValueTextView.setText("");
        lastMccValueTextView.setText("");
        lastMncValueTextView.setText("");
        lastLacValueTextView.setText("");
        lastSignalStrengthValueTextView.setText("");
        lastLatitudeValueTextView.setText("");
        lastLongitudeValueTextView.setText("");
        lastGpsAccuracyValueTextView.setText("");
        lastDateTimeValueTextView.setText("");
    }

}
