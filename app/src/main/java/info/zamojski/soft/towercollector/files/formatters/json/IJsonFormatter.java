/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.json;

import org.json.JSONException;

import java.util.List;

import info.zamojski.soft.towercollector.model.Measurement;

public interface IJsonFormatter {

    String formatHeader();

    String formatList(List<Measurement> ms) throws JSONException;

    String formatNewSegment();

    String formatFooter();
}
