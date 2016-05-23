/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.content.Context;
import trikita.log.Log;
import android.util.TypedValue;

public class ResourceUtils {

    private static final String TAG = ResourceUtils.class.getSimpleName();

    public static String getRawResource(Context context, int resourceId) {
        try {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(resourceId)));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            return builder.toString();
        } catch (Exception ex) {
            Log.e("getRawResource(): Unable to read resource", ex);
            return "";
        }
    }

    public static String getColorFromThemeInHex(Context context, int resourceId) {
        TypedValue typedvalueattr = new TypedValue();
        context.getTheme().resolveAttribute(resourceId, typedvalueattr, true);
        return String.format("#%06X", (0xFFFFFF & context.getResources().getColor(typedvalueattr.resourceId)));
    }

}
