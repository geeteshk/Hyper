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
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.MultiAutoCompleteTextView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.util.editor.ResourceHelper
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class Editor @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatMultiAutoCompleteTextView(context, attrs) {

    private val updateHandler = Handler()

    var onTextChangedListener: OnTextChangedListener? = null

    var updateDelay = 2000

    private var currentLine = 0
    private var lineDiff = 0

    private var codeType: CodeType? = null

    private var fileModified = true

    private var lineRect: Rect? = null

    private var numberPaint: Paint? = null
    private var lineShadowPaint: Paint? = null
    private var isHighlighting: Boolean = false
    private var colors: Colors? = null
    private var patterns: Patterns? = null

    private val updateRunnable = Runnable {
        if (!isHighlighting) {
            val e = text
            if (onTextChangedListener != null)
                onTextChangedListener!!.onTextChanged(e.toString())
            highlightWithoutChange(e)
        }
    }
    private var hasLineNumbers: Boolean = false

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
        init()
    }

    fun setTextHighlighted(text: CharSequence) {
        cancelUpdate()

        fileModified = false
        setText(highlight(SpannableStringBuilder(text)))
        fileModified = true

        if (onTextChangedListener != null) onTextChangedListener!!.onTextChanged(text.toString())
    }

    private fun init() {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        colors = Colors(!prefs!!.getBoolean("dark_theme_editor", false))
        patterns = Patterns()
        lineRect = Rect()
        hasLineNumbers = prefs!!.getBoolean("show_line_numbers", true)

        lineShadowPaint = Paint()
        lineShadowPaint!!.style = Paint.Style.FILL
        lineShadowPaint!!.color = colors!!.getColorLineShadow()

        if (hasLineNumbers) {
            numberPaint = Paint()
            numberPaint!!.style = Paint.Style.FILL
            numberPaint!!.isAntiAlias = true
            numberPaint!!.textSize = ResourceHelper.dpToPx(context, 14).toFloat()
            numberPaint!!.textAlign = Paint.Align.RIGHT
            numberPaint!!.color = colors!!.getColorNumber()
        } else {
            val padding = ResourceHelper.dpToPx(context, 8)
            if (Build.VERSION.SDK_INT > 15) {
                setPaddingRelative(padding, padding, padding, 0)
            } else {
                setPadding(padding, padding, padding, 0)
            }
        }

        setLineSpacing(0f, 1.2f)
        setBackgroundColor(colors!!.getColorBackground())
        setTextColor(colors!!.getColorText())
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

    private fun cancelUpdate() {
        updateHandler.removeCallbacks(updateRunnable)
    }

    private fun highlightWithoutChange(e: Editable) {
        fileModified = false
        highlight(e)
        fileModified = true
    }

    private fun highlight(e: Editable): Editable {
        isHighlighting = true

        try {
            if (e.isEmpty()) return e
            if (hasSpans(e)) clearSpans(e)

            var m: Matcher
            when (codeType) {
                Editor.CodeType.HTML -> {
                    m = patterns!!.patternKeywords.matcher(e)
                    while (m.find()) {
                        if (e.toString()[m.start() - 1] == '<' || e.toString()[m.start() - 1] == '/') {
                            e.setSpan(ForegroundColorSpan(colors!!.colorKeyword), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }

                    m = patterns!!.patternBuiltins.matcher(e)
                    while (m.find()) {
                        if (e.toString()[m.start() - 1] == ' ' && e.toString()[m.end()] == '=') {
                            e.setSpan(ForegroundColorSpan(colors!!.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }

                    m = patterns!!.patternStrings.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.getColorStrings()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    m = patterns!!.patternComments.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.getColorComment()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                Editor.CodeType.CSS -> {
                    m = patterns!!.patternKeywords.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.colorKeyword), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    m = patterns!!.patternParams.matcher(e)
                    while (m.find()) {
                        if (e.toString()[m.end()] == ':') {
                            e.setSpan(ForegroundColorSpan(colors!!.colorParams), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }

                    run {
                        var index = e.toString().indexOf(":")
                        while (index >= 0) {
                            e.setSpan(ForegroundColorSpan(colors!!.colorEnding), index + 1, e.toString().indexOf(";", index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            index = e.toString().indexOf(":", index + 1)
                        }
                    }

                    run {
                        var index = e.toString().indexOf(".")
                        while (index >= 0) {
                            e.setSpan(ForegroundColorSpan(colors!!.getColorBuiltin()), index + 1, e.toString().indexOf("{", index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            index = e.toString().indexOf(".", index + 1)
                        }
                    }

                    run {
                        var index = e.toString().indexOf("#")
                        while (index >= 0) {
                            e.setSpan(ForegroundColorSpan(colors!!.getColorBuiltin()), index + 1, e.toString().indexOf("{", index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            index = e.toString().indexOf("#", index + 1)
                        }
                    }

                    m = patterns!!.patternEndings.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.colorEnding), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    m = patterns!!.patternStrings.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.getColorStrings()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    m = patterns!!.patternCommentsOther.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.getColorComment()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                Editor.CodeType.JS -> {
                    m = patterns!!.patternDatatypes.matcher(e)
                    while (m.find()) {
                        if (e.toString()[m.end()] == ' ') {
                            e.setSpan(ForegroundColorSpan(colors!!.colorParams), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }

                    m = patterns!!.patternFunctions.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.colorFunctions), m.start() + 2, m.end() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    m = patterns!!.patternSymbols.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.colorKeyword), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    var index = e.toString().indexOf("null")
                    while (index >= 0) {
                        e.setSpan(ForegroundColorSpan(colors!!.colorEnding), index, index + 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        index = e.toString().indexOf("null", index + 1)
                    }

                    m = patterns!!.patternNumbers.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    m = patterns!!.patternBooleans.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.getColorBuiltin()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    m = patterns!!.patternStrings.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.getColorStrings()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    m = patterns!!.patternCommentsOther.matcher(e)
                    while (m.find()) {
                        e.setSpan(ForegroundColorSpan(colors!!.getColorComment()), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }

        isHighlighting = false
        return e
    }

    fun setType(type: CodeType) {
        codeType = type
    }

    private fun clearSpans(e: Editable) {
        run {
            val spans = e.getSpans(0, e.length, ForegroundColorSpan::class.java)
            var n = spans.size
            while (n-- > 0) e.removeSpan(spans[n])
        }
    }

    private fun hasSpans(e: Editable): Boolean =
            e.getSpans(0, e.length, ForegroundColorSpan::class.java).isNotEmpty()

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
                        canvas.drawRect(0f, (10 + lineBounds - lineHeight).toFloat(), 72f, (lineBounds + 8).toFloat(), lineShadowPaint!!)
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
            canvas.drawRect(0f, (8 + lineBounds - lineHeight).toFloat(), width.toFloat(), (lineBounds + 12).toFloat(), lineShadowPaint!!)
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

            if (c == '\n')
                break

            if (c != ' ' && c != '\t') {
                if (!dataBefore) {
                    if (c == '{' ||
                            c == '+' ||
                            c == '-' ||
                            c == '*' ||
                            c == '/' ||
                            c == '%' ||
                            c == '^' ||
                            c == '=')
                        --pt

                    dataBefore = true
                }

                if (c == '(')
                    --pt
                else if (c == ')')
                    ++pt
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

                if (c != ' ' && c != '\t')
                    break
                ++iend
            }

            indent += dest.subSequence(istart, iend)
        }

        if (pt < 0)
            indent += "\t"

        return source.toString() + indent
    }

    private fun setupAutoComplete() {
        val items = patterns!!.patternKeywords.pattern().replace("(", "").replace(")", "").substring(2, patterns!!.patternKeywords.pattern().length - 2).split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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

    enum class CodeType {
        HTML, CSS, JS
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

                    val dialog = AlertDialog.Builder(context, if (prefs!!.getBoolean("dark_theme", false)) R.style.Hyper_Dark else R.style.Hyper)
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
                    when (codeType) {
                        Editor.CodeType.HTML -> {
                            startComment = "<!-- "
                            endComment = " -->"
                        }
                        Editor.CodeType.CSS -> {
                            startComment = "/* "
                            endComment = " */"
                        }
                        Editor.CodeType.JS -> {
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

        override fun onDestroyActionMode(mode: ActionMode) {

        }
    }

    internal inner class Colors(private val darkTheme: Boolean) {
        val colorKeyword = -0x6d98e
        val colorParams = -0x9b340c
        val colorEnding = -0x658623
        val colorFunctions = -0x12a400

        private val colorBuiltin = -0x8d5000
        private val colorComment = -0x5f5f60
        private val colorStrings = -0x12a400

        private val colorBuiltinDark = -0x591dd2
        private val colorCommentDark = -0x8a8ea2
        private val colorStringsDark = -0x19248c

        private val colorLineShadow = 0x10000000
        private val colorNumber = -0x5f5f60
        private val colorBackground = -0x70708
        private val colorText = -0xddddde

        private val colorLineShadowDark = 0x10FFFFFF
        private val colorNumberDark = -0x2c2c2d
        private val colorBackgroundDark = -0xddddde
        private val colorTextDark = -0x70708

        fun getColorBuiltin(): Int = if (darkTheme) colorBuiltin else colorBuiltinDark

        fun getColorComment(): Int = if (darkTheme) colorComment else colorCommentDark

        fun getColorStrings(): Int = if (darkTheme) colorStrings else colorStringsDark

        fun getColorLineShadow(): Int = if (darkTheme) colorLineShadow else colorLineShadowDark

        fun getColorNumber(): Int = if (darkTheme) colorNumber else colorNumberDark

        fun getColorBackground(): Int = if (darkTheme) colorBackground else colorBackgroundDark

        fun getColorText(): Int = if (darkTheme) colorText else colorTextDark
    }

    internal inner class Patterns {
        val patternKeywords = Pattern.compile("\\b(a|address|app|applet|area|b|base|basefont|bgsound|big|blink|blockquote|body|br|button|caption|center|cite|code|col|colgroup|comment|dd|dfn|dir|div|dl|dt|em|embed|fieldset|font|form|frame|frameset|h1|h2|h3|h4|h5|h6|head|hr|html|htmlplus|hype|i|iframe|img|input|ins|del|isindex|kbd|label|legend|li|link|listing|map|marquee|menu|meta|multicol|nobr|noembed|noframes|noscript|ol|option|p|param|plaintext|pre|s|samp|script|select|small|sound|spacer|span|strike|strong|style|sub|sup|table|tbody|td|textarea|tfoot|th|thead|title|tr|tt|u|var|wbr|xmp|import)\\b")!!
        val patternBuiltins = Pattern.compile("\\b(charset|lang|href|onclick|onmouseover|onmouseout|code|codebase|width|height|align|vspace|hspace|name|archive|mayscript|alt|shape|coords|target|nohref|size|color|face|src|loop|bgcolor|background|text|vlink|alink|bgproperties|topmargin|leftmargin|marginheight|marginwidth|onload|onunload|onfocus|onblur|stylesrc|scroll|clear|type|value|valign|span|compact|pluginspage|pluginurl|hidden|autostart|playcount|volume|controller|mastersound|starttime|endtime|point-size|weight|action|method|enctype|onsubmit|onreset|scrolling|noresize|frameborder|bordercolor|cols|rows|framespacing|border|noshade|longdesc|ismap|usemap|lowsrc|naturalsizeflag|nosave|dynsrc|controls|start|suppress|maxlength|checked|language|onchange|onkeypress|onkeyup|onkeydown|autocomplete|prompt|for|rel|rev|media|direction|behaviour|scrolldelay|scrollamount|http-equiv|content|gutter|defer|event|multiple|readonly|cellpadding|cellspacing|rules|bordercolorlight|bordercolordark|summary|colspan|rowspan|nowrap|halign|disabled|accesskey|tabindex|id|class)\\b")!!
        val patternParams = Pattern.compile("\\b(azimuth|background-attachment|background-color|background-image|background-position|background-repeat|background|border-collapse|border-color|border-spacing|border-style|border-top|border-right|border-bottom|border-left|border-top-color|border-right-color|border-left-color|border-bottom-color|border-top-style|border-right-style|border-bottom-style|border-left-style|border-top-width|border-right-width|border-bottom-width|border-left-width|border-width|border|bottom|caption-side|clear|clip|color|content|counter-increment|counter-reset|cue-after|cue-before|cue|cursor|direction|display|elevation|empty-cells|float|font-family|font-size|font-style|font-variant|font-weight|font|height|left|letter-spacing|line-height|list-style-image|list-style-position|list-style-type|list-style|margin-left|margin-right|margin-top|margin-bottom|margin|max-height|max-width|min-height|min-width|orphans|outline-color|outline-style|outline-width|outline|overflow|padding-top|padding-right|padding-bottom|padding-left|padding|page-break-after|page-break-before|page-break-inside|pause-after|pause-before|pause|pitch-range|pitch|play-during|position|quotes|richness|right|speak-header|speak-numeral|speak-punctuation|speak|speech-rate|stress|table-layout|text-align|text-decoration|text-indent|text-transform|top|unicode-bidi|vertical-align|visibility|voice-family|volume|white-space|widows|width|word-spacing|z-index)\\b")!!
        val patternComments = Pattern.compile("/\\**?\\*/|<!--.*")!!
        val patternCommentsOther = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/|//.*")!!
        val patternEndings = Pattern.compile("(em|rem|px|pt|%)")!!
        val patternDatatypes = Pattern.compile("\\b(abstract|arguments|boolean|byte|char|class|const|double|enum|final|float|function|int|interface|long|native|package|private|protected|public|short|static|synchronized|transient|var|void|volatile)\\b")!!
        val patternSymbols = Pattern.compile("(&|=|throw|new|for|if|else|>|<|^|\\+|-|\\s\\|\\s|break|try|catch|do|!|finally|default|case|switch|native|let|super|throws|return)")!!
        val patternFunctions = Pattern.compile("n\\((.*?)\\)")!!
        val patternNumbers = Pattern.compile("\\b(\\d*[.]?\\d+)\\b")!!
        val patternBooleans = Pattern.compile("\\b(true|false)\\b")!!
        val patternStrings = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1")!!
    }

    companion object {

        private val TAG = Editor::class.java.simpleName
    }
}