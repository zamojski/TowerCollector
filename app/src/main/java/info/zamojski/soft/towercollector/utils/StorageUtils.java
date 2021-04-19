/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;

public class StorageUtils {

    public static final int OPEN_DOCUMENT_ACTIVITY_RESULT = 'D';

    public static void requestStorageUri(Activity activity) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);
        alertDialog.setTitle(R.string.storage_request_access_title);
        String message = activity.getString(R.string.storage_request_access_message);
        Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
        if (storageUri != null || canMigrateLegacyStorage()) {
            message += "\n\n" + activity.getString(R.string.storage_request_access_migrate_message);
        }
        alertDialog.setMessage(message);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getString(R.string.dialog_proceed), (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            intent.putExtra("android.content.extra.FANCY", true);
            intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
            ComponentName handler = intent.resolveActivity(activity.getPackageManager());
            if (handler == null) {
                Throwable ex = new RuntimeException("No handler to select storage folder");
                MyApplication.handleSilentException(ex);
            } else {
                activity.startActivityForResult(intent, OPEN_DOCUMENT_ACTIVITY_RESULT);
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.dialog_cancel), (dialog, which) -> {
            // empty
        });
        alertDialog.show();
    }

    public static void persistStorageUri(Activity activity, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri oldStorageUri = MyApplication.getPreferencesProvider().getStorageUri();
            if (oldStorageUri != null) {
                activity.getContentResolver().releasePersistableUriPermission(oldStorageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            Uri storageUri = data.getData();
            activity.grantUriPermission(activity.getPackageName(), storageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.getContentResolver().takePersistableUriPermission(storageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            MyApplication.getPreferencesProvider().setStorageUri(storageUri);
        } else {
            Toast.makeText(activity, R.string.storage_access_denied, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean canReadStorageUri(Uri storageUri) {
        if (storageUri == null)
            return false;
        DocumentFile storageDirectory = DocumentFile.fromTreeUri(MyApplication.getApplication(), storageUri);
        return canReadStorageUri(storageDirectory);
    }

    public static boolean canReadStorageUri(DocumentFile storageDirectory) {
        return storageDirectory != null && storageDirectory.canRead();
    }

    public static boolean canWriteStorageUri(Uri storageUri) {
        if (storageUri == null)
            return false;
        DocumentFile storageDirectory = DocumentFile.fromTreeUri(MyApplication.getApplication(), storageUri);
        return canWriteStorageUri(storageDirectory);
    }

    public static boolean canWriteStorageUri(DocumentFile storageDirectory) {
        return storageDirectory != null && storageDirectory.canWrite();
    }

    private static boolean canMigrateLegacyStorage() {
        // only if storage permission granted
        boolean hasStoragePermission = PermissionUtils.hasPermissions(MyApplication.getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasStoragePermission) {
            // only if storage available
            boolean isLegacyStorageAvailable = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                    || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Environment.isExternalStorageLegacy();
            if (isLegacyStorageAvailable) {
                // only if the old folder exists and contains some files
                File legacyFolder = new File(Environment.getExternalStorageDirectory(), "TowerCollector");
                return (legacyFolder.exists() && legacyFolder.canRead() && legacyFolder.listFiles().length > 0);
            }
        }
        return false;
    }
}
