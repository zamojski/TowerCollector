/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dev;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import trikita.log.Log;
import android.widget.Toast;

import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.utils.FileUtils;

public class DatabaseOperations {

    private static final String TAG = DatabaseOperations.class.getSimpleName();

    public static void importDatabase(Context context) {
        File srcFile = getDatabaseExportPath();
        File dstFile = getDatabasePath(context);
        copyDatabase(context, srcFile, dstFile, "import");
    }

    public static void exportDatabase(Context context) {
        File srcFile = getDatabasePath(context);
        File dstFile = getDatabaseExportUniquePath();
        copyDatabase(context, srcFile, dstFile, "export");
    }

    private static void copyDatabase(Context context, File srcFile, File dstFile, String operation) {
        try {
            String externalStorageState = Environment.getExternalStorageState();
            if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
                File externalStorage = Environment.getExternalStorageDirectory();
                if (externalStorage.canWrite()) {
                    FileUtils.copyFile(srcFile, dstFile);
                    Log.d(TAG, "copyDatabase(): Database " + operation + "ed");
                    Toast.makeText(context, "Database " + operation + "ed", Toast.LENGTH_LONG).show();
                }
                else {
                    Log.d(TAG, "copyDatabase(): External storage is read only");
                }
            }
            else {
                Log.d(TAG, "copyDatabase(): External storage is not available");
            }
        } catch (Exception ex) {
            Log.e(TAG, "copyDatabase(): Cannot " + operation + " database", ex);
        }
    }

    public static void deleteDatabase(Context context) {
        File dbFile = getDatabasePath(context);
        Log.d(TAG, "deleteDatabase(): Deleting file " + dbFile);
        boolean deleted = dbFile.delete();
        if (deleted) {
            Log.d(TAG, "deleteDatabase(): File deleted");
            MeasurementsDatabase.invalidateInstance(context);
            Toast.makeText(context, "Database file deleted", Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "deleteDatabase(): Failed to delete database");
        }
    }

    private static File getDatabasePath(Context context) {
        return context.getDatabasePath(MeasurementsDatabase.DATABASE_FILE_NAME);
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
