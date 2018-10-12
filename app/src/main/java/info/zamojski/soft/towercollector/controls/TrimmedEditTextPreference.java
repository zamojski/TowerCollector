/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.controls;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;

public class TrimmedEditTextPreference extends EditTextPreference {
    public TrimmedEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TrimmedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrimmedEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        String value = super.getText();
        return TextUtils.isEmpty(value) ? "" : value;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.setText(restoreValue ? getPersistedString(null) : (String) defaultValue);
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(Html.fromHtml(summary.toString()));
    }

    @Override
    public void setText(String text) {
        if (TextUtils.isEmpty(text)) {
            super.setText("");
        } else {
            super.setText(text.trim());
        }
    }
}
