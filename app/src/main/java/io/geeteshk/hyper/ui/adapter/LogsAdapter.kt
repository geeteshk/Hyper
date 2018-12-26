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

package io.geeteshk.hyper.ui.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.extensions.inflate
import kotlinx.android.synthetic.main.item_log.view.*

class LogsAdapter(private val localWithoutIndex: String, private val jsLogs: List<ConsoleMessage>, private val darkTheme: Boolean) : RecyclerView.Adapter<LogsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView = parent.inflate(R.layout.item_log)
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(jsLogs[position])

    override fun getItemCount(): Int = jsLogs.size

    inner class ViewHolder(private var v: View) : RecyclerView.ViewHolder(v) {

        fun bind(consoleMessage: ConsoleMessage) {
            with (v) {
                val newId = consoleMessage.sourceId().replace(localWithoutIndex, "") + ":" + consoleMessage.lineNumber()
                val msg = consoleMessage.message()
                val msgLevel = consoleMessage.messageLevel().name.substring(0, 1)

                logLevel.text = msgLevel
                logLevel.setTextColor(getLogColor(consoleMessage.messageLevel()))
                logMessage.text = msg
                logMessage.setTextColor(if (darkTheme) { Color.WHITE } else { Color.BLACK })
                logDetails.text = newId
            }
        }

        @ColorInt
        private fun getLogColor(messageLevel: ConsoleMessage.MessageLevel): Int = when (messageLevel) {
            ConsoleMessage.MessageLevel.LOG -> if (darkTheme) {
                Color.WHITE
            } else {
                -0x79000000
            }

            ConsoleMessage.MessageLevel.TIP -> -0x83b201
            ConsoleMessage.MessageLevel.DEBUG -> -0xff198a
            ConsoleMessage.MessageLevel.ERROR -> -0xadae
            ConsoleMessage.MessageLevel.WARNING -> -0x3c00
        }
    }
}
