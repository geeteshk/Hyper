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

package io.geeteshk.hyper.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.widget.holder.TagTreeHolder
import io.geeteshk.hyper.util.snack
import kotlinx.android.synthetic.main.activity_view.*
import kotlinx.android.synthetic.main.widget_toolbar.*
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

class ViewActivity : ThemedActivity() {

    private lateinit var htmlDoc: Document
    private lateinit var htmlFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val htmlPath = intent.getStringExtra("html_path")
        htmlFile = File(htmlPath)
        val rootNode = TreeNode.root()
        try {
            setupViewTree(rootNode, htmlFile)
        } catch (e: IOException) {
            Timber.e(e)
        }

        setSupportActionBar(toolbar)
        toolbar.title = htmlFile.name
        toolbar.subtitle = htmlFile.path.substring(htmlFile.path.indexOf("Hyper/") + 6)

        val treeView = AndroidTreeView(this@ViewActivity, rootNode)
        treeView.setDefaultAnimation(true)
        treeView.setDefaultViewHolder(TagTreeHolder::class.java)
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle)
        viewLayout.addView(treeView.view)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                try {
                    FileUtils.writeStringToFile(htmlFile, htmlDoc.outerHtml(), Charset.defaultCharset(), false)
                } catch (e: IOException) {
                    Timber.e(e)
                }

                setResult(Activity.RESULT_OK)
                viewLayout.snack(R.string.save_changes_done, Snackbar.LENGTH_SHORT)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @Throws(IOException::class)
    private fun setupViewTree(root: TreeNode, html: File) {
        htmlDoc = Jsoup.parse(html, "UTF-8")

        val head = htmlDoc.head()
        val headNode = TreeNode(TagTreeHolder.TagTreeItem(head))
        setupElementTree(headNode, head)
        root.addChild(headNode)

        val body = htmlDoc.body()
        val bodyNode = TreeNode(TagTreeHolder.TagTreeItem(body))
        setupElementTree(bodyNode, body)
        root.addChild(bodyNode)
    }

    private fun setupElementTree(root: TreeNode, element: Element) {
        val children = element.children()
        for (child in children) {
            val elementNode = TreeNode(TagTreeHolder.TagTreeItem(child))
            setupElementTree(elementNode, child)
            root.addChild(elementNode)
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this@ViewActivity)
                .setTitle("Save changes?")
                .setMessage("This will append any changes to the html file. If you choose to discard unsaved changes will not be saved.")
                .setPositiveButton("SAVE") { _, _ ->
                    try {
                        FileUtils.writeStringToFile(htmlFile, htmlDoc.outerHtml(), Charset.defaultCharset(), false)
                    } catch (e: IOException) {
                        Timber.e(e)
                    }

                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .setNegativeButton("DISCARD") { _, _ -> finish() }
                .show()
    }
}
