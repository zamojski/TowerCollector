/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.generators.wrappers;

import java.util.List;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.enums.GeneratorResult;
import info.zamojski.soft.towercollector.files.DeviceOperationException.Reason;
import info.zamojski.soft.towercollector.files.FileGeneratorResult;
import info.zamojski.soft.towercollector.files.generators.wrappers.interfaces.IProgressListener;
import info.zamojski.soft.towercollector.files.generators.wrappers.interfaces.IProgressiveTextGeneratorWrapper;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;
import info.zamojski.soft.towercollector.utils.FileUtils;
import timber.log.Timber;

public class CompositeTextGeneratorWrapper extends TextGeneratorWrapperBase {

    private List<IProgressiveTextGeneratorWrapper> subGenerators;
    private int alreadyGenerated = 0;
    private final int maxProgressPercent = 100;

    public CompositeTextGeneratorWrapper(List<IProgressiveTextGeneratorWrapper> subGenerators) {
        this.subGenerators = subGenerators;
    }

    public FileGeneratorResult generate() {
        FileGeneratorResult lastResult = new FileGeneratorResult(GeneratorResult.Unknown, Reason.Unknown, "Nothing to generate");
        try {
            notifyProgressListeners(0, maxProgressPercent);
            for (IProgressiveTextGeneratorWrapper generator : subGenerators) {
                try {
                    generator.addProgressListener(subProgressListener);
                    long startTime = System.currentTimeMillis();
                    // run generator
                    lastResult = generator.generate();
                    // send stats
                    long endTime = System.currentTimeMillis();
                    AnalyticsStatistics stats = MeasurementsDatabase.getInstance(context).getAnalyticsStatistics();
                    String fileExt = FileUtils.getFileExtension(generator.getDevice().getPath());
                    long duration = (endTime - startTime);
                    MyApplication.getAnalytics().sendExportFinished(duration, fileExt, stats);
                    if (lastResult.getResult() != GeneratorResult.Succeeded) {
                        return lastResult;
                    }
                    alreadyGenerated++;
                } finally {
                    generator.removeProgressListener(subProgressListener);
                }
            }
            // fix for dialog not closed when operation is running in background and data deleted
            notifyProgressListeners(maxProgressPercent, maxProgressPercent);
        } catch (Exception ex) {
            Timber.e(ex, "generate(): Failed to save data on external memory");
            MyApplication.handleSilentException(ex);
            return new FileGeneratorResult(GeneratorResult.Failed, Reason.Unknown, ex.getMessage());
        }
        return lastResult;
    }

    @Override
    public void cancel() {
        super.cancel();
        for (IProgressiveTextGeneratorWrapper generator : subGenerators) {
            generator.cancel();
        }
    }

    public List<IProgressiveTextGeneratorWrapper> getSubGenerators() {
        return subGenerators;
    }

    private IProgressListener subProgressListener = (value, max) -> {
        // count fully generated plus in-progress
        double alreadyGeneratedPercent = 100.0 * alreadyGenerated / subGenerators.size();
        double inProgressPercent = 100.0 * value / max / subGenerators.size();
        int currentProgressPercent = (int) Math.round(alreadyGeneratedPercent + inProgressPercent);
        notifyProgressListeners(currentProgressPercent, maxProgressPercent);
    };
}
