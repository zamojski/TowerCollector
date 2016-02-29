/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import java.util.List;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.model.UpdateInfo.DownloadLink;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class UpdateDialogArrayAdapter extends ArrayAdapter<DownloadLink> {

    private static final int VIEW_LAYOUT = R.layout.new_version_row;
    private static final String DIRECT_DOWNLOAD_LABEL = "_DIRECT_";

    private final Context context;
    private final LayoutInflater inflater;
    private final List<DownloadLink> downloadLinks;

    public UpdateDialogArrayAdapter(Context context, LayoutInflater inflater, List<DownloadLink> downloadLinks) {
        super(context, VIEW_LAYOUT, downloadLinks);
        this.context = context;
        this.inflater = inflater;
        this.downloadLinks = downloadLinks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DownloadLink downloadLink = downloadLinks.get(position);
        String label = downloadLink.getLabel();
        if (label.equals(DIRECT_DOWNLOAD_LABEL))
            label = label.replace(DIRECT_DOWNLOAD_LABEL, context.getString(R.string.updater_dialog_direct_download_label));

        View rowView = inflater.inflate(VIEW_LAYOUT, parent, false);
        TextView rowTextView = (TextView) rowView.findViewById(R.id.download_options_list_row_textview);
        rowTextView.setText(label);

        return rowView;
    }
}
