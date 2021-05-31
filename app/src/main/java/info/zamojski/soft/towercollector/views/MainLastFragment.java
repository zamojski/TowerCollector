/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import java.util.Date;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;
import info.zamojski.soft.towercollector.utils.UnitConverter;
import timber.log.Timber;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainLastFragment extends MainFragmentBase {

    private ViewGroup lastCellHasDataViewGroup;
    private ViewGroup lastCellHasNoDataViewGroup;

    private CardView lastCellCardView2;

    private TableRow lastLongCellIdValueTableRow1;
    private TableRow lastCellIdRncValueTableRow1;
    private TableRow lastCellIdValueTableRow1;

    private TextView lastNetworkTypeValueTextView1;
    private TextView lastLongCellIdValueTextView1;
    private TextView lastCellIdRncValueTextView1;
    private TextView lastCellIdValueTextView1;
    private TextView lastMccValueTextView1;
    private TextView lastMncValueTextView1;
    private TextView lastLacValueTextView1;
    private TextView lastSignalStrengthValueTextView1;

    private TableRow lastLongCellIdValueTableRow2;
    private TableRow lastCellIdRncValueTableRow2;
    private TableRow lastCellIdValueTableRow2;

    private TextView lastNetworkTypeValueTextView2;
    private TextView lastLongCellIdValueTextView2;
    private TextView lastCellIdRncValueTextView2;
    private TextView lastCellIdValueTextView2;
    private TextView lastMccValueTextView2;
    private TextView lastMncValueTextView2;
    private TextView lastLacValueTextView2;
    private TextView lastSignalStrengthValueTextView2;

    private TextView lastNumberOfCellsValueTextView;
    private TextView lastLatitudeValueTextView;
    private TextView lastLongitudeValueTextView;
    private TextView lastGpsAccuracyValueTextView;
    private TextView lastDateTimeValueTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        lastCellHasDataViewGroup = view.findViewById(R.id.main_last_cell_has_data);
        lastCellHasNoDataViewGroup = view.findViewById(R.id.main_last_cell_has_no_data);

        lastCellCardView2 = view.findViewById(R.id.main_last_cell_cardview2);

        lastLongCellIdValueTableRow1 = view.findViewById(R.id.main_last_long_cell_id_tablerow1);
        lastCellIdRncValueTableRow1 = view.findViewById(R.id.main_last_cell_id_rnc_tablerow1);
        lastCellIdValueTableRow1 = view.findViewById(R.id.main_last_cell_id_tablerow1);
        lastNetworkTypeValueTextView1 = view.findViewById(R.id.main_last_network_type_value_textview1);
        lastLongCellIdValueTextView1 = view.findViewById(R.id.main_last_long_cell_id_value_textview1);
        lastCellIdRncValueTextView1 = view.findViewById(R.id.main_last_cell_id_rnc_value_textview1);
        lastCellIdValueTextView1 = view.findViewById(R.id.main_last_cell_id_value_textview1);
        lastMccValueTextView1 = view.findViewById(R.id.main_last_mcc_value_textview1);
        lastMncValueTextView1 = view.findViewById(R.id.main_last_mnc_value_textview1);
        lastLacValueTextView1 = view.findViewById(R.id.main_last_lac_value_textview1);
        lastSignalStrengthValueTextView1 = view.findViewById(R.id.main_last_signal_strength_value_textview1);

        lastLongCellIdValueTableRow2 = view.findViewById(R.id.main_last_long_cell_id_tablerow2);
        lastCellIdRncValueTableRow2 = view.findViewById(R.id.main_last_cell_id_rnc_tablerow2);
        lastCellIdValueTableRow2 = view.findViewById(R.id.main_last_cell_id_tablerow2);
        lastNetworkTypeValueTextView2 = view.findViewById(R.id.main_last_network_type_value_textview2);
        lastLongCellIdValueTextView2 = view.findViewById(R.id.main_last_long_cell_id_value_textview2);
        lastCellIdRncValueTextView2 = view.findViewById(R.id.main_last_cell_id_rnc_value_textview2);
        lastCellIdValueTextView2 = view.findViewById(R.id.main_last_cell_id_value_textview2);
        lastMccValueTextView2 = view.findViewById(R.id.main_last_mcc_value_textview2);
        lastMncValueTextView2 = view.findViewById(R.id.main_last_mnc_value_textview2);
        lastLacValueTextView2 = view.findViewById(R.id.main_last_lac_value_textview2);
        lastSignalStrengthValueTextView2 = view.findViewById(R.id.main_last_signal_strength_value_textview2);

        lastNumberOfCellsValueTextView = view.findViewById(R.id.main_last_number_of_cells_value_textview);
        lastLatitudeValueTextView = view.findViewById(R.id.main_last_latitude_value_textview);
        lastLongitudeValueTextView = view.findViewById(R.id.main_last_longitude_value_textview);
        lastGpsAccuracyValueTextView = view.findViewById(R.id.main_last_gps_accuracy_value_textview);
        lastDateTimeValueTextView = view.findViewById(R.id.main_last_date_time_value_textview);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeasurementSavedEvent event) {
        Measurement measurement = event.getMeasurement();
        printOrClearMeasurement(measurement);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PrintMainWindowEvent event) {
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
        Timber.d("printMeasurement(): Printing last measurement %s", measurement);
        int neighboringCellsCount = measurement.getNeighboringCellsCount();
        int mainCellsCount = measurement.getMainCells().size();

        lastCellHasDataViewGroup.setVisibility(View.VISIBLE);
        lastCellHasNoDataViewGroup.setVisibility(View.GONE);

        {
            Cell mainCell = measurement.getMainCells().get(0);
            printCell(mainCell, lastNetworkTypeValueTextView1, lastLongCellIdValueTableRow1, lastCellIdRncValueTableRow1, lastCellIdValueTableRow1, lastLongCellIdValueTextView1, lastCellIdRncValueTextView1, lastCellIdValueTextView1, lastLacValueTextView1, lastMccValueTextView1, lastMncValueTextView1, lastSignalStrengthValueTextView1);
        }

        if (measurement.getMainCells().size() > 1) {
            Cell mainCell = measurement.getMainCells().get(1);
            printCell(mainCell, lastNetworkTypeValueTextView2, lastLongCellIdValueTableRow2, lastCellIdRncValueTableRow2, lastCellIdValueTableRow2, lastLongCellIdValueTextView2, lastCellIdRncValueTextView2, lastCellIdValueTextView2, lastLacValueTextView2, lastMccValueTextView2, lastMncValueTextView2, lastSignalStrengthValueTextView2);
            lastCellCardView2.setVisibility(View.VISIBLE);
        } else {
            lastCellCardView2.setVisibility(View.GONE);
        }

        lastNumberOfCellsValueTextView.setText(String.format(locale, getStringForLocale(R.string.main_last_number_of_cells_value), mainCellsCount, neighboringCellsCount));
        lastLatitudeValueTextView.setText(String.format(locale, getStringForLocale(R.string.main_last_latitude_value), measurement.getLatitude()));
        lastLongitudeValueTextView.setText(String.format(locale, getStringForLocale(R.string.main_last_longitude_value), measurement.getLongitude()));
        if (measurement.getGpsAccuracy() != Measurement.GPS_VALUE_NOT_AVAILABLE)
            lastGpsAccuracyValueTextView.setText(String.format(locale, getStringForLocale(R.string.main_last_gps_accuracy_value),
                    (useImperialUnits ? UnitConverter.convertMetersToFeet(measurement.getGpsAccuracy()) : measurement.getGpsAccuracy()), preferredLengthUnit));
        else
            lastGpsAccuracyValueTextView.setText(getStringForLocale(R.string.main_gps_accuracy_not_available));
        lastDateTimeValueTextView.setText(dateTimeFormatStandard.format(new Date(measurement.getMeasuredAt())));
    }

    private void printCell(Cell mainCell, TextView lastNetworkTypeValueTextView, TableRow lastLongCellIdValueTableRow, TableRow lastCellIdRncValueTableRow, TableRow lastCellIdValueTableRow, TextView lastLongCellIdValueTextView, TextView lastCellIdRncValueTextView, TextView lastCellIdValueTextView, TextView lastLacValueTextView, TextView lastMccValueTextView, TextView lastMncValueTextView, TextView lastSignalStrengthValueTextView) {
        int networkNameId = NetworkTypeUtils.getNetworkGroupNameResId(mainCell.getNetworkType());
        lastNetworkTypeValueTextView.setText(getStringForLocale(networkNameId));
        if (mainCell.isCidLong()) {
            lastLongCellIdValueTableRow.setVisibility(View.VISIBLE);
            lastCellIdRncValueTableRow.setVisibility(View.VISIBLE);
            lastCellIdValueTableRow.setVisibility(View.GONE);
        } else {
            lastLongCellIdValueTableRow.setVisibility(View.GONE);
            lastCellIdRncValueTableRow.setVisibility(View.GONE);
            lastCellIdValueTableRow.setVisibility(View.VISIBLE);
        }
        lastLongCellIdValueTextView.setText(String.format(locale, "%d", mainCell.getLongCid()));
        lastCellIdRncValueTextView.setText(String.format(locale, getStringForLocale(R.string.main_last_cell_id_rnc_value), mainCell.getShortCid(), mainCell.getRnc()));
        lastCellIdValueTextView.setText(String.format(locale, "%d", mainCell.getCid()));
        lastLacValueTextView.setText(String.format(locale, "%d", mainCell.getLac()));
        lastMccValueTextView.setText((mainCell.getMcc() != Cell.UNKNOWN_CID ? String.format(locale, "%d", mainCell.getMcc()) : ""));
        lastMncValueTextView.setText(String.format(locale, "%d", mainCell.getMnc()));
        if (mainCell.getDbm() != Cell.UNKNOWN_SIGNAL) {
            lastSignalStrengthValueTextView.setText(String.format(locale, getStringForLocale(R.string.main_last_signal_strength_value), mainCell.getDbm()));
        } else {
            lastSignalStrengthValueTextView.setText(getStringForLocale(R.string.main_signal_strength_not_available));
        }
    }

    private void clearMeasurement() {
        Timber.d("clearMeasurement(): Clearing last measurement");
        lastCellHasDataViewGroup.setVisibility(View.GONE);
        lastCellHasNoDataViewGroup.setVisibility(View.VISIBLE);

        clearCell(lastNetworkTypeValueTextView1, lastLongCellIdValueTextView1, lastCellIdRncValueTextView1, lastCellIdValueTextView1, lastLacValueTextView1, lastMccValueTextView1, lastMncValueTextView1, lastSignalStrengthValueTextView1);
        clearCell(lastNetworkTypeValueTextView2, lastLongCellIdValueTextView2, lastCellIdRncValueTextView2, lastCellIdValueTextView2, lastLacValueTextView2, lastMccValueTextView2, lastMncValueTextView2, lastSignalStrengthValueTextView2);

        lastNumberOfCellsValueTextView.setText("");
        lastLatitudeValueTextView.setText("");
        lastLongitudeValueTextView.setText("");
        lastGpsAccuracyValueTextView.setText("");
        lastDateTimeValueTextView.setText("");
    }

    private void clearCell(TextView lastNetworkTypeValueTextView, TextView lastLongCellIdValueTextView, TextView lastCellIdRncValueTextView, TextView lastCellIdValueTextView, TextView lastLacValueTextView, TextView lastMccValueTextView, TextView lastMncValueTextView, TextView lastSignalStrengthValueTextView) {
        lastNetworkTypeValueTextView.setText("");
        lastLongCellIdValueTextView.setText("");
        lastCellIdRncValueTextView.setText("");
        lastCellIdValueTextView.setText("");
        lastLacValueTextView.setText("");
        lastMccValueTextView.setText("");
        lastMncValueTextView.setText("");
        lastSignalStrengthValueTextView.setText("");
    }
}
