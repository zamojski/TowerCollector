/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.controls;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;

import androidx.preference.ListPreference;

public class HtmlSummaryListPreference extends ListPreference {

    public HtmlSummaryListPreference(Context context) {
        super(context);
    }

    public HtmlSummaryListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(Html.fromHtml(summary.toString()));
    }
}
