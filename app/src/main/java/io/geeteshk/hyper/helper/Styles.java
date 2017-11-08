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

import io.geeteshk.hyper.R;

/**
 * Helper class to handle theme related stuff
 */
public class Styles {

    /**
     * Get current theme
     *
     * @param context to get pref
     * @return theme int
     */
    public static int getThemeInt(Context context) {
        if (Prefs.get(context, "dark_theme", false)) {
            return R.style.Hyper_Dark;
        } else {
            return R.style.Hyper;
        }
    }
}
