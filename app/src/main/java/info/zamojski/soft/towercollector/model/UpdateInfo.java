/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateInfo implements Serializable {

    private static final long serialVersionUID = 2374016653367388314L;

    private final int versionCode;
    private final String versionName;
    private final List<DownloadLink> downloadLinks;

    public UpdateInfo(int versionCode, String versionName) {
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.downloadLinks = new ArrayList<>();
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public List<DownloadLink> getDownloadLinks() {
        return downloadLinks;
    }

    public void addDownloadLinks(DownloadLink... links) {
        downloadLinks.addAll(Arrays.asList(links));
    }

    public static class DownloadLink implements Serializable {

        private static final long serialVersionUID = 2834764366113140232L;

        private final String label;
        private final String[] links;

        public DownloadLink(String label, String[] links) {
            this.label = label;
            this.links = links;
        }

        public String getLabel() {
            return label;
        }

        public String[] getLinks() {
            return links;
        }
    }
}
