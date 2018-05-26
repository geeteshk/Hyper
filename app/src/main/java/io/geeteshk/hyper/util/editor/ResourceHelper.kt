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

package io.geeteshk.hyper.util.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.util.TypedValue
import android.widget.ImageView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.util.project.ProjectManager
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

    fun setIcon(image: ImageView, file: File, filter: Int) {
        image.clearColorFilter()
        val fileName = file.name
        if (file.isDirectory) setWithFilter(image, R.drawable.ic_folder, filter)
        else if (ProjectManager.isImageFile(file)) setWithFilter(image, R.drawable.ic_image, filter)
        else if (ProjectManager.isBinaryFile(file)) setWithFilter(image, R.drawable.ic_binary, filter)
        else if (fileName.endsWith(".html")) image.setImageResource(R.drawable.ic_html)
        else if (fileName.endsWith(".css")) image.setImageResource(R.drawable.ic_css)
        else if (fileName.endsWith(".js")) image.setImageResource(R.drawable.ic_js)
        else if (fileName.endsWith(".woff") || fileName.endsWith(".ttf") || fileName.endsWith(".otf") || fileName.endsWith(".woff2") || fileName.endsWith(".fnt")) setWithFilter(image, R.drawable.ic_font, filter)
        else setWithFilter(image, R.drawable.ic_file, filter)
    }

    private fun setWithFilter(image: ImageView, icon: Int, filter: Int) {
        image.setImageResource(icon)
        image.colorFilter = PorterDuffColorFilter(filter, PorterDuff.Mode.SRC_ATOP)
    }

    fun dpToPx(context: Context, dp: Int): Int {
        val r = context.resources
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics))
    }

}
