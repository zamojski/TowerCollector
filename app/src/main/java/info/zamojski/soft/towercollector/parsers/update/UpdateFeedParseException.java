/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.parsers.update;

import org.json.JSONException;

public class UpdateFeedParseException extends Exception {

    private static final long serialVersionUID = 4146489906664671476L;

    public UpdateFeedParseException(String detailMessage, JSONException ex) {
        super(detailMessage, ex);
    }
}
