package io.geeteshk.hyper;

import android.os.Environment;

import java.io.File;

public class Constants {

    public static final String GITHUB_URL = "https://github.com/OpenMatter/Hyper";
    public static final String GITHUB_ISSUES_URL = "https://api.github.com/repos/OpenMatter/Hyper/issues";
    public static final String PAYPAL_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XFYSLYYVHVE2J";
    public static final String HYPER_ROOT = Environment.getExternalStorageDirectory().getPath() + File.separator + "Hyper";
    public static final String PACKAGE = "io.geeteshk.hyper";
    public static final String SCHEME = "package";
}
