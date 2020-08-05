/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.files.devices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import info.zamojski.soft.towercollector.files.DeviceOperationException;
import info.zamojski.soft.towercollector.utils.FileUtils;

public class GZipFileTextDevice implements IWritableTextDevice, IPersistedTextDevice {

    private static final String COMPRESSED_EXTENSION = "gz";

    private String path;
    private String originalFileExtension;

    private GZIPOutputStream gzipOutputStream = null;
    private OutputStreamWriter fileWriter = null;
    private BufferedWriter bufferedWriter = null;

    public GZipFileTextDevice(String path) {
        this.originalFileExtension = FileUtils.getFileExtension(path);
        this.path = path + "." + COMPRESSED_EXTENSION;
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
        return originalFileExtension + "+" + COMPRESSED_EXTENSION;
    }

    @Override
    public void open() throws DeviceOperationException, IOException {
        File file = new File(getPath());
        FileUtils.checkAccess(file);

        gzipOutputStream = new GZIPOutputStream(new FileOutputStream(file, false));
        fileWriter = new OutputStreamWriter(gzipOutputStream);
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
        if (gzipOutputStream != null) {
            try {
                gzipOutputStream.close();
            } catch (IOException ex) {
            }
        }
    }
}
