/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.generators.wrappers;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.enums.GeneratorResult;
import info.zamojski.soft.towercollector.files.DeviceOperationException.Reason;
import info.zamojski.soft.towercollector.files.FileGeneratorResult;
import info.zamojski.soft.towercollector.files.formatters.gpx.IGpxFormatter;
import info.zamojski.soft.towercollector.files.formatters.gpx.model.HeaderData;
import info.zamojski.soft.towercollector.io.filesystem.CompressionFormat;
import info.zamojski.soft.towercollector.io.filesystem.FileWriter;
import info.zamojski.soft.towercollector.io.filesystem.WriteResult;
import info.zamojski.soft.towercollector.model.Boundaries;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.utils.FileUtils;
import timber.log.Timber;

public class GpxTextGeneratorWrapper extends TextGeneratorWrapperBase {

    private final IGpxFormatter formatter;
    private final Uri storageUri;
    private final String fileName;
    private final String compressedExtension;
    private final CompressionFormat compressionFormat;
    private Uri filePath;

    public GpxTextGeneratorWrapper(Context context, Uri storageUri, String fileName, String compressedExtension, CompressionFormat compressionFormat, IGpxFormatter formatter) {
        this.context = context;
        this.storageUri = storageUri;
        this.fileName = fileName;
        this.compressedExtension = compressedExtension;
        this.compressionFormat = compressionFormat;
        this.formatter = formatter;
    }

    @Override
    public FileGeneratorResult generate() {
        // get number of locations to process
        int locationsCount = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getAllLocationsCount(false);
        // check if there is anything to process
        if (locationsCount == 0) {
            Timber.d("generate(): Cancelling save due to no data");
            return new FileGeneratorResult(GeneratorResult.NoData, Reason.Unknown);
        }
        // calculate number of parts
        final int LOCATIONS_PER_PART = 80;
        int partsCount = 1;
        if (locationsCount > LOCATIONS_PER_PART) {
            partsCount = (int) Math.ceil(1.0 * locationsCount / LOCATIONS_PER_PART);
        }
        notifyProgressListeners(0, locationsCount);
        int finalPartsCount = partsCount;
        FileWriter fileWriter = new FileWriter() {
            @Override
            protected void writeFileInternal(OutputStream outputStream) throws Exception {
                try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                    // write header
                    Measurement firstMeasurement = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getFirstMeasurement();
                    Measurement lastMeasurement = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getLastMeasurement();
                    Boundaries bounds = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getLocationBounds();
                    HeaderData headerData = new HeaderData();
                    headerData.FirstMeasurementTimestamp = firstMeasurement.getMeasuredAt();
                    headerData.LastMeasurementTimestamp = lastMeasurement.getMeasuredAt();
                    headerData.Boundaries = bounds;
                    bufferedWriter.write(formatter.formatHeader(headerData));
                    // remember previous measurement
                    Measurement prevMeasurement = firstMeasurement;
                    // get locations in loop
                    for (int i = 0; i < finalPartsCount; i++) {
                        // get from database
                        List<Measurement> measurements = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getMeasurementsPart(i * LOCATIONS_PER_PART, LOCATIONS_PER_PART);
                        // write to file
                        for (Measurement m : measurements) {
                            // if time difference is more than 30 minutes then create new segment
                            if ((m.getMeasuredAt() - prevMeasurement.getMeasuredAt()) > 1800000) {
                                bufferedWriter.write(formatter.formatNewSegment());
                            }
                            bufferedWriter.write(formatter.formatEntry(m));
                            prevMeasurement = m;
                        }
                        notifyProgressListeners(i * LOCATIONS_PER_PART + measurements.size(), locationsCount);
                        if (cancel) {
                            break;
                        }
                    }
                    // write footer
                    bufferedWriter.write(formatter.formatFooter());
                }
            }
        };
        WriteResult result = fileWriter.writeFile(MyApplication.getApplication(), storageUri, fileName, compressedExtension, compressionFormat);
        filePath = result.getFilePath();
        // fix for dialog not closed when operation is running in background and data deleted
        notifyProgressListeners(locationsCount, locationsCount);
        switch (result.getResultType()) {
            case Success:
                if (cancel) {
                    Timber.d("generate(): Export cancelled");
                    return new FileGeneratorResult(GeneratorResult.Cancelled, Reason.Unknown);
                } else {
                    Timber.d("generate(): All %s locations exported", locationsCount);
                    return new FileGeneratorResult(GeneratorResult.Succeeded, Reason.Unknown);
                }
            case StorageNotFound:
                Toast.makeText(context, R.string.storage_storage_not_found, Toast.LENGTH_LONG).show();
                return new FileGeneratorResult(GeneratorResult.Failed, Reason.LocationNotExists);
            case FileNotWritable:
                Toast.makeText(context, R.string.storage_file_not_writable, Toast.LENGTH_LONG).show();
                return new FileGeneratorResult(GeneratorResult.Failed, Reason.DeviceNotWritable);
            case Failed:
            default:
                Toast.makeText(context, context.getString(R.string.storage_write_failed, result.getErrorMessage()), Toast.LENGTH_LONG).show();
                return new FileGeneratorResult(GeneratorResult.Failed, Reason.Unknown);
        }
    }

    @Override
    public Uri getFullPath() {
        return filePath;
    }

    @Override
    public String getFileType() {
        return FileUtils.getFileExtension(fileName) + (compressedExtension != null ? "+" + compressedExtension : "");
    }
}
