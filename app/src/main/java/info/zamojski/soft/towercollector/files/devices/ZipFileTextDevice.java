/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.devices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import info.zamojski.soft.towercollector.files.DeviceOperationException;
import info.zamojski.soft.towercollector.utils.FileUtils;

public class ZipFileTextDevice implements IWritableTextDevice {

    private String path;
    private File file;
    private String originalFileName;

    private ZipOutputStream zipOutputStream = null;
    private OutputStreamWriter fileWriter = null;
    private BufferedWriter bufferedWriter = null;

    public ZipFileTextDevice(String path) {
        this.path = path;
        this.file = new File(path + ".zip");
        this.originalFileName = new File(path).getName();
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
    public void open() throws DeviceOperationException, IOException {
        FileUtils.checkAccess(file);

        zipOutputStream = new ZipOutputStream(new FileOutputStream(file, false));
        ZipEntry zipEntry = new ZipEntry(originalFileName);
        zipOutputStream.putNextEntry(zipEntry);
        fileWriter = new OutputStreamWriter(zipOutputStream);
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
        if (zipOutputStream != null) {
            try {
                zipOutputStream.closeEntry();
                zipOutputStream.close();
            } catch (IOException ex) {
            }
        }
    }
}
