/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dev;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.utils.FileUtils;
import timber.log.Timber;

public class DatabaseOperations {

    private static final String OPERATION_IMPORT = "import";
    private static final String OPERATION_EXPORT = "export";

    public static void importDatabase(Context context) {
        File srcFile = getDatabaseImportPath();
        File dstFile = getDatabasePath(context);
        copyDatabase(context, srcFile, dstFile, OPERATION_IMPORT);
    }

    public static void exportDatabase(Context context) {
        File srcFile = getDatabasePath(context);
        File dstFile = getDatabaseExportPath();
        copyDatabase(context, srcFile, dstFile, OPERATION_EXPORT);
    }

    public static void exportDatabaseUnique(Context context) {
        File srcFile = getDatabasePath(context);
        File dstFile = getDatabaseExportUniquePath();
        copyDatabase(context, srcFile, dstFile, OPERATION_EXPORT);
    }

    private static void copyDatabase(Context context, File srcFile, File dstFile, String operation) {
        try {
            String externalStorageState = Environment.getExternalStorageState();
            if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
                File externalStorage = Environment.getExternalStorageDirectory();
                if (externalStorage.canWrite()) {
                    FileUtils.copyFile(srcFile, dstFile);
                    Timber.d("copyDatabase(): Database " + operation + "ed");
                    int operationMessage = operation.equals(OPERATION_IMPORT) ? R.string.database_import_message : R.string.database_export_message;
                    Toast.makeText(context, operationMessage, Toast.LENGTH_LONG).show();
                } else {
                    Timber.d("copyDatabase(): External storage is read only");
                    Toast.makeText(context, R.string.export_toast_storage_read_only, Toast.LENGTH_LONG).show();
                }
            } else {
                Timber.d("copyDatabase(): External storage is not available");
                Toast.makeText(context, R.string.export_toast_no_storage, Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Timber.e(ex, "copyDatabase(): Cannot " + operation + " database");
            Toast.makeText(context, R.string.database_import_export_failed_message, Toast.LENGTH_LONG).show();
        }
    }

    public static void deleteDatabase(Context context) {
        File dbFile = getDatabasePath(context);
        Timber.d("deleteDatabase(): Deleting file %s", dbFile);
        boolean deleted = dbFile.delete();
        if (deleted) {
            Timber.d("deleteDatabase(): File deleted");
            MeasurementsDatabase.invalidateInstance(context);
            Toast.makeText(context, "Database file deleted", Toast.LENGTH_LONG).show();
        } else {
            Timber.e("deleteDatabase(): Failed to delete database");
        }
    }

    private static File getDatabasePath(Context context) {
        return context.getDatabasePath(MeasurementsDatabase.DATABASE_FILE_NAME);
    }

    private static File getDatabaseImportPath() {
        return new File(FileUtils.getExternalStorageAppDir(),
                MeasurementsDatabase.DATABASE_FILE_NAME);
    }

    private static File getDatabaseExportPath() {
        return new File(FileUtils.getExternalStorageAppDir(),
                MeasurementsDatabase.DATABASE_FILE_NAME);
    }

    private static File getDatabaseExportUniquePath() {
        return new File(FileUtils.getExternalStorageAppDir(),
                System.currentTimeMillis() + "_" + MeasurementsDatabase.DATABASE_FILE_NAME);
    }
}
