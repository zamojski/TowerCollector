/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.export;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import info.zamojski.soft.towercollector.R;

public class ExportProgressDialogFragment extends DialogFragment {

    public static final String FRAGMENT_TAG = "EXPORT_DIALOG_FRAGMENT";

    private static final String ARGS_STORAGE_URI = "STORAGE_URI";
    private static final String ARGS_CURRENT_PERCENT = "CURRENT_PERCENT";
    private static final String ARGS_MAX_PERCENT = "MAX_PERCENT";

    private OnExportCancelledListener cancelListener;
    private Uri storageUri;
    private int currentPercent;
    private int maxPercent;

    public static ExportProgressDialogFragment createInstance(Uri storageUri, int currentPercent, int maxPercent) {
        ExportProgressDialogFragment fragment = new ExportProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGS_STORAGE_URI, storageUri);
        bundle.putInt(ARGS_CURRENT_PERCENT, currentPercent);
        bundle.putInt(ARGS_MAX_PERCENT, maxPercent);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        if (getArguments() == null)
            throw new IllegalArgumentException("Fragment creation requires arguments");
        this.storageUri = getArguments().getParcelable(ARGS_STORAGE_URI);
        this.currentPercent = getArguments().getInt(ARGS_CURRENT_PERCENT);
        this.maxPercent = getArguments().getInt(ARGS_MAX_PERCENT);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        ProgressDialog exportProgressDialog = new ProgressDialog(getContext());
        exportProgressDialog.setTitle(R.string.export_dialog_progress_title);
        exportProgressDialog.setMessage(getString(R.string.export_dialog_progress_message, storageUri == null ? "" : storageUri.getPath()));
        exportProgressDialog.setCancelable(false);
        exportProgressDialog.setCanceledOnTouchOutside(false);
        exportProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_cancel), (dialog, which) -> {
            if (cancelListener != null)
                cancelListener.onExportCancelled();
            // hide loading indicator
            if (dialog != null)
                dialog.dismiss();
        });
        exportProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        exportProgressDialog.setProgress(currentPercent);
        exportProgressDialog.setMax(maxPercent);
        return exportProgressDialog;
    }

    public void setProgress(int value) {
        ProgressDialog dialog = (ProgressDialog) getDialog();
        if (dialog != null) {
            dialog.setProgress(value);
        }
    }

    public void setCancelListener(OnExportCancelledListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    public interface OnExportCancelledListener {
        void onExportCancelled();
    }
}
