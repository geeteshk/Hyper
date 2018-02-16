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

package io.geeteshk.hyper.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.hyperx.inflate
import kotlinx.android.synthetic.main.item_git_log.view.*
import org.eclipse.jgit.revwalk.RevCommit

class GitLogsAdapter(private val context: Context, private val gitLogs: List<RevCommit>?) : RecyclerView.Adapter<GitLogsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.inflate(R.layout.item_git_log)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val commit = gitLogs!![position]
        val string = SpannableString(commit.fullMessage)
        var index = commit.fullMessage.indexOf('\n') + 1
        if (index == 0) index = commit.fullMessage.length
        val fullShown = booleanArrayOf(false)
        string.setSpan(StyleSpan(Typeface.BOLD), 0, index, 0)
        holder.view.setOnClickListener {
            if (!fullShown[0]) {
                holder.commitName.typeface = Typeface.DEFAULT
                holder.commitName.text = string
                fullShown[0] = true
            } else {
                holder.commitName.typeface = Typeface.DEFAULT_BOLD
                holder.commitName.text = commit.shortMessage
                fullShown[0] = false
            }
        }

        holder.view.setOnLongClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("hash", commit.id.name)
            clipboard.primaryClip = clip
            Snackbar.make(holder.view, R.string.commit_hash_copy, Snackbar.LENGTH_SHORT).show()
            true
        }

        holder.commitName.text = commit.shortMessage
        holder.commitName.typeface = Typeface.DEFAULT_BOLD
        holder.commitAuthor.text = context.getString(R.string.git_user_format, commit.authorIdent.name, commit.authorIdent.emailAddress)
        holder.commitDate.text = commit.authorIdent.`when`.toString()
        holder.commitHash.text = commit.id.name
    }

    override fun getItemCount(): Int = gitLogs?.size ?: 0

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        var commitName: TextView = view.commitName
        var commitDate: TextView = view.commitDate
        var commitAuthor: TextView = view.commitAuthor
        var commitHash: TextView = view.commitHash
    }
}
