/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.controls;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class ManagedHtmlInfoDialogFragment extends DialogFragment {

    public static final String TAG = "ManagedHtmlInfoDialogFragment";

    private static final String ARG_TITLE_ID = "title_id";
    private static final String ARG_MESSAGE_ID = "message_id";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_LARGE_TEXT = "large_text";
    private static final String ARG_TEXT_SELECTABLE = "text_selectable";

    public static ManagedHtmlInfoDialogFragment newInstance(int titleId, int messageId, boolean largeText, boolean textIsSelectable) {
        ManagedHtmlInfoDialogFragment fragment = new ManagedHtmlInfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE_ID, titleId);
        args.putInt(ARG_MESSAGE_ID, messageId);
        args.putBoolean(ARG_LARGE_TEXT, largeText);
        args.putBoolean(ARG_TEXT_SELECTABLE, textIsSelectable);
        fragment.setArguments(args);
        return fragment;
    }

    public static ManagedHtmlInfoDialogFragment newInstance(int titleId, String message, boolean largeText, boolean textIsSelectable) {
        ManagedHtmlInfoDialogFragment fragment = new ManagedHtmlInfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE_ID, titleId);
        args.putString(ARG_MESSAGE, message);
        args.putBoolean(ARG_LARGE_TEXT, largeText);
        args.putBoolean(ARG_TEXT_SELECTABLE, textIsSelectable);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int titleId = getArguments().getInt(ARG_TITLE_ID);
        boolean largeText = getArguments().getBoolean(ARG_LARGE_TEXT);
        boolean textIsSelectable = getArguments().getBoolean(ARG_TEXT_SELECTABLE);

        if (getArguments().containsKey(ARG_MESSAGE_ID)) {
            int messageId = getArguments().getInt(ARG_MESSAGE_ID);
            return DialogManager.createHtmlInfoDialog(requireContext(), titleId, messageId, largeText, textIsSelectable);
        } else {
            String message = getArguments().getString(ARG_MESSAGE);
            return DialogManager.createHtmlInfoDialog(requireContext(), titleId, message, largeText, textIsSelectable);
        }
    }
}
