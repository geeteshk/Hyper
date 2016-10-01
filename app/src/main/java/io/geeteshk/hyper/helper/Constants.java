package io.geeteshk.hyper.helper;

import android.os.Environment;

import java.io.File;

/**
 * Constant values used around app
 */
public class Constants {

    /**
     * GitHub repo url
     */
    public static final String GITHUB_URL = "https://github.com/geeteshk/Hyper";

    /**
     * Paypal donation url
     */
    public static final String PAYPAL_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XFYSLYYVHVE2J";

    /**
     * Hyper root directory in sdcard
     */
    public static final String HYPER_ROOT = Environment.getExternalStorageDirectory().getPath() + File.separator + "Hyper";

    /**
     * Application package
     */
    public static final String PACKAGE = "io.geeteshk.hyper";

    /**
     * Scheme for app details intent
     */
    public static final String SCHEME = "package";

    /**
     * Firebase Global Storage Bucket
     */
    static final String GS_BUCKET = "gs://hyper-a0ee4.appspot.com";

    /**
     * Literally in the name
     */
    static final int ONE_MEGABYTE = 1024 * 1024;
}
