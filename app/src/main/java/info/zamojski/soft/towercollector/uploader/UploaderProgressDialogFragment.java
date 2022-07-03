/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.uploader;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import info.zamojski.soft.towercollector.R;

public class UploaderProgressDialogFragment extends DialogFragment {

    public static final String FRAGMENT_TAG = "UPLOADER_DIALOG_FRAGMENT";

    private static final String ARGS_CURRENT_PERCENT = "CURRENT_PERCENT";
    private static final String ARGS_MAX_PERCENT = "MAX_PERCENT";

    private OnUploaderCancelledListener cancelListener;
    private int currentPercent;
    private int maxPercent;

    public static UploaderProgressDialogFragment createInstance(int currentPercent, int maxPercent) {
        UploaderProgressDialogFragment fragment = new UploaderProgressDialogFragment();
        Bundle bundle = new Bundle();
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
        this.currentPercent = getArguments().getInt(ARGS_CURRENT_PERCENT);
        this.maxPercent = getArguments().getInt(ARGS_MAX_PERCENT);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        ProgressDialog uploaderProgressDialog = new ProgressDialog(getContext());
        uploaderProgressDialog.setTitle(R.string.uploader_dialog_progress_title);
        uploaderProgressDialog.setCancelable(false);
        uploaderProgressDialog.setCanceledOnTouchOutside(false);
        uploaderProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_cancel), (dialog, which) -> {
            if (cancelListener != null)
                cancelListener.onUploadCancelled();
            // hide loading indicator
            if (dialog != null)
                dialog.dismiss();
        });
        uploaderProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        uploaderProgressDialog.setProgress(currentPercent);
        uploaderProgressDialog.setMax(maxPercent);
        return uploaderProgressDialog;
    }

    public void setProgress(int value) {
        ProgressDialog dialog = (ProgressDialog) getDialog();
        if (dialog != null) {
            dialog.setProgress(value);
        }
    }

    public void setCancelListener(OnUploaderCancelledListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    public interface OnUploaderCancelledListener {
        void onUploadCancelled();
    }
}
