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

package io.geeteshk.hyper.ui.widget

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.text.*
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.MultiAutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.util.editor.Highlighter
import io.geeteshk.hyper.util.editor.ResourceHelper
import java.util.*

class Editor constructor(context: Context, attrs: AttributeSet? = null)
    : AppCompatMultiAutoCompleteTextView(context, attrs) {

    lateinit var fileEnding: String

    private val updateHandler = Handler()

    var onTextChangedListener: OnTextChangedListener? = null

    var updateDelay = 3000

    private var currentLine = 0
    private var lineDiff = 0

    private var fileModified = true

    private var lineRect: Rect

    private var numberPaint: Paint? = null
    private var lineShadowPaint: Paint
    private var colors: IntArray

    private val updateRunnable = Runnable {
        val e = text
        if (onTextChangedListener != null)
            onTextChangedListener!!.onTextChanged(e.toString())
        highlightWithoutChange(e)
    }

    private var hasLineNumbers = false
    private var darkTheme = false

    private var prefs: SharedPreferences? = null

    private val lines: List<CharSequence>
        get() {
            val lines = ArrayList<CharSequence>()
            val layout = layout

            if (layout != null) {
                val lineCount = layout.lineCount
                val text = layout.text

                var i = 0
                var startIndex = 0
                while (i < lineCount) {
                    val endIndex = layout.getLineEnd(i)
                    lines.add(text.subSequence(startIndex, endIndex))
                    startIndex = endIndex
                    i++
                }
            }

            return lines
        }

    private val currentCursorLine: Int
        get() {
            val selectionStart = Selection.getSelectionStart(text)
            val layout = layout

            return if (selectionStart != -1) {
                layout.getLineForOffset(selectionStart)
            } else -1

        }

    init {
        darkTheme = prefs!!.getBoolean("dark_theme_editor", false)
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        colors = context.resources.getIntArray(if (darkTheme) {
            R.array.code_dark
        } else {
            R.array.code_light
        })

        lineRect = Rect()
        hasLineNumbers = prefs!!.getBoolean("show_line_numbers", true)

        lineShadowPaint = Paint().apply {
            style = Paint.Style.FILL
            color = colors[0]
        }

        if (hasLineNumbers) {
            numberPaint = Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true
                textSize = ResourceHelper.dpToPx(context, 14).toFloat()
                textAlign = Paint.Align.RIGHT
                color = colors[1]
            }
        } else {
            val padding = ResourceHelper.dpToPx(context, 8)
            if (Build.VERSION.SDK_INT > 15) {
                setPaddingRelative(padding, padding, padding, 0)
            } else {
                setPadding(padding, padding, padding, 0)
            }
        }

        setLineSpacing(0f, 1.2f)
        setBackgroundColor(colors[2])
        setTextColor(colors[3])
        typeface = Typeface.createFromAsset(context.assets, "fonts/Inconsolata-Regular.ttf")
        setHorizontallyScrolling(true)
        customSelectionActionModeCallback = EditorCallback()
        setHorizontallyScrolling(false)
        filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            if (fileModified && end - start == 1 && start < source.length && dstart < dest.length) {
                val c = source[start]
                if (c == '\n') return@InputFilter autoIndent(source, dest, dstart, dend)
            }

            source
        })

        viewTreeObserver.addOnGlobalLayoutListener { setupAutoComplete() }
    }

    fun setTextHighlighted(text: CharSequence) {
        cancelUpdate()

        highlight(SpannableStringBuilder(text))
        fileModified = false

        if (onTextChangedListener != null) onTextChangedListener!!.onTextChanged(text.toString())
    }

    private fun cancelUpdate() {
        updateHandler.removeCallbacks(updateRunnable)
    }

    private fun highlightWithoutChange(e: Editable) {
        highlight(e)
        fileModified = false
    }

    private fun highlight(e: Editable): Editable {
        if (e.isEmpty()) return e
        Highlighter.run(context, this, e, fileEnding, darkTheme)
        return e
    }

    override fun onDraw(canvas: Canvas) {
        if (hasLineNumbers) {
            val cursorLine = currentCursorLine
            var lineBounds: Int
            val lineHeight = lineHeight
            val lineCount = lineCount
            val lines = lines

            for (i in 0 until lineCount) {
                lineBounds = getLineBounds(i - lineDiff, lineRect)
                if (lines[i].toString().endsWith("\n") || i == lineCount - 1) {
                    if (hasLineNumbers) canvas.drawText((currentLine + 1).toString(), 64f, lineBounds.toFloat(), numberPaint!!)
                    currentLine += 1
                    lineDiff = 0
                } else {
                    lineDiff += 1
                }

                if (i == cursorLine) {
                    if (hasLineNumbers) {
                        canvas.drawRect(0f, (10 + lineBounds - lineHeight).toFloat(), 72f, (lineBounds + 8).toFloat(), lineShadowPaint)
                    }
                }

                if (i == lineCount - 1) {
                    currentLine = 0
                    lineDiff = 0
                }
            }
        } else {
            val cursorLine = currentCursorLine
            val lineBounds: Int
            val lineHeight = lineHeight

            lineBounds = getLineBounds(cursorLine - lineDiff, lineRect)
            canvas.drawRect(0f, (8 + lineBounds - lineHeight).toFloat(), width.toFloat(), (lineBounds + 12).toFloat(), lineShadowPaint)
        }

        super.onDraw(canvas)
    }

    private fun autoIndent(source: CharSequence, dest: Spanned, dstart: Int, dend: Int): CharSequence {
        var indent = ""
        var istart = dstart - 1
        var iend: Int

        var dataBefore = false
        var pt = 0

        while (istart > -1) {
            val c = dest[istart]
            if (c == '\n') break
            if (c != ' ' && c != '\t') {
                if (!dataBefore) {
                    if (arrayOf('{', '+', '-', '*', '/', '%', '^', '=').contains(c)) --pt
                    dataBefore = true
                }

                if (c == '(') {
                    --pt
                } else if (c == ')') {
                    ++pt
                }
            }

            --istart
        }

        if (istart > -1) {
            val charAtCursor = dest[dstart]

            iend = ++istart
            while (iend < dend) {
                val c = dest[iend]

                if (charAtCursor != '\n' &&
                        c == '/' &&
                        iend + 1 < dend &&
                        dest[iend] == c) {
                    iend += 2
                    break
                }

                if (c != ' ' && c != '\t') break
                ++iend
            }

            indent += dest.subSequence(istart, iend)
        }

        if (pt < 0) indent += "\t"

        return source.toString() + indent
    }

    private fun setupAutoComplete() {
        val keywords = "a|address|app|applet|area|b|base|basefont|bgsound|big|blink|blockquote|body|br|button|caption|center|cite|code|col|colgroup|comment|dd|dfn|dir|div|dl|dt|em|embed|fieldset|font|form|frame|frameset|h1|h2|h3|h4|h5|h6|head|hr|html|htmlplus|hype|i|iframe|img|input|ins|del|isindex|kbd|label|legend|li|link|listing|map|marquee|menu|meta|multicol|nobr|noembed|noframes|noscript|ol|option|p|param|plaintext|pre|s|samp|script|select|small|sound|spacer|span|strike|strong|style|sub|sup|table|tbody|td|textarea|tfoot|th|thead|title|tr|tt|u|var|wbr|xmp|import"
        val items = keywords.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, items)
        setAdapter(adapter)

        threshold = 1
        setTokenizer(object : MultiAutoCompleteTextView.Tokenizer {
            override fun findTokenStart(text: CharSequence, cursor: Int): Int {
                var i = cursor

                while (i > 0 && text[i - 1] != '<') {
                    i--
                }

                return if (i < 1 || text[i - 1] != '<') {
                    cursor
                } else i
            }

            override fun findTokenEnd(text: CharSequence, cursor: Int): Int {
                var i = cursor
                val len = text.length

                while (i < len) {
                    if (text[i] == ' ' || text[i] == '>' || text[i] == '/') {
                        return i
                    } else {
                        i++
                    }
                }

                return i
            }

            override fun terminateToken(text: CharSequence): CharSequence {
                var i = text.length

                while (i > 0 && text[i - 1] == ' ') {
                    i--
                }

                return if (i > 0 && text[i - 1] == ' ') {
                    text
                } else {
                    if (text is Spanned) {
                        val sp = SpannableString(text)
                        TextUtils.copySpansFrom(text, 0, text.length, Any::class.java, sp, 0)
                        sp
                    } else {
                        text.toString() + "></" + text + ">"
                    }
                }
            }
        })

        addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                val layout = layout
                val position = selectionStart
                val line = layout.getLineForOffset(position)
                val baseline = layout.getLineBaseline(line)
                val bottom = height
                var x = layout.getPrimaryHorizontal(position).toInt()

                if (x + width / 2 > width) {
                    x = width / 2
                }

                dropDownVerticalOffset = baseline - bottom
                dropDownHorizontalOffset = x

                dropDownHeight = height / 3
                dropDownWidth = width / 2
            }

            override fun afterTextChanged(e: Editable) {
                cancelUpdate()

                if (!fileModified) return

                updateHandler.postDelayed(updateRunnable, updateDelay.toLong())
            }
        })
    }

    interface OnTextChangedListener {
        fun onTextChanged(text: String)
    }

    private inner class EditorCallback : ActionMode.Callback {

        private val selectedString: String
            get() = text.toString().substring(selectionStart, selectionEnd)

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.add(0, 1, 3, R.string.refactor)
            menu.add(0, 2, 3, R.string.comment)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                1 -> {
                    val selected = selectedString
                    val layout = View.inflate(context, R.layout.dialog_refactor, null)

                    val replaceFrom = layout.findViewById<EditText>(R.id.replaceFrom)
                    val replaceTo = layout.findViewById<EditText>(R.id.replaceTo)
                    replaceFrom.setText(selected)

                    val dialog = AlertDialog.Builder(context, if (darkTheme) R.style.Hyper_Dark else R.style.Hyper)
                            .setView(layout)
                            .setPositiveButton(R.string.replace, null)
                            .create()

                    dialog.show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val replaceFromStr = replaceFrom.text.toString()
                        val replaceToStr = replaceTo.text.toString()

                        when {
                            replaceFromStr.isEmpty() -> replaceFrom.error = context.getString(R.string.empty_field_no_no)
                            replaceToStr.isEmpty() -> replaceTo.error = context.getString(R.string.empty_field_no_no)
                            else -> {
                                setText(text.toString().replace(replaceFromStr, replaceToStr))
                                dialog.dismiss()
                            }
                        }
                    }

                    return true
                }

                2 -> {
                    var startComment = ""
                    var endComment = ""
                    when (fileEnding) {

                        "html" -> {
                            startComment = "<!-- "
                            endComment = " -->"
                        }

                        "css" -> {
                            startComment = "/* "
                            endComment = " */"
                        }

                        "js" -> {
                            startComment = "/** "
                            endComment = " */"
                        }
                    }

                    text = text.insert(selectionStart, startComment).insert(selectionEnd, endComment)
                    return true
                }
            }

            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {}
    }
}