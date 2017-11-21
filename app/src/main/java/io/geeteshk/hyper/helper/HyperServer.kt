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

package io.geeteshk.hyper.helper

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

class HyperServer(private val project: String) : NanoHTTPD(8080) {

    override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        var uri = session.uri
        val mimeType = getMimeType(uri)

        if (uri == "/") {
            val indexFile = ProjectManager.getIndexFile(project)
            var indexPath = indexFile!!.path
            indexPath = indexPath.replace(File(Constants.HYPER_ROOT + File.separator + project).path, "")
            uri = File.separator + indexPath
        }

        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(Constants.HYPER_ROOT + File.separator + project + uri)
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        }

        return try {
            NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, mimeType, IOUtils.toString(inputStream, Charset.defaultCharset()))
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
            NanoHTTPD.newFixedLengthResponse(e.toString())
        }

    }

    private fun getMimeType(uri: String): String {
        return TYPES.indices
                .firstOrNull { uri.endsWith("." + TYPES[it]) }
                ?.let { MIMES[it] }
                ?: NanoHTTPD.MIME_HTML
    }

    companion object {

        private val TAG = HyperServer::class.java.simpleName
        private val TYPES = arrayOf("css", "js", "ico", "png", "jpg", "jpe", "svg", "bm", "gif", "ttf", "otf", "woff", "woff2", "eot", "sfnt")
        private val MIMES = arrayOf("text/css", "text/js", "image/x-icon", "image/png", "image/jpg", "image/jpeg", "image/svg+xml", "image/bmp", "image/gif", "application/x-font-ttf", "application/x-font-opentype", "application/font-woff", "application/font-woff2", "application/vnd.ms-fontobject", "application/font-sfnt")
    }
}
