/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.generators;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import info.zamojski.soft.towercollector.files.devices.IWritableTextDevice;
import info.zamojski.soft.towercollector.files.formatters.json.IJsonFormatter;
import info.zamojski.soft.towercollector.model.Measurement;

public class JsonTextGenerator<TFormatter extends IJsonFormatter, TDevice extends IWritableTextDevice> {

    private TFormatter formatter;
    private TDevice device;

    public JsonTextGenerator(TFormatter formatter, TDevice device) {
        this.formatter = formatter;
        this.device = device;
    }

    public void writeHeader() throws IOException {
        String header = this.formatter.formatHeader();
        this.device.write(header);
    }

    public void writeEntries(List<Measurement> ms) throws IOException {
        try {
            String entry = this.formatter.formatList(ms);
            this.device.write(entry);
        } catch (JSONException ex) {
            throw new IOException("Invalid json format.", ex);
        }
    }

    public void writeNewSegment() throws IOException {
        String newSegment = this.formatter.formatNewSegment();
        this.device.write(newSegment);
    }

    public void writeFooter() throws IOException {
        String footer = this.formatter.formatFooter();
        this.device.write(footer);
    }

    public String getMimeType() {
        return "application/json";
    }
}
