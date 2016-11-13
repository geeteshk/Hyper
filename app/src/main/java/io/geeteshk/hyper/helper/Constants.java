package io.geeteshk.hyper.helper;

import android.os.Environment;

import java.io.File;

/**
 * Constant values used around app
 */
public class Constants {

    /**
     * Hyper root directory in sdcard
     */
    public static final String HYPER_ROOT = Environment.getExternalStorageDirectory().getPath() + File.separator + "Hyper";

    /**
     * Application package
     */
    public static final String PACKAGE = "io.geeteshk.hyper";

    /**
     * Firebase Global Storage Bucket
     */
    public static final String GS_BUCKET = "gs://hyper-a0ee4.appspot.com";

    /**
     * Literally in the name
     */
    public static final int ONE_MEGABYTE = 1024 * 1024;
}
