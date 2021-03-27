/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.io.filesystem;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.documentfile.provider.DocumentFile;

import java.io.InputStream;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.utils.StorageUtils;
import timber.log.Timber;

public abstract class FileReader<T> {

    public ReadResult<T> readFile(Context context, Uri storageDirectoryUri, String fileName) throws Exception {
        if (storageDirectoryUri == null)
            throw new IllegalArgumentException("Storage directory uri cannot be empty.");
        if (TextUtils.isEmpty(fileName))
            throw new IllegalArgumentException("File name cannot be empty.");

        DocumentFile storageDirectory = DocumentFile.fromTreeUri(MyApplication.getApplication(), storageDirectoryUri);
        if (!StorageUtils.canReadStorageUri(storageDirectory)) {
            Timber.i("readFile(): Cannot read from storage %s", storageDirectory == null ? storageDirectoryUri.toString() : storageDirectory.getUri().toString());
            return new ReadResult<>(ReadResultType.StorageNotFound);
        }

        DocumentFile file = storageDirectory.findFile(fileName);
        if (file == null || !file.exists()) {
            Timber.i("readFile(): Cannot find the file %s", file == null ? fileName : file.getUri().toString());
            return new ReadResult<>(ReadResultType.FileNotFound);
        }

        if (!file.canRead()) {
            Timber.i("readFile(): Cannot read from file %s", file.getUri().toString());
            return new ReadResult<>(ReadResultType.FileNotReadable);
        }

        try (InputStream inputStream = context.getContentResolver().openInputStream(file.getUri())) {
            T result = readFileInternal(inputStream);
            Timber.d("readFile(): File %s read successfully", file.getUri().toString());
            return new ReadResult<>(ReadResultType.Success, result);
        } catch (Exception ex) {
            Timber.w(ex, "readFile(): Failed to read from file %s", file.getUri().toString());
            return new ReadResult<>(ReadResultType.Failed, ex.getMessage());
        }
    }

    protected abstract T readFileInternal(InputStream inputStream) throws Exception;
}
