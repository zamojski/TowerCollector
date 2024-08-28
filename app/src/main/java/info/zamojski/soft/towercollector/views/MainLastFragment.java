/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;
import info.zamojski.soft.towercollector.utils.UnitConverter;
import timber.log.Timber;

public class MainLastFragment extends MainFragmentBase implements View.OnLongClickListener {

    private ViewGroup lastCellHasDataViewGroup;
    private ViewGroup lastCellHasNoDataViewGroup;

    private CardView lastCellCardView2;

    private TableRow lastMncTableRow1;
    private TableRow lastLacTableRow1;
    private TableRow lastLongCellIdTableRow1;
    private TableRow lastCellIdRncTableRow1;
    private TableRow lastCellIdTableRow1;

    private TextView lastNetworkTypeValueTextView1;
    private TextView lastLongCellIdValueTextView1;
    private TextView lastCellIdRncValueTextView1;
    private TextView lastCellIdValueTextView1;
    private TextView lastMccValueTextView1;
    private TextView lastMncValueTextView1;
    private TextView lastLacValueTextView1;
    private TextView lastSignalStrengthValueTextView1;

    private TableRow lastMccTableRow1;
    private TextView lastMncLabelTextView1;
    private TextView lastLacLabelTextView1;
    private TextView lastCellIdLabelTextView1;

    private TableRow lastMncTableRow2;
    private TableRow lastLacTableRow2;
    private TableRow lastLongCellIdTableRow2;
    private TableRow lastCellIdRncTableRow2;
    private TableRow lastCellIdTableRow2;

    private TextView lastNetworkTypeValueTextView2;
    private TextView lastLongCellIdValueTextView2;
    private TextView lastCellIdRncValueTextView2;
    private TextView lastCellIdValueTextView2;
    private TextView lastMccValueTextView2;
    private TextView lastMncValueTextView2;
    private TextView lastLacValueTextView2;
    private TextView lastSignalStrengthValueTextView2;

    private TableRow lastMccTableRow2;
    private TextView lastMncLabelTextView2;
    private TextView lastLacLabelTextView2;
    private TextView lastCellIdLabelTextView2;

    private TextView lastNumberOfCellsValueTextView;
    private TextView lastLatitudeValueTextView;
    private TextView lastLongitudeValueTextView;
    private TextView lastGpsAccuracyValueTextView;
    private TextView lastDateTimeValueTextView;

    private ClipboardManager clipboardManager;

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

        lastMccTableRow1 = view.findViewById(R.id.main_last_mcc_tablerow1);
        lastMncTableRow1 = view.findViewById(R.id.main_last_mnc_tablerow1);
        lastLacTableRow1 = view.findViewById(R.id.main_last_lac_tablerow1);
        lastLongCellIdTableRow1 = view.findViewById(R.id.main_last_long_cell_id_tablerow1);
        lastCellIdRncTableRow1 = view.findViewById(R.id.main_last_cell_id_rnc_tablerow1);
        lastCellIdTableRow1 = view.findViewById(R.id.main_last_cell_id_tablerow1);

        lastNetworkTypeValueTextView1 = view.findViewById(R.id.main_last_network_type_value_textview1);
        lastNetworkTypeValueTextView1.setOnLongClickListener(this);
        lastLongCellIdValueTextView1 = view.findViewById(R.id.main_last_long_cell_id_value_textview1);
        lastLongCellIdValueTextView1.setOnLongClickListener(this);
        lastCellIdRncValueTextView1 = view.findViewById(R.id.main_last_cell_id_rnc_value_textview1);
        lastCellIdRncValueTextView1.setOnLongClickListener(this);
        lastCellIdLabelTextView1 = view.findViewById(R.id.main_last_cell_id_label_textview1);
        lastCellIdLabelTextView1.setOnLongClickListener(this);
        lastCellIdValueTextView1 = view.findViewById(R.id.main_last_cell_id_value_textview1);
        lastCellIdValueTextView1.setOnLongClickListener(this);
        lastLacLabelTextView1 = view.findViewById(R.id.main_last_lac_label_textview1);
        lastLacLabelTextView1.setOnLongClickListener(this);
        lastLacValueTextView1 = view.findViewById(R.id.main_last_lac_value_textview1);
        lastLacValueTextView1.setOnLongClickListener(this);
        lastMccValueTextView1 = view.findViewById(R.id.main_last_mcc_value_textview1);
        lastMccValueTextView1.setOnLongClickListener(this);
        lastMncLabelTextView1 = view.findViewById(R.id.main_last_mnc_label_textview1);
        lastMncLabelTextView1.setOnLongClickListener(this);
        lastMncValueTextView1 = view.findViewById(R.id.main_last_mnc_value_textview1);
        lastMncValueTextView1.setOnLongClickListener(this);
        lastSignalStrengthValueTextView1 = view.findViewById(R.id.main_last_signal_strength_value_textview1);
        lastSignalStrengthValueTextView1.setOnLongClickListener(this);

