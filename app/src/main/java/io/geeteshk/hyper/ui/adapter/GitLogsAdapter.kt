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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.geeteshk.hyper.R
import io.geeteshk.hyper.extensions.inflate
import io.geeteshk.hyper.extensions.snack
import kotlinx.android.synthetic.main.item_git_log.view.*
import org.eclipse.jgit.revwalk.RevCommit

class GitLogsAdapter(private val gitLogs: List<RevCommit>?) : RecyclerView.Adapter<GitLogsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.inflate(R.layout.item_git_log)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(gitLogs!![position])

    override fun getItemCount(): Int = gitLogs?.size ?: 0

    inner class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        fun bind(commit: RevCommit) {
            with (view) {
                val msg = SpannableString(commit.fullMessage)
                var index = commit.fullMessage.indexOf('\n') + 1
                val fullShown = booleanArrayOf(false)

                if (index == 0) index = commit.fullMessage.length


                msg.setSpan(StyleSpan(Typeface.BOLD), 0, index, 0)
                setOnClickListener {
                    if (!fullShown[0]) {
                        commitName.typeface = Typeface.DEFAULT
                        commitName.text = msg
                        fullShown[0] = true
                    } else {
                        commitName.typeface = Typeface.DEFAULT_BOLD
                        commitName.text = commit.shortMessage
                        fullShown[0] = false
                    }
                }

                setOnLongClickListener {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("hash", commit.id.name)
                    clipboard.primaryClip = clip
                    snack(R.string.commit_hash_copy, Snackbar.LENGTH_SHORT)
                    true
                }

                commitName.text = commit.shortMessage
                commitName.typeface = Typeface.DEFAULT_BOLD
                commitAuthor.text = context.getString(R.string.git_user_format, commit.authorIdent.name, commit.authorIdent.emailAddress)
                commitDate.text = commit.authorIdent.`when`.toString()
                commitHash.text = commit.id.name
            }
        }
    }
}
