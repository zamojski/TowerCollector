/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.devices;

public class MemoryTextDevice implements IWritableTextDevice {

    private StringBuilder device;

    @Override
    public void write(String s) {
        this.device.append(s);
    }

    @Override
    public String read() {
        return this.device.toString();
    }

    @Override
    public void open() {
        device = new StringBuilder();
    }

    @Override
    public void close() {
    }

    @Override
    public String getPath() {
        throw new UnsupportedOperationException("In-memory device is not persisted!");
    }

    @Override
    public String getFileType() {
        throw new UnsupportedOperationException("In-memory device is not persisted!");
    }

}
