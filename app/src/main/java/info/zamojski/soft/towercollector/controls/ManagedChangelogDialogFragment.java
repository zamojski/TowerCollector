/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.controls;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;

public class ManagedChangelogDialogFragment extends DialogFragment {

    public static final String TAG = "ManagedChangelogDialogFragment";

    private static final String ARG_MESSAGE = "message";
    private static final String ARG_PREV_VERSION = "prev_version";

    public static ManagedChangelogDialogFragment newInstance(String message, int previousVersionCode) {
        ManagedChangelogDialogFragment fragment = new ManagedChangelogDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putInt(ARG_PREV_VERSION, previousVersionCode);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getArguments().getString(ARG_MESSAGE);
        final int previousVersionCode = getArguments().getInt(ARG_PREV_VERSION);

        return DialogManager.createHtmlInfoDialog(
                requireContext(), 
                R.string.dialog_what_is_new, 
                message, 
                false, 
                false, 
                R.string.dialog_remind_later, 
                (dialog, which) -> MyApplication.getPreferencesProvider().setRecentDeveloperMessagesVersion(previousVersionCode)
        );
    }
}
