package io.geeteshk.hyper.helper;

import android.content.Context;

import io.geeteshk.hyper.R;

public class Theme {

    public static int getThemeInt(Context context) {
        if (Pref.get(context, "dark_theme", false)) {
            return R.style.Hyper_Dark;
        } else {
            return R.style.Hyper;
        }
    }
}
