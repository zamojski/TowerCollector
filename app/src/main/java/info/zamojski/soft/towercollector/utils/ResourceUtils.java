/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.RawRes;
import androidx.core.content.res.ResourcesCompat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import timber.log.Timber;

public class ResourceUtils {

    public static String getRawString(Context context, @RawRes int rawId) {
        try {
            InputStream inputStream = context.getResources().openRawResource(rawId);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
            return byteArrayOutputStream.toString();
        } catch (Exception ex) {
            Timber.e(ex, "getRawString(): Unable to read resource");
            return "";
        }
    }

    public static Bitmap getDrawableBitmap(Context context, @DrawableRes int drawableId) {
        return getDrawableBitmap(context, drawableId, null);
    }

    public static Bitmap getDrawableBitmap(Context context, @DrawableRes int drawableId, Resources.Theme theme) {
        try {
            Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), drawableId, theme);
            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception ex) {
            Timber.e(ex, "getDrawableBitmap(): Unable to convert drawable to bitmap");
            return null;
        }
    }
}
