/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.controls;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import info.zamojski.soft.towercollector.R;

public class ManagedConfirmationDialogFragment extends DialogFragment {

    public static final String TAG = "ManagedConfirmationDialogFragment";

    private static final String ARG_TITLE_ID = "title_id";
    private static final String ARG_MESSAGE_ID = "message_id";
    private static final String ARG_ACTION_ID = "action_id";

    public interface ConfirmationListener {
        void onConfirmed(int actionId);
    }

    public static ManagedConfirmationDialogFragment newInstance(int titleId, int messageId, int actionId) {
        ManagedConfirmationDialogFragment fragment = new ManagedConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE_ID, titleId);
        args.putInt(ARG_MESSAGE_ID, messageId);
        args.putInt(ARG_ACTION_ID, actionId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int titleId = getArguments().getInt(ARG_TITLE_ID);
        int messageId = getArguments().getInt(ARG_MESSAGE_ID);
        final int actionId = getArguments().getInt(ARG_ACTION_ID);

        DialogInterface.OnClickListener confirmedAction = (dialog, which) -> {
            if (getParentFragment() instanceof ConfirmationListener) {
                ((ConfirmationListener) getParentFragment()).onConfirmed(actionId);
            } else if (getActivity() instanceof ConfirmationListener) {
                ((ConfirmationListener) getActivity()).onConfirmed(actionId);
            }
        };

        return DialogManager.createConfirmationDialog(requireContext(), titleId, messageId, confirmedAction);
    }
}
