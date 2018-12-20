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

package io.geeteshk.hyper.util.project

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import com.google.android.material.snackbar.Snackbar
import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.adapter.ProjectAdapter
import io.geeteshk.hyper.util.Constants
import io.geeteshk.hyper.util.copyInputStreamToFile
import io.geeteshk.hyper.util.editor.ProjectFiles
import io.geeteshk.hyper.util.snack
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

object ProjectManager {

    val TYPES = arrayOf("Default")

    fun generate(context: Context, name: String, author: String, description: String, keywords: String, stream: InputStream?, adapter: ProjectAdapter, view: View, type: Int) {
        var nameNew = name
        var counter = 1
        while (File(Constants.HYPER_ROOT + File.separator + nameNew).exists()) {
            nameNew = "$name($counter)"
            counter++
        }

        var status = false
        when (type) {
            0 -> status = generateDefault(context, nameNew, author, description, keywords, stream)
        }

        if (status) {
            adapter.insert(nameNew)
            view.snack(R.string.project_success, Snackbar.LENGTH_SHORT)
        } else {
            view.snack(R.string.project_fail, Snackbar.LENGTH_SHORT)
        }
    }

    private fun generateDefault(context: Context, name: String, author: String, description: String, keywords: String, stream: InputStream?): Boolean {
        val projectFile = File("${Constants.HYPER_ROOT}/$name")
        val cssFile = File(projectFile, "css")
        val jsFile = File(projectFile, "js")
        try {
            projectFile.mkdirs()
            File(projectFile, "images").mkdirs()
            File(projectFile, "fonts").mkdirs()
            cssFile.mkdirs()
            jsFile.mkdirs()

            File(projectFile, "index.html").writeText(ProjectFiles.getHtml(context, "default", name, author, description, keywords))
            File(cssFile, "style.css").writeText(ProjectFiles.getCss(context, "default"))
            File(jsFile, "main.js").writeText(ProjectFiles.getJs(context, "default"))

            if (stream == null) {
                copyIcon(context, name)
            } else {
                copyIcon(name, stream)
            }
        } catch (e: IOException) {
            Timber.e(e)
            return false
        }

        return true
    }

    fun importProject(context: Context, fileStr: String, name: String, author: String, description: String, keywords: String, adapter: ProjectAdapter, view: View) {
        val file = File(fileStr)
        var nameNew = name
        var counter = 1
        while (File(Constants.HYPER_ROOT + File.separator + nameNew).exists()) {
            nameNew = file.name + "(" + counter + ")"
            counter++
        }

        val outFile = File(Constants.HYPER_ROOT + File.separator + nameNew)
        try {
            outFile.mkdirs()
            file.copyRecursively(outFile)

            val index = File(outFile, "index.html")
            if (!index.exists()) {
                index.writeText(ProjectFiles.getHtml(context, "import", nameNew, author, description, keywords))
            }
        } catch (e: IOException) {
            Timber.e(e)
            view.snack(R.string.project_fail, Snackbar.LENGTH_SHORT)
            return
        }

        adapter.insert(nameNew)
        view.snack(R.string.project_success, Snackbar.LENGTH_SHORT)
    }

    fun isValid(string: String): Boolean = getIndexFile(string) != null

    fun deleteProject(name: String) {
        try {
            File("${Constants.HYPER_ROOT}/$name").deleteRecursively()
        } catch (e: IOException) {
            Timber.e(e)
        }

    }

    private fun getFaviconFile(dir: File) =
            dir.walkTopDown().filter { it.name == "favicon.ico" }.firstOrNull()

    fun getIndexFile(project: String) =
            File("${Constants.HYPER_ROOT}/$project").walkTopDown()
                    .filter { it.name == "index" }.firstOrNull()

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
            Timber.e(e)
        }
    }

    private fun copyIcon(name: String, stream: InputStream) {
        try {
            val output = File(Constants.HYPER_ROOT + File.separator + name + File.separator + "images" + File.separator + "favicon.ico")
            output.copyInputStreamToFile(stream)
            stream.close()
        } catch (e: Exception) {
            Timber.e(e)
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
            Timber.e(e, result.toString())
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
            output.copyInputStreamToFile(inputStream!!)
            inputStream.close()
        } catch (e: Exception) {
            Timber.e(e)
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
