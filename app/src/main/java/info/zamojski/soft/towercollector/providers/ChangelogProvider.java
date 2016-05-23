/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import info.zamojski.soft.towercollector.model.ChangelogInfo;
import info.zamojski.soft.towercollector.model.ChangelogInfo.ChangelogEntry;
import info.zamojski.soft.towercollector.parsers.changelog.ChangelogFeedParseException;
import info.zamojski.soft.towercollector.parsers.changelog.ChangelogFeedParser;

import trikita.log.Log;

import info.zamojski.soft.towercollector.utils.ResourceUtils;

public class ChangelogProvider {

    private static final String TAG = ChangelogProvider.class.getSimpleName();

    private Context context;
    private int changelogId;
    private ChangelogFeedParser changelogParser;

    public ChangelogProvider(Context context, int changelogId) {
        this.context = context;
        this.changelogId = changelogId;
        this.changelogParser = new ChangelogFeedParser();
    }

    public ChangelogInfo getChangelog(int previousVersion) {
        try {
            String rawChangelog = ResourceUtils.getRawResource(context, changelogId);
            ChangelogInfo changelog = changelogParser.parse(rawChangelog);
            List<ChangelogEntry> entriesToRemove = new ArrayList<ChangelogEntry>();
            for (ChangelogEntry entry : changelog.getEntries()) {
                if (entry.getVersionCode() <= previousVersion) {
                    entriesToRemove.add(entry);
                }
            }
            changelog.removeEntries(entriesToRemove);
            return changelog;
        } catch (ChangelogFeedParseException ex) {
            Log.e("getChangelog(): Failed to parse changelog", ex);
            return new ChangelogInfo();
        }
    }
}
