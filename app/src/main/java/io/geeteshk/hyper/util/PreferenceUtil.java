package io.geeteshk.hyper.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class to store and read preferences
 */
public class PreferenceUtil {

    /**
     * Name of this application's package
     */
    private static final String PACKAGE = "io.geeteshk.hyper";

    /**
     * Method to get sharedpreferences
     *
     * @param context used to get preference object
     * @return shared preferences object
     */
    private static SharedPreferences pref(Context context) {
        return context.getSharedPreferences(PACKAGE, Context.MODE_PRIVATE);
    }

    /**
     * Store a String in preferences
     *
     * @param context used to get preference object
     * @param name    of the preference
     * @param value   of the preference
     */
    public static void store(Context context, String name, String value) {
        pref(context).edit().putString(name, value).apply();
    }

    /**
     * Store an int in preferences
     *
     * @param context used to get preference object
     * @param name    of the preference
     * @param value   of the preference
     */
    public static void store(Context context, String name, int value) {
        pref(context).edit().putInt(name, value).apply();
    }

    /**
     * Store a boolean in preferences
     *
     * @param context used to get preference object
     * @param name    of the preference
     * @param value   of the preference
     */
    public static void store(Context context, String name, boolean value) {
        pref(context).edit().putBoolean(name, value).apply();
    }

    /**
     * Retrieve a String from preferences
     *
     * @param context  used to get preference object
     * @param name     of the preference
     * @param defValue default value in case preference is not set
     * @return value of preference
     */
    public static String get(Context context, String name, String defValue) {
        return pref(context).getString(name, defValue);
    }

    /**
     * Retrieve an int from preferences
     *
     * @param context  used to get preference object
     * @param name     of the preference
     * @param defValue default value in case preference is not set
     * @return value of preference
     */
    public static int get(Context context, String name, int defValue) {
        return pref(context).getInt(name, defValue);
    }

    /**
     * Retrieve a boolean from preferences
     *
     * @param context  used to get preference object
     * @param name     of the preference
     * @param defValue default value in case preference is not set
     * @return value of preference
     */
    public static boolean get(Context context, String name, boolean defValue) {
        return pref(context).getBoolean(name, defValue);
    }
}
