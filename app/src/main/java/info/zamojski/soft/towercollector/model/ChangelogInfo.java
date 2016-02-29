/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChangelogInfo implements Serializable {

    private static final long serialVersionUID = 1438072125223734182L;

    private List<ChangelogEntry> entries;

    public ChangelogInfo() {
        this.entries = new ArrayList<ChangelogEntry>();
    }

    public List<ChangelogEntry> getEntries() {
        return this.entries;
    }

    public void addEntries(List<ChangelogEntry> entries) {
        for (ChangelogEntry entry : entries) {
            this.entries.add(entry);
        }
    }

    public void removeEntries(List<ChangelogEntry> entries) {
        for (ChangelogEntry entry : entries) {
            this.entries.remove(entry);
        }
    }

    public static class ChangelogEntry implements Serializable {

        private static final long serialVersionUID = -4193198152281670744L;

        private int versionCode;
        private String title;
        private String description;
        private List<String> messages;

        public ChangelogEntry(int versionCode, String title, String description) {
            this.versionCode = versionCode;
            this.title = title;
            this.description = description;
            this.messages = new ArrayList<String>();
        }

        public void addMessages(String... messages) {
            for (String message : messages) {
                this.messages.add(message);
            }
        }

        public int getVersionCode() {
            return this.versionCode;
        }

        public String getTitle() {
            return this.title;
        }

        public String getDescription() {
            return this.description;
        }

        public List<String> getMessages() {
            return this.messages;
        }

    }
}
