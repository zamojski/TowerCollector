/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.controls;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.Collection;
import java.util.Collections;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.utils.ResourceUtils;
import io.noties.markwon.Markwon;
import io.noties.markwon.image.ImageItem;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.SchemeHandler;
import io.noties.markwon.linkify.LinkifyPlugin;

public class DialogManager {

    public static AlertDialog createHtmlInfoDialog(Context context, int titleId, int messageId, boolean largeText, boolean textIsSelectable) {
        return createHtmlInfoDialog(context, titleId, messageId, null, largeText, textIsSelectable, null, null);
    }

    public static AlertDialog createHtmlInfoDialog(Context context, int titleId, String message, boolean largeText, boolean textIsSelectable) {
        return createHtmlInfoDialog(context, titleId, null, message, largeText, textIsSelectable, null, null);
    }

    public static AlertDialog createHtmlInfoDialog(Context context, int titleId, String message, boolean largeText, boolean textIsSelectable, Integer negativeActionTextId, DialogInterface.OnClickListener negativeAction) {
        return createHtmlInfoDialog(context, titleId, null, message, largeText, textIsSelectable, negativeActionTextId, negativeAction);
    }

    private static AlertDialog createHtmlInfoDialog(Context context, int titleId, Integer messageId, String message, boolean largeText, boolean textIsSelectable, Integer negativeActionTextId, DialogInterface.OnClickListener negativeAction) {
        if (messageId == null && message == null)
            throw new IllegalArgumentException("MessageId or message values is required");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogLayout = inflater.inflate(R.layout.html_information_dialog, null);
        builder.setView(dialogLayout);
        builder.setPositiveButton(R.string.dialog_ok, null);
        if (negativeActionTextId != null && negativeAction != null) {
            builder.setNegativeButton(negativeActionTextId, negativeAction);
        }

        builder.setTitle(titleId);
        TextView messageView = dialogLayout.findViewById(R.id.html_info_dialog_textview);
        messageView.setTextAppearance(context, (largeText ? android.R.style.TextAppearance_Medium : android.R.style.TextAppearance_Small));

        Markwon markwon = Markwon.builder(context)
                .usePlugin(ImagesPlugin.create(plugin -> {
                    // Custom schema handler because ImagesPlugin doesn't support drawables natively
                    plugin.addSchemeHandler(new SchemeHandler() {
                        @NonNull
                        @Override
                        public ImageItem handle(@NonNull String raw, @NonNull Uri uri) {
                            // For "drawable://my_icon", uri.getHost() will be "my_icon"
                            String resourceName = uri.getHost();

                            // Using getIdentifier is discouraged, but necessary here to load drawables from markdown
                            int resId = context.getResources().getIdentifier(
                                    resourceName,
                                    "drawable",
                                    context.getPackageName()
                            );

                            if (resId != 0) {
                                Drawable drawable = AppCompatResources.getDrawable(context, resId);
                                if (drawable != null) {
                                    return ImageItem.withResult(drawable);
                                }
                            }

                            // Fallback to a transparent drawable if the resource is not found to avoid crashing
                            return ImageItem.withResult(new ColorDrawable(Color.TRANSPARENT));
                        }

                        @NonNull
                        @Override
                        public Collection<String> supportedSchemes() {
                            return Collections.singleton("drawable");
                        }
                    });
                }))
                // Do not use autolink XML attribute on your TextView as it will remove all links except autolinked ones ¯\_(ツ)_/¯
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .build();

        if (messageId != null) {
            message = ResourceUtils.getRawString(context, messageId);
        }
        markwon.setMarkdown(messageView, message);
        // don't move above setting the content because it won't work
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
