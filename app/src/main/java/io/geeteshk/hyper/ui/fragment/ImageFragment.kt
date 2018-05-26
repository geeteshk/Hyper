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

package io.geeteshk.hyper.ui.fragment

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.util.editor.ResourceHelper
import java.io.File

class ImageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val location = arguments!!.getString("location")
        var file: File? = null
        location?.let {
            file = File(location)
        }

        file?.let {
            if (!file!!.exists()) {
                val textView = TextView(activity)
                val padding = ResourceHelper.dpToPx(activity!!, 48)
                textView.setPadding(padding, padding, padding, padding)
                textView.gravity = Gravity.CENTER
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alert_error, 0, 0, 0)
                textView.setText(R.string.file_problem)
                return textView
            }
        }

        val drawable = BitmapDrawable(activity!!.resources, file!!.absolutePath)
        val imageView = ImageView(activity)
        val fileSize = getSize(file!!)
        imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.setImageDrawable(drawable)
        imageView.setOnClickListener {
            val snackbar = Snackbar.make(imageView, drawable.intrinsicWidth.toString() + "x" + drawable.intrinsicHeight + "px " + fileSize, Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction("OK") { snackbar.dismiss() }

            snackbar.show()
        }

        return imageView
    }

    private fun getSize(f: File): String {
        val size = f.length() / 1024
        return if (size >= 1024) {
            (size / 1024).toString() + " MB"
        } else {
            size.toString() + " KB"
        }
    }
}
