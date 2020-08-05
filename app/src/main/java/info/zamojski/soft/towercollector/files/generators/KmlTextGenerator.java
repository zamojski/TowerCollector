/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.generators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import info.zamojski.soft.towercollector.files.devices.IWritableTextDevice;
import info.zamojski.soft.towercollector.files.formatters.kml.IKmlFormatter;
import info.zamojski.soft.towercollector.model.Measurement;

public class KmlTextGenerator<TFormatter extends IKmlFormatter, TDevice extends IWritableTextDevice> {

    private TFormatter formatter;
    private TDevice device;
    private List<String> lineEntries = new ArrayList<>();
    private List<String> lineSegments = new ArrayList<>();

    public KmlTextGenerator(TFormatter formatter, TDevice device) {
        this.formatter = formatter;
        this.device = device;
    }

    public void writeHeader(long firstMeasurementTimestamp, long lastMeasurementTimestamp) throws IOException {
        String header = this.formatter.formatHeader(firstMeasurementTimestamp, lastMeasurementTimestamp);
        this.device.write(header);
    }

    public void writeEntry(Measurement m) throws IOException {
        String entry = this.formatter.formatEntry(m);
        this.device.write(entry);
        String lineEntry = this.formatter.formatLineEntry(m);
        this.lineEntries.add(lineEntry);
    }

    public void writeNewSegment(int segmentId) throws IOException {
        String newSegment = this.formatter.formatNewSegment(segmentId);
        this.device.write(newSegment);
        String lineSegment = this.formatter.formatLineSegment(this.lineEntries);
        this.lineSegments.add(lineSegment);
        lineEntries.clear();
    }

    public void writeFooter() throws IOException {
        String footer = this.formatter.formatFooter(lineSegments);
        this.device.write(footer);
    }
}
