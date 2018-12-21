package io.geeteshk.hyper.util.editor

import android.content.Context
import android.text.Editable
import android.text.style.ForegroundColorSpan
import io.geeteshk.hyper.ui.widget.Editor
import io.geeteshk.hyper.util.color
import io.geeteshk.hyper.util.json.ColorReader
import timber.log.Timber

object Highlighter {

    private var currentThread: HighlightThread? = null

    fun run(context: Context, codeView: Editor, editable: Editable, fileEnding: String, darkTheme: Boolean) {
        if (currentThread != null && currentThread!!.isAlive) {
            currentThread!!.interrupt()

            val defs = ColorReader.getColorDefs(context, "$fileEnding.json")
            currentThread = HighlightThread(codeView, defs, editable, darkTheme)
            currentThread!!.start()
        }
    }

    class HighlightThread(private var codeView: Editor, private var defs: ArrayList<ColorReader.ColorDef>,
                          private var editable: Editable, private var darkTheme: Boolean) : Thread() {

        override fun run() {
            super.run()
            try {
                clearSpans(editable)
                defs.forEach { editable.color(it.pattern, if (darkTheme) { it.dark } else { it.color }) }
                codeView.text = editable
            } catch (e: InterruptedException) {
                Timber.d(e)
            }
        }

        private fun clearSpans(e: Editable) {
            run {
                val spans = e.getSpans(0, e.length, ForegroundColorSpan::class.java)
                var n = spans.size
                while (n-- > 0) e.removeSpan(spans[n])
            }
        }
    }
}