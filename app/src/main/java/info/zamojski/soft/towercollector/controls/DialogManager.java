/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.controls;

import org.sufficientlysecure.htmltextview.HtmlResImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import info.zamojski.soft.towercollector.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

public class DialogManager {

    public static AlertDialog createHtmlInfoDialog(Context context, int titleId, int messageId, boolean largeText, boolean textIsSelectable) {
        return createHtmlInfoDialog(context, titleId, messageId, null, largeText, textIsSelectable);
    }

    public static AlertDialog createHtmlInfoDialog(Context context, int titleId, String message, boolean largeText, boolean textIsSelectable) {
        return createHtmlInfoDialog(context, titleId, null, message, largeText, textIsSelectable);
    }

    private static AlertDialog createHtmlInfoDialog(Context context, int titleId, Integer messageId, String message, boolean largeText, boolean textIsSelectable) {
        if (messageId == null && message == null)
            throw new IllegalArgumentException("MessageId or message values is required");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogLayout = inflater.inflate(R.layout.html_information_dialog, null);
        builder.setView(dialogLayout);
        builder.setPositiveButton(R.string.dialog_ok, null);

        builder.setTitle(titleId);
        HtmlTextView messageView = (HtmlTextView) dialogLayout.findViewById(R.id.html_info_dialog_textview);
        messageView.setTextAppearance(context, (largeText ? android.R.style.TextAppearance_Medium : android.R.style.TextAppearance_Small));
        if (messageId != null) {
            messageView.setHtml(messageId, new HtmlResImageGetter(messageView));
        } else {
            messageView.setHtml(message, new HtmlResImageGetter(messageView));
        }
        // don't move above settings content because it won't work
        messageView.setTextIsSelectable(textIsSelectable);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public static AlertDialog createConfirmationDialog(Context context, int titleId, int messageId, DialogInterface.OnClickListener confirmedAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setPositiveButton(R.string.dialog_proceed, confirmedAction);
        builder.setNegativeButton(R.string.dialog_cancel, null);

        builder.setTitle(titleId);
        builder.setMessage(messageId);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
}
