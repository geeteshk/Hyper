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

package io.geeteshk.hyper.widget

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.support.v7.widget.AppCompatTextView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import io.geeteshk.hyper.helper.ResourceHelper
import java.util.regex.Pattern

class DiffView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    private lateinit var rectPaint: Paint

    init {
        init(context)
    }

    private fun init(context: Context) {
        typeface = Typeface.createFromAsset(context.assets, "fonts/Consolas.ttf")
        setBackgroundColor(-0xcccccd)
        setTextColor(-0x1)
        textSize = ResourceHelper.dpToPx(context, 4).toFloat()

        rectPaint = Paint()
        rectPaint.color = -0xbbbbbc
        rectPaint.style = Paint.Style.FILL
        rectPaint.isAntiAlias = true
    }

    fun setDiffText(text: SpannableString) {
        setText(highlight(text))
    }

    private fun highlight(input: SpannableString): SpannableString {
        run {
            val m = REMOVE_CHANGES.matcher(input)
            while (m.find()) {
                input.setSpan(ForegroundColorSpan(-0x3eaff), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        run {
            val m = ADD_CHANGES.matcher(input)
            while (m.find()) {
                input.setSpan(ForegroundColorSpan(-0xff009a), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        run {
            val m = LINE_CHANGES.matcher(input)
            while (m.find()) {
                input.setSpan(ForegroundColorSpan(-0x1f9901), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        run {
            val m = INFO_CHANGES.matcher(input)
            while (m.find()) {
                input.setSpan(ForegroundColorSpan(-0x1a00), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        run {
            val m = INFO_CHANGES_TWO.matcher(input)
            while (m.find()) {
                input.setSpan(ForegroundColorSpan(-0x1a00), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        run {
            val m = INFO_CHANGES_THREE.matcher(input)
            while (m.find()) {
                input.setSpan(ForegroundColorSpan(-0x1a00), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        val m = INFO_CHANGES_FOUR.matcher(input)
        while (m.find()) {
            input.setSpan(ForegroundColorSpan(-0x1a00), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return input
    }

    companion object {

        private val INFO_CHANGES = Pattern.compile("/\\**?\\*/|diff.*")
        private val INFO_CHANGES_TWO = Pattern.compile("/\\**?\\*/|index.*")
        private val INFO_CHANGES_THREE = Pattern.compile("/\\**?\\*/|\\+\\+\\+.*")
        private val INFO_CHANGES_FOUR = Pattern.compile("/\\**?\\*/|---.*")
        private val LINE_CHANGES = Pattern.compile("/\\**?\\*/|@@.*")
        private val ADD_CHANGES = Pattern.compile("/\\**?\\*/|\\+.*")
        private val REMOVE_CHANGES = Pattern.compile("/\\**?\\*/|-.*")
    }
}
