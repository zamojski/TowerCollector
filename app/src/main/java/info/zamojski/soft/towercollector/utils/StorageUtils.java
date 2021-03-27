/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;

import static android.app.Activity.RESULT_OK;

public class StorageUtils {

    public static final int OPEN_DOCUMENT_ACTIVITY_RESULT = 'D';

    public static void requestStorageUri(Activity activity) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);
        alertDialog.setTitle(R.string.storage_request_access_title);
        alertDialog.setMessage(activity.getString(R.string.storage_request_access_message));
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
        if (resultCode == RESULT_OK) {
            Uri treeUri = data.getData();
            activity.grantUriPermission(activity.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            MyApplication.getPreferencesProvider().setStorageUri(treeUri);
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
}
