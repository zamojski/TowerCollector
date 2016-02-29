/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files;

import info.zamojski.soft.towercollector.files.devices.IWritableTextDevice;
import info.zamojski.soft.towercollector.files.formatters.csv.CsvExportFormatter;
import info.zamojski.soft.towercollector.files.formatters.csv.CsvUploadFormatter;
import info.zamojski.soft.towercollector.files.formatters.csv.ICsvFormatter;
import info.zamojski.soft.towercollector.files.formatters.gpx.GpxExportFormatter;
import info.zamojski.soft.towercollector.files.formatters.gpx.IGpxFormatter;
import info.zamojski.soft.towercollector.files.generators.CsvTextGenerator;
import info.zamojski.soft.towercollector.files.generators.GpxTextGenerator;

public class TextGeneratorFactory {

    public static CsvTextGenerator<ICsvFormatter, IWritableTextDevice> CreateCsvExportGenerator(IWritableTextDevice device) {
        ICsvFormatter formatter = new CsvExportFormatter();
        return CreateCsvGenerator(formatter, device);
    }

    public static CsvTextGenerator<ICsvFormatter, IWritableTextDevice> CreateCsvUploadGenerator(IWritableTextDevice device) {
        ICsvFormatter formatter = new CsvUploadFormatter();
        return CreateCsvGenerator(formatter, device);
    }

    private static CsvTextGenerator<ICsvFormatter, IWritableTextDevice> CreateCsvGenerator(ICsvFormatter formatter, IWritableTextDevice device) {
        return new CsvTextGenerator<ICsvFormatter, IWritableTextDevice>(formatter, device);
    }

    public static GpxTextGenerator<IGpxFormatter, IWritableTextDevice> CreateGpxExportGenerator(IWritableTextDevice device) {
        IGpxFormatter formatter = new GpxExportFormatter();
        return CreateGpxGenerator(formatter, device);
    }

    private static GpxTextGenerator<IGpxFormatter, IWritableTextDevice> CreateGpxGenerator(IGpxFormatter formatter, IWritableTextDevice device) {
        return new GpxTextGenerator<IGpxFormatter, IWritableTextDevice>(formatter, device);
    }

}
