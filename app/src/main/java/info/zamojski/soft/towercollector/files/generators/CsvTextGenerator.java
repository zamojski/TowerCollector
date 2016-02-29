/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.generators;

import java.io.IOException;
import java.util.List;

import info.zamojski.soft.towercollector.files.devices.IWritableTextDevice;
import info.zamojski.soft.towercollector.files.formatters.csv.ICsvFormatter;
import info.zamojski.soft.towercollector.model.Measurement;

public class CsvTextGenerator<TFormatter extends ICsvFormatter, TDevice extends IWritableTextDevice> {

    private TFormatter formatter;
    private TDevice device;

    public CsvTextGenerator(TFormatter formatter, TDevice device) {
        this.formatter = formatter;
        this.device = device;
    }

    public void writeHeader() throws IOException {
        String header = this.formatter.formatHeader();
        this.device.write(header);
    }

    public void writeEntry(Measurement m) throws IOException {
        String entry = this.formatter.formatRow(m);
        this.device.write(entry);
    }

    public void writeEntryChunk(List<Measurement> ms) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Measurement m : ms) {
            String entry = this.formatter.formatRow(m);
            sb.append(entry);
        }
        this.device.write(sb.toString());
    }

    public String getMimeType() {
        return "text/csv";
    }
}
