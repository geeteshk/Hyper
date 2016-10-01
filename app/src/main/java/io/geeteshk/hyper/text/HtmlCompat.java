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
    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT < 24) {
            return Html.fromHtml(html);
        } else {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        }
    }
}
