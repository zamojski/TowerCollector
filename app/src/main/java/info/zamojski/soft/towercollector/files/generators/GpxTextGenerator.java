/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.generators;

import java.io.IOException;

import info.zamojski.soft.towercollector.files.devices.IWritableTextDevice;
import info.zamojski.soft.towercollector.files.formatters.gpx.IGpxFormatter;
import info.zamojski.soft.towercollector.files.formatters.gpx.model.HeaderData;
import info.zamojski.soft.towercollector.model.Measurement;

public class GpxTextGenerator<TFormatter extends IGpxFormatter, TDevice extends IWritableTextDevice> {

    private TFormatter formatter;
    private TDevice device;

    public GpxTextGenerator(TFormatter formatter, TDevice device) {
        this.formatter = formatter;
        this.device = device;
    }

    public void writeHeader(HeaderData h) throws IOException {
        String header = this.formatter.formatHeader(h);
        this.device.write(header);
    }

    public void writeEntry(Measurement m) throws IOException {
        String entry = this.formatter.formatEntry(m);
        this.device.write(entry);
    }

    public void writeNewSegment() throws IOException {
        String newSegment = this.formatter.formatNewSegment();
        this.device.write(newSegment);
    }

    public void writeFooter() throws IOException {
        String footer = this.formatter.formatFooter();
        this.device.write(footer);
    }
}
