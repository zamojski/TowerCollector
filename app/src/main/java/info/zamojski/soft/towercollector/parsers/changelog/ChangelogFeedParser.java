/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.parsers.changelog;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import trikita.log.Log;

import info.zamojski.soft.towercollector.model.ChangelogInfo;
import info.zamojski.soft.towercollector.model.ChangelogInfo.ChangelogEntry;
import info.zamojski.soft.towercollector.parsers.update.UpdateFeedParser;
import info.zamojski.soft.towercollector.utils.StringUtils;

public class ChangelogFeedParser {

    private static final String TAG = UpdateFeedParser.class.getSimpleName();

    private static final String ENTRIES = "Entries";
    private static final String VERSION_CODE = "VersionCode";
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";
    private static final String MESSAGES = "Messages";

    public ChangelogInfo parse(String content) throws ChangelogFeedParseException {
        try {
            if (!StringUtils.mayBeJson(content))
                return new ChangelogInfo();
            JSONObject json = new JSONObject(content);
            List<ChangelogEntry> entries = getEntries(json);
            ChangelogInfo changelog = new ChangelogInfo();
            changelog.addEntries(entries);
            return changelog;
        } catch (JSONException ex) {
            Log.w("parse(): Error while parsing JSON content");
            throw new ChangelogFeedParseException("Cannot parse changelog feed: `" + TextUtils.htmlEncode(content) + "`", ex);
        }
    }

    private List<ChangelogEntry> getEntries(JSONObject object) throws JSONException {
        List<ChangelogEntry> entries = new ArrayList<ChangelogEntry>();
        JSONArray array = object.optJSONArray(ENTRIES);
        if (array == null)
            return entries;
        int noOfEntries = array.length();
        for (int i = 0; i < noOfEntries; i++) {
            entries.add(getEntry(array.getJSONObject(i)));
        }
        return entries;
    }

    private ChangelogEntry getEntry(JSONObject object) throws JSONException {
        int versionCode = getVersionCode(object);
        String title = getTitle(object);
        String description = getDescription(object);
        ChangelogEntry entry = new ChangelogEntry(versionCode, title, description);
        String[] messages = getMessages(object);
        entry.addMessages(messages);
        return entry;
    }

    private int getVersionCode(JSONObject object) throws JSONException {
        return object.getInt(VERSION_CODE);
    }

    private String getTitle(JSONObject object) throws JSONException {
        return object.optString(TITLE);
    }

    private String getDescription(JSONObject object) throws JSONException {
        return object.optString(DESCRIPTION);
    }

    private String[] getMessages(JSONObject object) throws JSONException {
        JSONArray array = object.optJSONArray(MESSAGES);
        if (array == null)
            return new String[0];
        int noOfMessages = array.length();
        String[] messages = new String[noOfMessages];
        for (int i = 0; i < noOfMessages; i++) {
            messages[i] = array.getString(i);
        }
        return messages;
    }
}
