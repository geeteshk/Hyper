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

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

/**
 * Web server class using NanoHTTPD
 */
public class Hyperion extends NanoHTTPD {

    /**
     * Log TAG
     */
    private static final String TAG = Hyperion.class.getSimpleName();

    /**
     * File types and respective mimes
     */
    private final String[] mTypes = {"css", "js", "ico", "png", "jpg", "jpe", "svg", "bm", "gif", "ttf", "otf", "woff", "woff2", "eot", "sfnt"};
    private final String[] mMimes = {"text/css", "text/js", "image/x-icon", "image/png", "image/jpg", "image/jpeg", "image/svg+xml", "image/bmp", "image/gif", "application/x-font-ttf", "application/x-font-opentype", "application/font-woff", "application/font-woff2", "application/vnd.ms-fontobject", "application/font-sfnt"};

    /**
     * Project to host web server for
     */
    private String mProject;

    /**
     * public Constructor
     *
     * @param project to host server for
     */
    public Hyperion(String project) {
        super(8080);
        mProject = project;
    }

    /**
     * Serving files on server
     *
     * @param session not sure what this is
     * @return response
     */
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
            Log.e(TAG, e.getMessage());
        }

        return new Response(Response.Status.OK, mimeType, inputStream);
    }

    /**
     * Get mimetype from uri
     *
     * @param uri of file
     * @return file mimetype
     */
    private String getMimeType(String uri) {
        for (int i = 0; i < mTypes.length; i++) {
            if (uri.endsWith("." + mTypes[i])) {
                return mMimes[i];
            }
        }

        return NanoHTTPD.MIME_HTML;
    }
}
