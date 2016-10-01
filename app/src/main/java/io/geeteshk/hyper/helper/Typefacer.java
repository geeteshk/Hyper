package io.geeteshk.hyper.helper;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Utility class to help override typeface
 */
public final class Typefacer {

    /**
     * Log TAG
     */
    private static final String TAG = Typefacer.class.getSimpleName();

    /**
     * Method to set default font
     *
     * @param context                 used to get assets
     * @param staticTypefaceFieldName name of typeface
     * @param fontAssetName           name of asset
     */
    public static void setDefaultFont(Context context, String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(), fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    /**
     * Uses reflection to override typefaces
     *
     * @param staticTypefaceFieldName name of typeface
     * @param newTypeface             new typeface
     */
    private static void replaceFont(String staticTypefaceFieldName, final Typeface newTypeface) {
        try {
            final Field StaticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
            StaticField.setAccessible(true);
            StaticField.set(null, newTypeface);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
