/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.devices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import info.zamojski.soft.towercollector.files.DeviceOperationException;
import info.zamojski.soft.towercollector.utils.FileUtils;

public class ZipFileTextDevice implements IWritableTextDevice, IPersistedTextDevice {

    private static final String COMPRESSED_EXTENSION = "zip";

    private String path;
    private String originalFileName;
    private String originalFileExtension;
    private String customExtension;

    private ZipOutputStream zipOutputStream = null;
    private OutputStreamWriter fileWriter = null;
    private BufferedWriter bufferedWriter = null;

    public ZipFileTextDevice(String path) {
        this(path, null);
    }

    public ZipFileTextDevice(String path, String customExtension) {
        this.originalFileName = new File(path).getName();
        this.originalFileExtension = FileUtils.getFileExtension(path);
        this.customExtension = customExtension;
        this.path = customExtension != null ? FileUtils.changeExtension(path, customExtension) : (path + "." + COMPRESSED_EXTENSION);
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
        return customExtension != null ? customExtension : (originalFileExtension + "+" + COMPRESSED_EXTENSION);
    }

    @Override
    public void open() throws DeviceOperationException, IOException {
        File file = new File(getPath());
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
