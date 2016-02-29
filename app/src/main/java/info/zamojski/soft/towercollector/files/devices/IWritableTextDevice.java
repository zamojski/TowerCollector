/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.devices;

import info.zamojski.soft.towercollector.files.DeviceOperationException;

import java.io.IOException;

public interface IWritableTextDevice {

    public void write(String s) throws IOException;

    public String read();

    public void open() throws DeviceOperationException, IOException;

    public void close();

}
