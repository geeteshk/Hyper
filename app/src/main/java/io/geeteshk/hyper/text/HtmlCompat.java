package io.geeteshk.hyper.text;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

public class HtmlCompat {

    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT < 24) {
            return Html.fromHtml(html);
        } else {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        }
    }
}
