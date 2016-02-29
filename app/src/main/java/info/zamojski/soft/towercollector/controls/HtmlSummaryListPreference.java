/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.controls;

import android.content.Context;
import android.preference.ListPreference;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HtmlSummaryListPreference extends ListPreference {

    private TextView summaryTextView;
    private String summaryText;

    public HtmlSummaryListPreference(Context context) {
        super(context);
    }

    public HtmlSummaryListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    protected void onBindView(View paramView) {
        super.onBindView(paramView);
        // remove previous summary instance from view
        if (this.summaryTextView != null) {
            ((ViewGroup) this.summaryTextView.getParent()).removeView(this.summaryTextView);
        }
        // find current summary instance on view
        this.summaryTextView = (TextView) paramView.findViewById(android.R.id.summary);
        // replace current summary instance with new one
        if (this.summaryTextView != null) {
            this.summaryTextView.setVisibility(View.GONE);
            TextView newSummaryTextView = new TextView(getContext());
            newSummaryTextView.setLayoutParams(this.summaryTextView.getLayoutParams());
            newSummaryTextView.setEnabled(this.summaryTextView.isEnabled());
            ((ViewGroup) this.summaryTextView.getParent()).addView(newSummaryTextView);
            this.summaryTextView = newSummaryTextView;
            if (this.summaryText != null) {
                this.summaryTextView.setText(Html.fromHtml(this.summaryText));
            }
        }
    }

    @Override
    public void setSummary(CharSequence summary) {
        this.summaryText = summary.toString();
        super.setSummary(summaryText);
    }
}
