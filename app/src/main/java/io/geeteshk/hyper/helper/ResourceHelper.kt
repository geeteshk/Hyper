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
import android.util.TypedValue
import io.geeteshk.hyper.R
import java.io.File

object ResourceHelper {

    fun decodeUri(context: Context, selectedImage: Uri): Bitmap? {
        try {
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(selectedImage), null, o)

            val requiredSize = 140
            var widthTmp = o.outWidth
            var heightTmp = o.outHeight
            var scale = 1
            while (true) {
                if (widthTmp / 2 < requiredSize || heightTmp / 2 < requiredSize) {
                    break
                }
                widthTmp /= 2
                heightTmp /= 2
                scale *= 2
            }

            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            return BitmapFactory.decodeStream(context.contentResolver.openInputStream(selectedImage), null, o2)
        } catch (e: Exception) {
            return null
        }

    }

    fun getIcon(file: File): Int {
        val fileName = file.name
        if (file.isDirectory) return R.drawable.ic_folder
        if (ProjectManager.isImageFile(file)) return R.drawable.ic_image
        if (ProjectManager.isBinaryFile(file)) return R.drawable.ic_binary
        if (fileName.endsWith(".html")) return R.drawable.ic_html
        if (fileName.endsWith(".css")) return R.drawable.ic_css
        if (fileName.endsWith(".js")) return R.drawable.ic_js
        return if (fileName.endsWith(".woff") || fileName.endsWith(".ttf") || fileName.endsWith(".otf") || fileName.endsWith(".woff2") || fileName.endsWith(".fnt")) R.drawable.ic_font else R.drawable.ic_file
    }

    fun dpToPx(context: Context, dp: Int): Int {
        val r = context.resources
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics))
    }

}
