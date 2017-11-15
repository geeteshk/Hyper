/*
 * Copyright 2016 Geetesh Kalakoti <kalakotig@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.geeteshk.hyper.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class to store and read preferences
 */
public class Prefs {

    /**
     * Method to get sharedpreferences
     *
     * @param context used to get preference object
     * @return shared preferences object
     */
    private static SharedPreferences pref(Context context) {
        return context.getSharedPreferences(Constants.PACKAGE, Context.MODE_PRIVATE);
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
    
    public static void storeProject(Context context, String name, String author, String description, String keywords, int type) {
        store(context, "name", name);
        store(context, "author", author);
        store(context, "description", description);
        store(context, "keywords", keywords);
        store(context, "type", type);
    }
}
