/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.generators.wrappers;

import android.content.Context;

import java.io.IOException;
import java.util.List;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.enums.GeneratorResult;
import info.zamojski.soft.towercollector.files.DeviceOperationException;
import info.zamojski.soft.towercollector.files.DeviceOperationException.Reason;
import info.zamojski.soft.towercollector.files.FileGeneratorResult;
import info.zamojski.soft.towercollector.files.devices.IWritableTextDevice;
import info.zamojski.soft.towercollector.files.formatters.json.IJsonFormatter;
import info.zamojski.soft.towercollector.files.formatters.json.JsonMozillaExportFormatter;
import info.zamojski.soft.towercollector.files.generators.JsonTextGenerator;
import info.zamojski.soft.towercollector.model.Measurement;
import timber.log.Timber;

public class JsonTextGeneratorWrapper extends TextGeneratorWrapperBase {

    private JsonTextGenerator<IJsonFormatter, IWritableTextDevice> generator;

    public JsonTextGeneratorWrapper(Context context, IWritableTextDevice device) {
        this.context = context;
        this.device = device;
        this.generator = new JsonTextGenerator(new JsonMozillaExportFormatter(), device);
    }

    @Override
    public FileGeneratorResult generate() {
        try {
            // get number of locations to process
            int locationsCount = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getAllLocationsCount(false);
            // check if there is anything to process
            if (locationsCount == 0) {
                Timber.d("generate(): Cancelling save due to no data");
                return new FileGeneratorResult(GeneratorResult.NoData, DeviceOperationException.Reason.Unknown);
            }
            // calculate number of parts
            final int LOCATIONS_PER_PART = 80;
            int partsCount = 1;
            if (locationsCount > LOCATIONS_PER_PART) {
                partsCount = (int) Math.ceil(1.0 * locationsCount / LOCATIONS_PER_PART);
            }
            device.open();
            notifyProgressListeners(0, locationsCount);
            generator.writeHeader();
            // get locations in loop
            for (int i = 0; i < partsCount; i++) {
                if (i > 0) {
                    generator.writeNewSegment();
                }
                // get from database
                List<Measurement> measurements = MeasurementsDatabase.getInstance(context).getMeasurementsPart(i * LOCATIONS_PER_PART, LOCATIONS_PER_PART);
                // write to file
                generator.writeEntries(measurements);
                notifyProgressListeners(i * LOCATIONS_PER_PART + measurements.size(), locationsCount);
                if (cancel) {
                    break;
                }
            }
            generator.writeFooter();
            device.close();
            // fix for dialog not closed when operation is running in background and data deleted
            notifyProgressListeners(locationsCount, locationsCount);
            if (cancel) {
                Timber.d("generate(): Export cancelled");
                return new FileGeneratorResult(GeneratorResult.Cancelled, Reason.Unknown);
            } else {
                Timber.d("generate(): All %s locations exported", locationsCount);
                return new FileGeneratorResult(GeneratorResult.Succeeded, Reason.Unknown);
            }
        } catch (DeviceOperationException ex) {
            Timber.e(ex, "generate(): Failed to check external memory compatibility");
            MyApplication.handleSilentException(ex);
            return new FileGeneratorResult(GeneratorResult.Failed, ex.getReason());
        } catch (IOException ex) {
            Timber.e(ex, "generate(): Failed to save data on external memory");
            MyApplication.handleSilentException(ex);
            return new FileGeneratorResult(GeneratorResult.Failed, Reason.Unknown, ex.getMessage());
        } finally {
            // just for sure
            device.close();
        }
    }
}
