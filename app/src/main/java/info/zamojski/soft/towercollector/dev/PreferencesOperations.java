/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dev;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.android.internal.util.XmlUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.io.filesystem.FileReader;
import info.zamojski.soft.towercollector.io.filesystem.FileWriter;
import info.zamojski.soft.towercollector.io.filesystem.ReadResult;
import info.zamojski.soft.towercollector.io.filesystem.WriteResult;
import timber.log.Timber;

public class PreferencesOperations {

    public static void importPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String fileName = getPreferencesExportFileName();
        boolean loaded = loadSharedPreferencesFromFile(context, prefs, fileName);
        if (loaded) {
            Toast.makeText(context, R.string.preferences_import_message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, R.string.preferences_import_export_failed_message, Toast.LENGTH_LONG).show();
        }
    }

    public static void exportPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String fileName = getPreferencesExportFileName();
        boolean saved = saveSharedPreferencesToFile(context, prefs, fileName);
        if (saved) {
            Toast.makeText(context, R.string.preferences_export_message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, R.string.preferences_import_export_failed_message, Toast.LENGTH_LONG).show();
        }
    }

    public static void clearPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor prefEdit = prefs.edit();
        prefEdit.clear();
        boolean cleared = prefEdit.commit();
        if (cleared) {
            Timber.d("clearPreferences(): Preferences cleared");
            Toast.makeText(context, "Preferences cleared", Toast.LENGTH_LONG).show();
        } else {
            Timber.e("clearPreferences(): Failed to clear preferences");
        }
    }

    private static boolean saveSharedPreferencesToFile(Context context, SharedPreferences prefs, String fileName) {
        try {
            Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
            if (storageUri != null) {
                FileWriter fileWriter = new FileWriter() {
                    @Override
                    protected void writeFileInternal(OutputStream outputStream) throws Exception {
                        XmlUtils.writeMapXml(prefs.getAll(), outputStream);
                    }
                };
                WriteResult result = fileWriter.writeFile(MyApplication.getApplication(), storageUri, fileName);
                switch (result.getResultType()) {
                    case Success:
                        Timber.d("saveSharedPreferencesToFile(): Preferences exported");
                        return true;
                    case StorageNotFound:
                        Toast.makeText(context, R.string.storage_storage_not_found, Toast.LENGTH_LONG).show();
                        return false;
                    case FileNotWritable:
                        Toast.makeText(context, R.string.storage_file_not_writable, Toast.LENGTH_LONG).show();
                        return false;
                    case Failed:
                    default:
                        Toast.makeText(context, context.getString(R.string.storage_write_failed, result.getErrorMessage()), Toast.LENGTH_LONG).show();
                        return false;
                }
            } else {
                Timber.w("saveSharedPreferencesToFile(): Storage access denied");
                Toast.makeText(context, R.string.storage_access_denied, Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception ex) {
            Timber.e(ex, "saveSharedPreferencesToFile(): Cannot export preferences");
            return false;
        }
    }

    private static boolean loadSharedPreferencesFromFile(Context context, SharedPreferences prefs, String fileName) {
        try {
            Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
            if (storageUri != null) {
                FileReader<Map<String, ?>> fileReader = new FileReader<Map<String, ?>>() {
                    @Override
                    protected Map<String, ?> readFileInternal(InputStream inputStream) throws Exception {
                        @SuppressWarnings("unchecked")
                        Map<String, ?> entries = XmlUtils.readMapXml(inputStream);
                        return entries;
                    }
                };
                ReadResult<Map<String, ?>> result = fileReader.readFile(MyApplication.getApplication(), storageUri, fileName);
                switch (result.getResultType()) {
                    case Success:
                        Editor prefEdit = prefs.edit();
                        prefEdit.clear();
                        for (Entry<String, ?> entry : result.getValue().entrySet()) {
                            Object v = entry.getValue();
                            String key = entry.getKey();

                            if (v instanceof Boolean)
                                prefEdit.putBoolean(key, (Boolean) v);
                            else if (v instanceof Float)
                                prefEdit.putFloat(key, (Float) v);
                            else if (v instanceof Integer)
                                prefEdit.putInt(key, (Integer) v);
                            else if (v instanceof Long)
                                prefEdit.putLong(key, (Long) v);
                            else if (v instanceof String)
                                prefEdit.putString(key, ((String) v));
                        }
                        prefEdit.apply();
                        Timber.d("loadSharedPreferencesFromFile(): Preferences imported");
                        return true;
                    case StorageNotFound:
                        Toast.makeText(context, R.string.storage_storage_not_found, Toast.LENGTH_LONG).show();
                        return false;
                    case FileNotFound:
                        Toast.makeText(context, R.string.storage_file_not_found, Toast.LENGTH_LONG).show();
                        return false;
                    case FileNotReadable:
                        Toast.makeText(context, R.string.storage_file_not_readable, Toast.LENGTH_LONG).show();
                        return false;
                    case Failed:
                    default:
                        Toast.makeText(context, context.getString(R.string.storage_read_failed, result.getErrorMessage()), Toast.LENGTH_LONG).show();
                        return false;
                }
            } else {
                Timber.w("loadSharedPreferencesFromFile(): Storage access denied");
                Toast.makeText(context, R.string.storage_access_denied, Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception ex) {
            Timber.e(ex, "loadSharedPreferencesFromFile(): Cannot import preferences");
            return false;
        }
    }

    private static String getPreferencesExportFileName() {
        return "preferences.xml";
    }
}
