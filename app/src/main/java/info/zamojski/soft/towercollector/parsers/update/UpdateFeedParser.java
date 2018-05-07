/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.parsers.update;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;


import info.zamojski.soft.towercollector.model.UpdateInfo;
import info.zamojski.soft.towercollector.model.UpdateInfo.DownloadLink;
import info.zamojski.soft.towercollector.utils.StringUtils;
import timber.log.Timber;

public class UpdateFeedParser {


    private static final String VERSION_CODE = "VersionCode";
    private static final String VERSION_NAME = "VersionName";
    private static final String VERSION_DOWNLOAD_LINKS = "DownloadLinks";
    private static final String VERSION_LABEL = "Label";
    private static final String VERSION_LINK = "Link";
    private static final String VERSION_LINKS = "Links";

    public UpdateInfo parse(String content) throws UpdateFeedParseException {
        try {
            if (!StringUtils.mayBeJson(content))
                return null;
            JSONObject json = new JSONObject(content);
            int versionCode = getVersionCode(json);
            String versionName = getVersionName(json);
            DownloadLink[] downloadLinks = getDownloadLinks(json);
            UpdateInfo updateInfo = new UpdateInfo(versionCode, versionName);
            updateInfo.addDownloadLinks(downloadLinks);
            return updateInfo;
        } catch (JSONException ex) {
            Timber.w("parse(): Error while parsing JSON response");
            throw new UpdateFeedParseException("Cannot parse update feed: `" + TextUtils.htmlEncode(content) + "`", ex);
        }
    }

    private int getVersionCode(JSONObject object) throws JSONException {
        return object.getInt(VERSION_CODE);
    }

    private String getVersionName(JSONObject object) throws JSONException {
        return object.optString(VERSION_NAME);
    }

    private DownloadLink[] getDownloadLinks(JSONObject object) throws JSONException {
        JSONArray array = object.optJSONArray(VERSION_DOWNLOAD_LINKS);
        if (array == null) {
            return new DownloadLink[0];
        }
        int numberOfLinks = array.length();
        DownloadLink[] links = new DownloadLink[numberOfLinks];
        for (int i = 0; i < numberOfLinks; i++) {
            links[i] = getDownloadLink(array.getJSONObject(i));
        }
        return links;
    }

    private DownloadLink getDownloadLink(JSONObject object) throws JSONException {
        String label = object.getString(VERSION_LABEL);
        String singleLink = object.getString(VERSION_LINK);
        JSONArray array = object.optJSONArray(VERSION_LINKS);
        if (array == null) {
            return new DownloadLink(label, new String[]{singleLink});
        }
        int numberOfLinks = array.length();
        String[] multipleLinks = new String[numberOfLinks];
        for (int i = 0; i < numberOfLinks; i++) {
            multipleLinks[i] = array.getString(i);
        }
        return new DownloadLink(label, multipleLinks);
    }
}
