/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.filesystem;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.documentfile.provider.DocumentFile;

import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.utils.FileUtils;
import info.zamojski.soft.towercollector.utils.StorageUtils;
import timber.log.Timber;

public abstract class FileWriter {

    public WriteResult writeFile(Context context, Uri storageDirectoryUri, String fileName, String compressedExtension, CompressionFormat compressionFormat) {
        if (storageDirectoryUri == null)
            throw new IllegalArgumentException("Storage directory uri cannot be empty.");
        if (TextUtils.isEmpty(fileName))
            throw new IllegalArgumentException("File name cannot be empty.");

        DocumentFile storageDirectory = DocumentFile.fromTreeUri(MyApplication.getApplication(), storageDirectoryUri);
        if (!StorageUtils.canWriteStorageUri(storageDirectoryUri)) {
            Timber.i("writeFile(): Cannot write to storage %s", storageDirectory == null ? storageDirectoryUri.toString() : storageDirectory.getUri().toString());
            return new WriteResult(WriteResultType.StorageNotFound, null);
        }

        String compressedFileName = fileName + (compressedExtension != null ? "." + compressedExtension : "");
        DocumentFile file = storageDirectory.findFile(compressedFileName);
        if (file == null || !file.exists()) {
            file = storageDirectory.createFile(FileUtils.getFileMimeType(compressedFileName), compressedFileName);
            Timber.i("writeFile(): File created %s", file == null ? compressedFileName : file.getUri().toString());
        } else {
            Timber.i("writeFile(): Overwriting file %s", file.getUri().toString());
        }

        if (file == null || !file.canWrite()) {
            Timber.i("writeFile(): Cannot write to file %s", file == null ? compressedFileName : file.getUri().toString());
            return new WriteResult(WriteResultType.FileNotWritable, file == null ? null : file.getUri());
        }

        try (OutputStream outputStream = context.getContentResolver().openOutputStream(file.getUri(), "wt")) {
            switch (compressionFormat) {
                case Zip:
                    writeZipFileInternal(outputStream, fileName);
                    break;
                case GZip:
                    writeGZipFileInternal(outputStream);
                    break;
                default:
                    writeFileInternal(outputStream);
                    break;
            }
            Timber.d("writeFile(): File %s wrote successfully", file.getUri().toString());
            return new WriteResult(WriteResultType.Success, file.getUri());
        } catch (Exception ex) {
            Timber.w(ex, "writeFile(): Failed to write to file %s", file.getUri().toString());
            return new WriteResult(WriteResultType.Failed, file.getUri(), ex.getMessage());
        }
    }

    public WriteResult writeFile(Context context, Uri storageDirectoryUri, String fileName) {
        return writeFile(context, storageDirectoryUri, fileName, null, CompressionFormat.None);
    }

    protected abstract void writeFileInternal(OutputStream outputStream) throws Exception;

    private void writeZipFileInternal(OutputStream outputStream, String fileName) throws Exception {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOutputStream.putNextEntry(zipEntry);
            writeFileInternal(zipOutputStream);
        }
    }

    private void writeGZipFileInternal(OutputStream outputStream) throws Exception {
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
            writeFileInternal(gzipOutputStream);
        }
    }
}
