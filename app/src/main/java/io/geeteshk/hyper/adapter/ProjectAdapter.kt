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

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.activity.ProjectActivity
import io.geeteshk.hyper.helper.HTMLParser
import io.geeteshk.hyper.helper.ProjectManager
import io.geeteshk.hyper.helper.inflate
import kotlinx.android.synthetic.main.item_project.view.*
import java.util.*

class ProjectAdapter(private val context: Context, private val projects: ArrayList<String>, private val layout: CoordinatorLayout, private val recyclerView: RecyclerView) : RecyclerView.Adapter<ProjectAdapter.MyViewHolder>() {

    fun insert(project: String) {
        projects.add(project)
        val position = projects.indexOf(project)
        notifyItemInserted(position)
        recyclerView.scrollToPosition(position)
    }

    fun remove(position: Int) {
        projects.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        Collections.sort(projects)
        val itemView = parent.inflate(R.layout.item_project)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val properties = HTMLParser.getProperties(projects[holder.adapterPosition])
        holder.title.text = properties[0]
        holder.author.text = properties[1]
        holder.description.text = properties[2]
        holder.favicon.setImageBitmap(ProjectManager.getFavicon(context, projects[holder.adapterPosition]))

        holder.layout.setOnClickListener {
            val intent = Intent(context, ProjectActivity::class.java)
            intent.putExtra("project", projects[holder.adapterPosition])
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

            if (Build.VERSION.SDK_INT >= 21) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            }

            (context as AppCompatActivity).startActivityForResult(intent, 0)
        }

        holder.layout.setOnLongClickListener {
            AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.delete) + " " + projects[holder.adapterPosition] + "?")
                    .setMessage(R.string.change_undone)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        val project = projects[holder.adapterPosition]
                        ProjectManager.deleteProject(project)
                        remove(holder.adapterPosition)

                        Snackbar.make(
                                layout,
                                "Deleted $project.",
                                Snackbar.LENGTH_LONG
                        ).show()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()

            true
        }
    }

    override fun getItemCount(): Int = projects.size

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var title: TextView = view.title
        var description: TextView = view.desc
        var author: TextView = view.author
        var favicon: ImageView = view.favicon
        var layout: LinearLayout = view.projectLayout
    }
}
