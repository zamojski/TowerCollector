/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.generators.wrappers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import info.zamojski.soft.towercollector.files.devices.IWritableTextDevice;
import info.zamojski.soft.towercollector.files.generators.wrappers.interfaces.IProgressListener;
import info.zamojski.soft.towercollector.files.generators.wrappers.interfaces.IProgressiveTextGeneratorWrapper;

public abstract class TextGeneratorWrapperBase implements IProgressiveTextGeneratorWrapper {

    private List<IProgressListener> progressListeners = new ArrayList<IProgressListener>();

    protected boolean cancel = false;

    protected Context context;

    protected IWritableTextDevice device;

    public void addProgressListener(IProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(IProgressListener listener) {
        progressListeners.remove(listener);
    }

    protected void notifyProgressListeners(int value, int max) {
        for (IProgressListener listener : progressListeners) {
            listener.reportProgress(value, max);
        }
    }

    @Override
    public void cancel() {
        cancel = true;
    }

}
