/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.filesystem;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.documentfile.provider.DocumentFile;

import java.io.OutputStream;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.utils.FileUtils;
import info.zamojski.soft.towercollector.utils.StorageUtils;
import timber.log.Timber;

public abstract class FileWriter {

    public WriteResult writeFile(Context context, Uri storageDirectoryUri, String fileName) {
        if (storageDirectoryUri == null)
            throw new IllegalArgumentException("Storage directory uri cannot be empty.");
        if (TextUtils.isEmpty(fileName))
            throw new IllegalArgumentException("File name cannot be empty.");

        DocumentFile storageDirectory = DocumentFile.fromTreeUri(MyApplication.getApplication(), storageDirectoryUri);
        if (!StorageUtils.canWriteStorageUri(storageDirectoryUri)) {
            Timber.i("writeFile(): Cannot write to storage %s", storageDirectory == null ? storageDirectoryUri.toString() : storageDirectory.getUri().toString());
            return new WriteResult(WriteResultType.StorageNotFound);
        }

        DocumentFile file = storageDirectory.findFile(fileName);
        if (file == null || !file.exists()) {
            file = storageDirectory.createFile(FileUtils.getFileMimeType(fileName), fileName);
            Timber.i("writeFile(): File created %s", file == null ? fileName : file.getUri().toString());
        } else {
            Timber.i("writeFile(): Overwriting file %s", file.getUri().toString());
        }

        if (file == null || !file.canWrite()) {
            Timber.i("writeFile(): Cannot write to file %s", file == null ? fileName : file.getUri().toString());
            return new WriteResult(WriteResultType.FileNotWritable);
        }

        try (OutputStream outputStream = context.getContentResolver().openOutputStream(file.getUri())) {
            writeFileInternal(outputStream);
            Timber.d("writeFile(): File %s read successfully", file.getUri().toString());
            return new WriteResult(WriteResultType.Success);
        } catch (Exception ex) {
            Timber.w(ex, "writeFile(): Failed to read from file %s", file.getUri().toString());
            return new WriteResult(WriteResultType.Failed, ex.getMessage());
        }
    }

    protected abstract void writeFileInternal(OutputStream outputStream) throws Exception;
}
