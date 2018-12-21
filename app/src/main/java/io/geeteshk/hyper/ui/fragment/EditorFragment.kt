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

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.widget.Editor
import io.geeteshk.hyper.util.editor.ResourceHelper
import io.geeteshk.hyper.util.inflate
import io.geeteshk.hyper.util.string
import kotlinx.android.synthetic.main.fragment_editor.*
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class EditorFragment : Fragment() {

    private var location: String? = null
    private lateinit var file: File

    init {
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        location = arguments!!.getString("location")
        file = File(location)
        if (!file.exists()) {
            val textView = TextView(activity)
            val padding = ResourceHelper.dpToPx(activity!!, 48)
            textView.setPadding(padding, padding, padding, padding)
            textView.gravity = Gravity.CENTER
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alert_error, 0, 0, 0)
            textView.setText(R.string.file_problem)
            return textView
        }

        return container?.inflate(R.layout.fragment_editor)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filename = file.name
        fileContent.fileEnding = filename.substringAfterLast('.', "")
        if (filename.endsWith(".html") || filename == "imports.txt") {
            setSymbol(fileContent, symbolTab, "\t\t")
            setSymbol(fileContent, symbolOne, "<")
            setSymbol(fileContent, symbolTwo, "/")
            setSymbol(fileContent, symbolThree, ">")
            setSymbol(fileContent, symbolFour, "\"")
            setSymbol(fileContent, symbolFive, "=")
            setSymbol(fileContent, symbolSix, "!")
            setSymbol(fileContent, symbolSeven, "-")
            setSymbol(fileContent, symbolEight, "/")
        } else if (filename.endsWith(".css")) {
            setSymbol(fileContent, symbolTab, "\t\t\t\t")
            setSymbol(fileContent, symbolOne, "{")
            setSymbol(fileContent, symbolTwo, "}")
            setSymbol(fileContent, symbolThree, ":")
            setSymbol(fileContent, symbolFour, ",")
            setSymbol(fileContent, symbolFive, "#")
            setSymbol(fileContent, symbolSix, ".")
            setSymbol(fileContent, symbolSeven, ";")
            setSymbol(fileContent, symbolEight, "-")
        } else if (filename.endsWith(".js")) {
            setSymbol(fileContent, symbolTab, "\t\t\t\t")
            setSymbol(fileContent, symbolOne, "{")
            setSymbol(fileContent, symbolTwo, "}")
            setSymbol(fileContent, symbolThree, "(")
            setSymbol(fileContent, symbolFour, ")")
            setSymbol(fileContent, symbolFive, "!")
            setSymbol(fileContent, symbolSix, "=")
            setSymbol(fileContent, symbolSeven, ":")
            setSymbol(fileContent, symbolEight, "?")
        }

        val contents = getContents(location!!)
        fileContent.setTextHighlighted(contents)
        val finalFile = file
        fileContent.onTextChangedListener = object : Editor.OnTextChangedListener {
            override fun onTextChanged(text: String) {
                try {
                    finalFile.writeText(fileContent.string())
                } catch (e: IOException) {
                    Timber.wtf(e)
                }

            }
        }
    }

    private fun setSymbol(editor: Editor, button: Button?, symbol: String) {
        button!!.text = symbol
        button.setOnClickListener(SymbolClickListener(editor, symbol))
    }

    private fun setSymbol(editor: Editor, button: ImageButton?, symbol: String) {
        button!!.setOnClickListener(SymbolClickListener(editor, symbol))
    }

    private fun getContents(location: String): String {
        try {
            return FileInputStream(location).bufferedReader().use(BufferedReader::readText)
        } catch (e: Exception) {
            Timber.e(e)
        }

        return "Unable to read file!"
    }

    private inner class SymbolClickListener
    internal constructor(private val editor: Editor, private val symbol: String) : View.OnClickListener {

        override fun onClick(v: View) {
            val start = Math.max(editor.selectionStart, 0)
            val end = Math.max(editor.selectionEnd, 0)
            editor.text.replace(Math.min(start, end), Math.max(start, end),
                    symbol, 0, symbol.length)
        }
    }

    companion object {

        fun newInstance(args: Bundle) = EditorFragment().apply {
            arguments = args
        }
    }
}
