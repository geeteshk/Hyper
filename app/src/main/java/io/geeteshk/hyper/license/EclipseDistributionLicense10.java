package io.geeteshk.hyper.license;

import android.content.Context;

import de.psdev.licensesdialog.licenses.License;
import io.geeteshk.hyper.R;

public class EclipseDistributionLicense10 extends License {

    @Override
    public String getName() {
        return "Eclipse Distribution License 1.0";
    }

    @Override
    public String readSummaryTextFromResources(Context context) {
        return getContent(context, R.raw.edl_v10);
    }

    @Override
    public String readFullTextFromResources(Context context) {
        return getContent(context, R.raw.edl_v10);
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getUrl() {
        return "http://www.eclipse.org/org/documents/edl-v10.php";
    }
}
