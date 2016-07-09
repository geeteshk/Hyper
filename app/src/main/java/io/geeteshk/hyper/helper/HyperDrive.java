package io.geeteshk.hyper.helper;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;
import io.geeteshk.hyper.Constants;

public class HyperDrive extends NanoHTTPD {

    private static final String TAG = HyperDrive.class.getSimpleName();
    private final String[] mTypes = {"css", "js", "ico", "png", "jpg", "jpe", "svg", "bm", "gif", "ttf", "otf", "woff", "woff2", "eot", "sfnt"};
    private final String[] mMimes = {"text/css", "text/js", "image/x-icon", "image/png", "image/jpg", "image/jpeg", "image/svg+xml", "image/bmp", "image/gif", "application/x-font-ttf", "application/x-font-opentype", "application/font-woff", "application/font-woff2", "application/vnd.ms-fontobject", "application/font-sfnt"};
    private String mProject;

    public HyperDrive(String project) {
        super(8080);
        mProject = project;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        String mimeType = getMimeType(uri);

        if (uri.equals("/")) {
            uri = "/index.html";
        }

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(Constants.HYPER_ROOT + File.separator + mProject + uri);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        return new Response(Response.Status.OK, mimeType, inputStream);
    }

    private String getMimeType(String uri) {
        for (int i = 0; i < mTypes.length; i++) {
            if (uri.endsWith("." + mTypes[i])) {
                return mMimes[i];
            }
        }

        return NanoHTTPD.MIME_HTML;
    }
}
