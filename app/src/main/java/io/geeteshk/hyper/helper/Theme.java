package io.geeteshk.hyper.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;

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

    public static void setNavigationColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
        }
    }
}
