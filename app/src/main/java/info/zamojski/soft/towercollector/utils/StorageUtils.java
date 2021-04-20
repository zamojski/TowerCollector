/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
    private static final int URI_BASIC_FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
    private static final int URI_EXTENDED_FLAGS = URI_BASIC_FLAGS | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;

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
            intent.addFlags(URI_EXTENDED_FLAGS);
            try {
                activity.startActivityForResult(intent, OPEN_DOCUMENT_ACTIVITY_RESULT);
            } catch (Exception ex) {
                Toast.makeText(activity, R.string.system_toast_no_handler_for_operation, Toast.LENGTH_LONG).show();
                Throwable wrappedEx = new RuntimeException("No handler to select storage folder", ex);
                MyApplication.handleSilentException(wrappedEx);
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.dialog_cancel), (dialog, which) -> {
            // empty
        });
        alertDialog.show();
    }

    public static void persistStorageUri(Activity activity, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            releaseStorageUri(activity);
            Uri storageUri = data.getData();
            int modeFlags = data.getFlags();
            activity.grantUriPermission(activity.getPackageName(), storageUri, modeFlags);
            activity.getContentResolver().takePersistableUriPermission(storageUri, URI_BASIC_FLAGS);
            MyApplication.getPreferencesProvider().setStorageUri(storageUri);
        } else {
            Toast.makeText(activity, R.string.storage_access_denied, Toast.LENGTH_LONG).show();
        }
    }

    public static void releaseStorageUri(Activity activity) {
        Uri oldStorageUri = MyApplication.getPreferencesProvider().getStorageUri();
        if (oldStorageUri != null) {
            try {
                activity.getContentResolver().releasePersistableUriPermission(oldStorageUri, URI_BASIC_FLAGS);
            } catch (SecurityException ex) {
                // ignore, it means that the permissions for uri are already released
            } finally {
                MyApplication.getPreferencesProvider().setStorageUri(null);
            }
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
                return (legacyFolder.exists() && legacyFolder.isDirectory() && legacyFolder.canRead() && legacyFolder.listFiles().length > 0);
            }
        }
        return false;
    }
}
