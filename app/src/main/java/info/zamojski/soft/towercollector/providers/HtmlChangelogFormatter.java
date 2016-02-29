/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers;

import java.util.List;

import android.text.TextUtils;

import info.zamojski.soft.towercollector.model.ChangelogInfo;
import info.zamojski.soft.towercollector.model.ChangelogInfo.ChangelogEntry;

public class HtmlChangelogFormatter {

    public String formatChangelog(ChangelogInfo changelog) {
        StringBuilder sb = new StringBuilder();
        for (ChangelogEntry entry : changelog.getEntries()) {
            String title = entry.getTitle();
            if (TextUtils.isEmpty(title))
                continue;
            sb.append(formatTitle(title));
            String description = entry.getDescription();
            if (!TextUtils.isEmpty(description)) {
                sb.append(formatDescription(description));
            }
            List<String> messages = entry.getMessages();
            if (!messages.isEmpty()) {
                sb.append(formatMessages(messages));
            }
        }
        return sb.toString();
    }

    private String formatTitle(String title) {
        return String.format("<h3>%s</h3>", title);
    }

    private String formatDescription(String description) {
        return String.format("<p>%s</p>", description);
    }

    private String formatMessages(List<String> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for (String message : messages) {
            sb.append(formatMessage(message));
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private String formatMessage(String message) {
        return String.format("<li>%s</li>", message);
    }

}
