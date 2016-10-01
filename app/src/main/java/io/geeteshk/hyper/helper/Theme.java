package io.geeteshk.hyper.helper;

import android.content.Context;

import io.geeteshk.hyper.R;

/**
 * Helper class to handle theme related stuff
 */
public class Theme {

    /**
     * Get current theme
     *
     * @param context to get pref
     * @return theme int
     */
    public static int getThemeInt(Context context) {
        if (Pref.get(context, "dark_theme", false)) {
            return R.style.Hyper_Dark;
        } else {
            return R.style.Hyper;
        }
    }
}
