/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.parsers.changelog;

import org.json.JSONException;

public class ChangelogFeedParseException extends Exception {

    private static final long serialVersionUID = -8924872429894303380L;

    public ChangelogFeedParseException(String detailMessage, JSONException ex) {
        super(detailMessage, ex);
    }

}
