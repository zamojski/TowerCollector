/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.dev;

import info.zamojski.soft.towercollector.utils.FileUtils;
import timber.log.Timber;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.android.internal.util.XmlUtils;

public class PreferencesOperations {


    public static void importPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        File srcFile = getPreferencesExportPath();
        boolean loaded = loadSharedPreferencesFromFile(prefs, srcFile);
        if (loaded) {
            Toast.makeText(context, "Preferences imported", Toast.LENGTH_LONG).show();
        }
    }

    public static void exportPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        File dstFile = getPreferencesExportPath();
        boolean saved = saveSharedPreferencesToFile(prefs, dstFile);
        if (saved) {
            Toast.makeText(context, "Preferences exported", Toast.LENGTH_LONG).show();
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

    private static boolean saveSharedPreferencesToFile(SharedPreferences prefs, File dst) {
        boolean res = false;
        ObjectOutputStream output = null;
        try {
            String externalStorageState = Environment.getExternalStorageState();
            if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
                File externalStorage = Environment.getExternalStorageDirectory();
                if (externalStorage.canWrite()) {
                    output = new ObjectOutputStream(new FileOutputStream(dst));
                    XmlUtils.writeMapXml(prefs.getAll(), output);
                    res = true;
                    Timber.d("saveSharedPreferencesToFile(): Preferences exported");
                }
                Timber.d("saveSharedPreferencesToFile(): External storage is read only");
            }
            Timber.d("saveSharedPreferencesToFile(): External storage is not available");
        } catch (Exception ex) {
            Timber.e(ex, "saveSharedPreferencesToFile(): Cannot export preferences");
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                Timber.e(ex, "saveSharedPreferencesToFile(): Failed to close file stream");
            }
        }
        return res;
    }

    private static boolean loadSharedPreferencesFromFile(SharedPreferences prefs, File src) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
            String externalStorageState = Environment.getExternalStorageState();
            if (externalStorageState.equals(Environment.MEDIA_MOUNTED)
                    || externalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                File externalStorage = Environment.getExternalStorageDirectory();
                if (externalStorage.canRead()) {
                    input = new ObjectInputStream(new FileInputStream(src));
                    Editor prefEdit = prefs.edit();
                    prefEdit.clear();
                    @SuppressWarnings("unchecked")
                    Map<String, ?> entries = XmlUtils.readMapXml(input);
                    for (Entry<String, ?> entry : entries.entrySet()) {
                        Object v = entry.getValue();
                        String key = entry.getKey();

                        if (v instanceof Boolean)
                            prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                        else if (v instanceof Float)
                            prefEdit.putFloat(key, ((Float) v).floatValue());
                        else if (v instanceof Integer)
                            prefEdit.putInt(key, ((Integer) v).intValue());
                        else if (v instanceof Long)
                            prefEdit.putLong(key, ((Long) v).longValue());
                        else if (v instanceof String)
                            prefEdit.putString(key, ((String) v));
                    }
                    prefEdit.commit();
                    Timber.d("loadSharedPreferencesFromFile(): Preferences imported");
                    res = true;
                }
                Timber.d("saveSharedPreferencesToFile(): External storage is read only");
            }
            Timber.d("saveSharedPreferencesToFile(): External storage is not available");
        } catch (Exception ex) {
            Timber.e(ex, "loadSharedPreferencesFromFile(): Cannot import preferences");
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                Timber.e(ex, "loadSharedPreferencesFromFile(): Failed to close file stream");
            }
        }
        return res;
    }

    private static File getPreferencesExportPath() {
        return new File(FileUtils.getExternalStorageAppDir(), "preferences.xml");
    }
}