        lastMccTableRow2 = view.findViewById(R.id.main_last_mcc_tablerow2);
        lastMncTableRow2 = view.findViewById(R.id.main_last_mnc_tablerow2);
        lastLacTableRow2 = view.findViewById(R.id.main_last_lac_tablerow2);
        lastLongCellIdTableRow2 = view.findViewById(R.id.main_last_long_cell_id_tablerow2);
        lastCellIdRncTableRow2 = view.findViewById(R.id.main_last_cell_id_rnc_tablerow2);
        lastCellIdTableRow2 = view.findViewById(R.id.main_last_cell_id_tablerow2);

        lastNetworkTypeValueTextView2 = view.findViewById(R.id.main_last_network_type_value_textview2);
        lastNetworkTypeValueTextView2.setOnLongClickListener(this);
        lastLongCellIdValueTextView2 = view.findViewById(R.id.main_last_long_cell_id_value_textview2);
        lastLongCellIdValueTextView2.setOnLongClickListener(this);
        lastCellIdRncValueTextView2 = view.findViewById(R.id.main_last_cell_id_rnc_value_textview2);
        lastCellIdRncValueTextView2.setOnLongClickListener(this);
        lastCellIdLabelTextView2 = view.findViewById(R.id.main_last_cell_id_label_textview2);
        lastCellIdLabelTextView2.setOnLongClickListener(this);
        lastCellIdValueTextView2 = view.findViewById(R.id.main_last_cell_id_value_textview2);
        lastCellIdValueTextView2.setOnLongClickListener(this);
        lastLacLabelTextView2 = view.findViewById(R.id.main_last_lac_label_textview2);
        lastLacLabelTextView2.setOnLongClickListener(this);
        lastLacValueTextView2 = view.findViewById(R.id.main_last_lac_value_textview2);
        lastLacValueTextView2.setOnLongClickListener(this);
        lastMncLabelTextView2 = view.findViewById(R.id.main_last_mnc_label_textview2);
        lastMncLabelTextView2.setOnLongClickListener(this);
        lastMccValueTextView2 = view.findViewById(R.id.main_last_mcc_value_textview2);
        lastMccValueTextView2.setOnLongClickListener(this);
        lastMncValueTextView2 = view.findViewById(R.id.main_last_mnc_value_textview2);
        lastMncValueTextView2.setOnLongClickListener(this);
        lastSignalStrengthValueTextView2 = view.findViewById(R.id.main_last_signal_strength_value_textview2);
        lastSignalStrengthValueTextView2.setOnLongClickListener(this);

