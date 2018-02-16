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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import io.geeteshk.hyper.R
import io.geeteshk.hyper.adapter.ProjectAdapter
import io.geeteshk.hyper.hyperx.copyInputStreamToFile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOCase
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.apache.commons.io.filefilter.NameFileFilter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*

object ProjectManager {

    val TYPES = arrayOf("Default")

    private val TAG = ProjectManager::class.java.simpleName

    fun generate(context: Context, name: String, author: String, description: String, keywords: String, stream: InputStream?, adapter: ProjectAdapter, view: View, type: Int) {
        var nameNew = name
        var counter = 1
        while (File(Constants.HYPER_ROOT + File.separator + nameNew).exists()) {
            nameNew = "$name($counter)"
            counter++
        }

        var status = false
        when (type) {
            0 -> status = generateDefault(context, nameNew, author, description, keywords, stream, type)
        }

        if (status) {
            adapter.insert(nameNew)
            Snackbar.make(view, R.string.project_success, Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(view, R.string.project_fail, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun generateDefault(context: Context, name: String, author: String, description: String, keywords: String, stream: InputStream?, type: Int): Boolean {
        val projectFile = File(Constants.HYPER_ROOT + File.separator + name)
        val cssFile = File(projectFile, "css")
        val jsFile = File(projectFile, "js")
        try {
            FileUtils.forceMkdir(projectFile)
            FileUtils.forceMkdir(File(projectFile, "images"))
            FileUtils.forceMkdir(File(projectFile, "fonts"))
            FileUtils.forceMkdir(cssFile)
            FileUtils.forceMkdir(jsFile)

            FileUtils.writeStringToFile(File(projectFile, "index.html"), ProjectFiles.Default.getIndex(name, author, description, keywords), Charset.defaultCharset())
            FileUtils.writeStringToFile(File(cssFile, "style.css"), ProjectFiles.Default.STYLE, Charset.defaultCharset())
            FileUtils.writeStringToFile(File(jsFile, "main.js"), ProjectFiles.Default.MAIN, Charset.defaultCharset())

            if (stream == null) {
                copyIcon(context, name)
            } else {
                copyIcon(name, stream)
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
            return false
        }

        return true
    }

    fun importProject(fileStr: String, name: String, author: String, description: String, keywords: String, type: Int, adapter: ProjectAdapter, view: View) {
        val file = File(fileStr)
        var nameNew = name
        var counter = 1
        while (File(Constants.HYPER_ROOT + File.separator + nameNew).exists()) {
            nameNew = file.name + "(" + counter + ")"
            counter++
        }

        val outFile = File(Constants.HYPER_ROOT + File.separator + nameNew)
        try {
            FileUtils.forceMkdir(outFile)
            FileUtils.copyDirectory(file, outFile)
            if (!File(outFile, "index.html").exists()) {
                FileUtils.writeStringToFile(File(outFile, "index.html"), ProjectFiles.Import.getIndex(nameNew, author, description, keywords), Charset.defaultCharset())
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
            Snackbar.make(view, R.string.project_fail, Snackbar.LENGTH_SHORT).show()
            return
        }

        adapter.insert(nameNew)
        Snackbar.make(view, R.string.project_success, Snackbar.LENGTH_SHORT).show()
    }

    fun isValid(string: String): Boolean = getIndexFile(string) != null

    fun deleteProject(name: String) {
        val projectDir = File(Constants.HYPER_ROOT + File.separator + name)
        try {
            FileUtils.deleteDirectory(projectDir)
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        }

    }

    private fun getFaviconFile(dir: File): File? {
        val filter = NameFileFilter("favicon.ico", IOCase.INSENSITIVE)
        val iterator = FileUtils.iterateFiles(dir, filter, DirectoryFileFilter.DIRECTORY)
        return if (iterator.hasNext()) {
            iterator.next()
        } else null

    }

    fun getIndexFile(project: String): File? {
        val filter = NameFileFilter("index.html", IOCase.INSENSITIVE)
        val iterator = FileUtils.iterateFiles(File(Constants.HYPER_ROOT + File.separator + project), filter, DirectoryFileFilter.DIRECTORY)
        return iterator.next()
    }

    fun getFavicon(context: Context, name: String): Bitmap {
        val faviconFile = getFaviconFile(File(Constants.HYPER_ROOT + File.separator + name))
        return if (faviconFile != null) {
            BitmapFactory.decodeFile(faviconFile.path)
        } else {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher)
        }
    }

    private fun copyIcon(context: Context, name: String) {
        try {
            val manager = context.assets
            val stream = manager.open("web/favicon.ico")
            val output = File(Constants.HYPER_ROOT + File.separator + name + File.separator + "images" + File.separator + "favicon.ico")
            output.copyInputStreamToFile(stream)
            stream.close()
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }

    private fun copyIcon(name: String, stream: InputStream) {
        try {
            val output = File(Constants.HYPER_ROOT + File.separator + name + File.separator + "images" + File.separator + "favicon.ico")
            output.copyInputStreamToFile(stream)
            stream.close()
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }

    fun isBinaryFile(f: File): Boolean {
        var result = 0
        try {
            val `in` = FileInputStream(f)
            var size = `in`.available()
            if (size > 1024) size = 1024
            val data = ByteArray(size)
            result = `in`.read(data)
            `in`.close()

            var ascii = 0
            var other = 0

            for (b in data) {
                if (b < 0x09) return true

                if (b.toInt() == 0x09 || b.toInt() == 0x0A || b.toInt() == 0x0C || b.toInt() == 0x0D)
                    ascii++
                else if (b in 0x20..0x7E)
                    ascii++
                else
                    other++
            }

            return other != 0 && 100 * other / (ascii + other) > 95

        } catch (e: Exception) {
            Log.e(TAG, e.message + result.toString())
        }

        return true
    }

    fun isImageFile(f: File): Boolean {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(f.absolutePath, options)
        return options.outWidth != -1 && options.outHeight != -1
    }

    fun importFile(context: Context, name: String, fileUri: Uri, fileName: String): Boolean {
        try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val output = File(Constants.HYPER_ROOT + File.separator + name + File.separator + fileName)
            output.copyInputStreamToFile(inputStream)
            inputStream?.close()
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            return false
        }

        return true
    }

    fun humanReadableByteCount(bytes: Long): String {
        val unit = 1000
        if (bytes < unit) return bytes.toString() + " B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = "kMGTPE"[exp - 1] + ""
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
}
