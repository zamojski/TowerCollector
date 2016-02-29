/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UpdateInfo implements Serializable {

    private static final long serialVersionUID = 2374016653367388314L;

    private int versionCode;
    private String versionName;
    private List<DownloadLink> downloadLinks;

    public UpdateInfo(int versionCode, String versionName) {
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.downloadLinks = new ArrayList<DownloadLink>();
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
        for (DownloadLink link : links) {
            downloadLinks.add(link);
        }
    }

    public static class DownloadLink implements Serializable {

        private static final long serialVersionUID = 2834764366113140232L;

        private String label;
        private String link;

        public DownloadLink(String label, String link) {
            this.label = label;
            this.link = link;
        }

        public String getLabel() {
            return label;
        }

        public String getLink() {
            return link;
        }
    }
}
