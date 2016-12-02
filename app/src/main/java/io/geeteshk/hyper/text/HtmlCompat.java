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

package io.geeteshk.hyper.text;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

/**
 * Compat class for handling Html text operations
 */
public class HtmlCompat {

    /**
     * Gets spanned from html text
     *
     * @param html to get spanned from
     * @return spanned html text
     */
    @SuppressLint("Deprecation")
    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT < 24) {
            return Html.fromHtml(html);
        } else {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        }
    }
}
