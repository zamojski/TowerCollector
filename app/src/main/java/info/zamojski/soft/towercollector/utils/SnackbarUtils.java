/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.material.snackbar.Snackbar;

import info.zamojski.soft.towercollector.R;

public final class SnackbarUtils {

    private SnackbarUtils() {
    }

    @NonNull
    public static Snackbar make(@NonNull View view, @StringRes int resId, int duration) {
        Snackbar snackbar = Snackbar.make(view, resId, duration);
        applyThemeColors(snackbar, view.getContext());
        return snackbar;
    }

    @NonNull
    public static Snackbar make(@NonNull View view, @NonNull CharSequence text, int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        applyThemeColors(snackbar, view.getContext());
        return snackbar;
    }

    public static void applyThemeColors(@NonNull Snackbar snackbar, @NonNull Context themeContext) {
        TypedArray attributes = themeContext.obtainStyledAttributes(new int[]{
                R.attr.snackbarTextColor,
                R.attr.snackbarActionColor
        });
        try {
            ColorStateList textColor = attributes.getColorStateList(0);
            ColorStateList actionColor = attributes.getColorStateList(1);
            if (textColor != null) {
                snackbar.setTextColor(textColor);
            }
            if (actionColor != null) {
                snackbar.setActionTextColor(actionColor);
            }
        } finally {
            attributes.recycle();
        }
    }
}
