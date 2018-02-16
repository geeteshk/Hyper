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
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import io.geeteshk.hyper.R
import io.geeteshk.hyper.helper.ResourceHelper
import io.geeteshk.hyper.hyperx.inflate
import kotlinx.android.synthetic.main.item_file_project.view.*
import java.io.File

class FileAdapter(context: Context, private val openFiles: List<String>) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, openFiles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView: View = convertView ?: parent.inflate(R.layout.item_file_project)
        ResourceHelper.setIcon(rootView.fileIcon, File(openFiles[position]), 0xffffffff.toInt())
        rootView.fileTitle.text = getPageTitle(position)
        rootView.fileTitle.setTextColor(0xffffffff.toInt())
        rootView.fileTitle.typeface = Typeface.DEFAULT_BOLD
        return rootView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView: View = convertView ?: parent.inflate(R.layout.item_file_project)
        ResourceHelper.setIcon(rootView.fileIcon, File(openFiles[position]), 0xffffffff.toInt())
        rootView.fileTitle.text = getPageTitle(position)
        return rootView
    }

    private fun getPageTitle(position: Int): CharSequence = File(openFiles[position]).name

    override fun getCount(): Int = openFiles.size
}
