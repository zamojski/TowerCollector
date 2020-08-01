/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.devices;

import info.zamojski.soft.towercollector.files.DeviceOperationException;
import info.zamojski.soft.towercollector.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileTextDevice implements IWritableTextDevice {

    private String path;
    private File file;

    private FileWriter fileWriter = null;
    private BufferedWriter bufferedWriter = null;

    public FileTextDevice(String path) {
        this.path = path;
        this.file = new File(path);
    }

    @Override
    public void write(String s) throws IOException {
        bufferedWriter.append(s);
    }

    @Override
    public String read() {
        throw new UnsupportedOperationException("Device is write-only");
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getFileType() {
        return FileUtils.getFileExtension(getPath());
    }

    @Override
    public void open() throws DeviceOperationException, IOException {
        FileUtils.checkAccess(file);
        fileWriter = new FileWriter(file, false);
        bufferedWriter = new BufferedWriter(fileWriter);
    }

    @Override
    public void close() {
        if (bufferedWriter != null) {
            try {
                bufferedWriter.close();
            } catch (IOException ex) {
            }
        }
        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (IOException ex) {
            }
        }
    }
}
