/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.formatters.gpx;

import info.zamojski.soft.towercollector.files.formatters.gpx.model.HeaderData;
import info.zamojski.soft.towercollector.model.Measurement;

public interface IGpxFormatter {

    public String formatHeader(HeaderData h);

    public String formatRow(Measurement m);

    public String formatNewSegment();

    public String formatFooter();

}
