/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dev;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.io.filesystem.FileReader;
import info.zamojski.soft.towercollector.io.filesystem.FileWriter;
import info.zamojski.soft.towercollector.io.filesystem.ReadResult;
import info.zamojski.soft.towercollector.io.filesystem.WriteResult;
import timber.log.Timber;

public class DatabaseOperations {

    public static void importDatabase(Context context) {
        String srcFileName = getDatabaseImportFileName();
        File dstFile = getDatabasePath(context);
        try {
            Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
            if (storageUri != null) {
                FileReader<Void> fileReader = new FileReader<Void>() {
                    @Override
                    protected Void readFileInternal(InputStream inputStream) throws Exception {
                        try (BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(dstFile))) {
                            byte[] buffer = new byte[4 * 1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead);
                            }
                            fileOutputStream.flush();
                        }
                        return null;
                    }
                };
                ReadResult<Void> result = fileReader.readFile(MyApplication.getApplication(), storageUri, srcFileName);
                switch (result.getResultType()) {
                    case Success:
                        Timber.d("importDatabase(): Database imported");
                        break;
                    case StorageNotFound:
                        Toast.makeText(context, R.string.storage_storage_not_found, Toast.LENGTH_LONG).show();
                        break;
                    case FileNotFound:
                        Toast.makeText(context, R.string.storage_file_not_found, Toast.LENGTH_LONG).show();
                        break;
                    case FileNotReadable:
                        Toast.makeText(context, R.string.storage_file_not_readable, Toast.LENGTH_LONG).show();
                        break;
                    case Failed:
                    default:
                        Toast.makeText(context, context.getString(R.string.storage_read_failed, result.getErrorMessage()), Toast.LENGTH_LONG).show();
                        break;
                }
            } else {
                Timber.w("importDatabase(): Storage access denied");
                Toast.makeText(context, R.string.storage_access_denied, Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Timber.e(ex, "importDatabase(): Failed to import database");
            Toast.makeText(context, R.string.database_import_export_failed_message, Toast.LENGTH_LONG).show();
        }
    }

    public static void exportDatabase(Context context) {
        File srcFile = getDatabasePath(context);
        String dstFileName = getDatabaseExportFileName();
        try {
            Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
            if (storageUri != null) {
                FileWriter fileWriter = new FileWriter() {
                    @Override
                    protected void writeFileInternal(OutputStream outputStream) throws Exception {
                        try (InputStream fileInputStream = new FileInputStream(srcFile)) {
                            BufferedOutputStream fileOutputStream = new BufferedOutputStream(outputStream);
                            byte[] buffer = new byte[4 * 1024];
                            int bytesRead;
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead);
                            }
                            fileOutputStream.flush();
                        }
                    }
                };
                WriteResult result = fileWriter.writeFile(MyApplication.getApplication(), storageUri, dstFileName);
                switch (result.getResultType()) {
                    case Success:
                        deleteDatabaseTransactionFiles(context);
                        MeasurementsDatabase.invalidateInstance();
                        Timber.d("exportDatabase(): Database exported");
                        Toast.makeText(context, R.string.database_export_message, Toast.LENGTH_LONG).show();
                        break;
                    case StorageNotFound:
                        Toast.makeText(context, R.string.storage_storage_not_found, Toast.LENGTH_LONG).show();
                        break;
                    case FileNotWritable:
                        Toast.makeText(context, R.string.storage_file_not_writable, Toast.LENGTH_LONG).show();
                        break;
                    case Failed:
                    default:
                        Toast.makeText(context, context.getString(R.string.storage_write_failed, result.getErrorMessage()), Toast.LENGTH_LONG).show();
                        break;
                }
            } else {
                Timber.w("exportDatabase(): Storage access denied");
                Toast.makeText(context, R.string.storage_access_denied, Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Timber.e(ex, "exportDatabase(): Failed to export database from \"%s\" to \"%s\"", srcFile, (dstFileName != null ? dstFileName : "null"));
            Toast.makeText(context, R.string.database_import_export_failed_message, Toast.LENGTH_LONG).show();
        }
    }

    public static String getDatabaseBaseString(Context context) {
        // invalidate handle
        MeasurementsDatabase.invalidateInstance();
        // get file
        File srcFile = getDatabasePath(context);
        // read and compress
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
                try (InputStream fileInputStream = new FileInputStream(srcFile)) {
                    byte[] buffer = new byte[4 * 1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        gzipOutputStream.write(buffer, 0, bytesRead);
                    }
                    gzipOutputStream.flush();
                }
            }
            outputStream.flush();
            // convert to string
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    public static void deleteDatabase(Context context) {
        File dbFile = getDatabasePath(context);
        Timber.d("deleteDatabase(): Deleting file %s", dbFile);
        boolean deleted = dbFile.delete();
        deleteDatabaseTransactionFiles(context);
        if (deleted) {
            Timber.d("deleteDatabase(): File deleted");
            MeasurementsDatabase.invalidateInstance();
            Toast.makeText(context, "Database file deleted", Toast.LENGTH_LONG).show();
        } else {
            Timber.e("deleteDatabase(): Failed to delete database");
        }
    }

    private static void deleteDatabaseTransactionFiles(Context context) {
        deleteDatabaseTransactionFile(context, "-wal");
        deleteDatabaseTransactionFile(context, "-shm");
    }

    private static void deleteDatabaseTransactionFile(Context context, String suffix) {
        File transactionFile = getDatabaseTransactionFile(context, suffix);
        if (transactionFile.exists()) {
            Timber.i("deleteDatabaseTransactionFile(): Deleting transaction file %s", transactionFile.getName());
            transactionFile.delete();
        }
    }

    private static File getDatabasePath(Context context) {
        return context.getDatabasePath(MeasurementsDatabase.DATABASE_FILE_NAME);
    }

    private static File getDatabaseTransactionFile(Context context, String suffix) {
        return context.getDatabasePath(MeasurementsDatabase.DATABASE_FILE_NAME + suffix);
    }

    private static String getDatabaseImportFileName() {
        return MeasurementsDatabase.DATABASE_FILE_NAME;
    }

    private static String getDatabaseExportFileName() {
        return MeasurementsDatabase.DATABASE_FILE_NAME;
    }
}