        lastNumberOfCellsValueTextView = view.findViewById(R.id.main_last_number_of_cells_value_textview);
        lastNumberOfCellsValueTextView.setOnLongClickListener(this);
        lastLatitudeValueTextView = view.findViewById(R.id.main_last_latitude_value_textview);
        lastLatitudeValueTextView.setOnLongClickListener(this);
        lastLongitudeValueTextView = view.findViewById(R.id.main_last_longitude_value_textview);
        lastLongitudeValueTextView.setOnLongClickListener(this);
        lastGpsAccuracyValueTextView = view.findViewById(R.id.main_last_gps_accuracy_value_textview);
        lastGpsAccuracyValueTextView.setOnLongClickListener(this);
        lastDateTimeValueTextView = view.findViewById(R.id.main_last_date_time_value_textview);
        lastDateTimeValueTextView.setOnLongClickListener(this);
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
            printCell(mainCell,
                    lastMccTableRow1, lastMncTableRow1, lastLacTableRow1,
                    lastLongCellIdTableRow1, lastCellIdRncTableRow1, lastCellIdTableRow1,
                    lastNetworkTypeValueTextView1, lastLongCellIdValueTextView1, lastCellIdRncValueTextView1,
                    lastCellIdLabelTextView1, lastCellIdValueTextView1, lastLacLabelTextView1,
                    lastLacValueTextView1, lastMncLabelTextView1,
                    lastMccValueTextView1, lastMncValueTextView1,
                    lastSignalStrengthValueTextView1);
        }

        if (measurement.getMainCells().size() > 1) {
            Cell mainCell = measurement.getMainCells().get(1);
            printCell(mainCell,
                    lastMccTableRow2, lastMncTableRow2, lastLacTableRow2,
                    lastLongCellIdTableRow2, lastCellIdRncTableRow2, lastCellIdTableRow2,
                    lastNetworkTypeValueTextView2, lastLongCellIdValueTextView2, lastCellIdRncValueTextView2,
                    lastCellIdLabelTextView2, lastCellIdValueTextView2, lastLacLabelTextView2,
                    lastLacValueTextView2, lastMncLabelTextView2,
                    lastMccValueTextView2, lastMncValueTextView2,
                    lastSignalStrengthValueTextView2);
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

    private void printCell(Cell mainCell,
                           TableRow lastMccTableRow, TableRow lastMncTableRow, TableRow lastLacTableRow,
                           TableRow lastLongCellIdTableRow, TableRow lastCellIdRncTableRow, TableRow lastCellIdTableRow,
                           TextView lastNetworkTypeValueTextView, TextView lastLongCellIdValueTextView, TextView lastCellIdRncValueTextView,
                           TextView lastCellIdLabelTextView, TextView lastCellIdValueTextView, TextView lastLacLabelTextView,
                           TextView lastLacValueTextView, TextView lastMncLabelTextView,
                           TextView lastMccValueTextView, TextView lastMncValueTextView,
                           TextView lastSignalStrengthValueTextView) {
        NetworkGroup networkGroup = mainCell.getNetworkType();
        int networkNameId = NetworkTypeUtils.getNetworkGroupNameResId(networkGroup);
        lastNetworkTypeValueTextView.setText(getStringForLocale(networkNameId));
        lastLongCellIdTableRow.setTag(networkGroup);
        if (mainCell.isCidLong()) {
            lastLongCellIdTableRow.setVisibility(View.VISIBLE);
            lastCellIdRncTableRow.setVisibility(View.VISIBLE);
            lastCellIdTableRow.setVisibility(View.GONE);
        } else {
            lastLongCellIdTableRow.setVisibility(View.GONE);
            lastCellIdRncTableRow.setVisibility(View.GONE);
            lastCellIdTableRow.setVisibility(View.VISIBLE);
        }

        lastMncTableRow.setTag(networkGroup);
        lastLacTableRow.setTag(networkGroup);
        lastCellIdTableRow.setTag(networkGroup);
        if (networkGroup == NetworkGroup.Cdma) {
            lastMccTableRow.setVisibility(View.GONE);
            lastMncLabelTextView.setText(R.string.main_last_sid_label);
            lastLacLabelTextView.setText(R.string.main_last_nid_label);
            lastCellIdLabelTextView.setText(R.string.main_last_bid_label);
        } else {
            lastMccTableRow.setVisibility(View.VISIBLE);
            lastMncLabelTextView.setText(R.string.main_last_mnc_label);
            if (networkGroup == NetworkGroup.Nr || networkGroup == NetworkGroup.Lte) {
                lastLacLabelTextView.setText(R.string.main_last_tac_label);
            } else {
                lastLacLabelTextView.setText(R.string.main_last_lac_label);
            }
            lastCellIdLabelTextView.setText(R.string.main_last_cell_id_label);
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

    @Override
    public boolean onLongClick(View v) {
        if (!(v instanceof TextView))
            return false;
        CharSequence value = ((TextView) v).getText();
        copyToClipboard(value);
        Toast.makeText(getActivity(), getString(R.string.main_last_toast_copy_message, value), Toast.LENGTH_SHORT).show();
        return true;
    }

    private void copyToClipboard(CharSequence value) {
        if (clipboardManager == null) {
            clipboardManager = (ClipboardManager) MyApplication.getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        }
        ClipData clip = ClipData.newPlainText(value, value);
        clipboardManager.setPrimaryClip(clip);
    }
}
