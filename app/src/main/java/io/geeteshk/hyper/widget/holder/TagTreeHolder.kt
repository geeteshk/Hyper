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

package io.geeteshk.hyper.widget.holder

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.PopupMenu
import com.unnamed.b.atv.model.TreeNode
import io.geeteshk.hyper.R
import io.geeteshk.hyper.adapter.AttrsAdapter
import io.geeteshk.hyper.text.HtmlCompat
import kotlinx.android.synthetic.main.dialog_element_add.view.*
import kotlinx.android.synthetic.main.dialog_element_info.view.*
import kotlinx.android.synthetic.main.dialog_input_single.view.*
import kotlinx.android.synthetic.main.item_element.view.*
import org.jsoup.nodes.Element

class TagTreeHolder(context: Context) : TreeNode.BaseNodeViewHolder<TagTreeHolder.TagTreeItem>(context) {

    override fun createNodeView(node: TreeNode, value: TagTreeItem): View {
        val view = View.inflate(context, R.layout.item_element, null)

        val element = HtmlCompat.fromHtml("\t&lt;<font color=\"#f92672\">" + value.element.tagName() + "</font>&gt;")
        view.elementName.text = element

        if (node.isLeaf) {
            view.elementArrow.visibility = View.GONE
        }

        if (!node.isExpanded) {
            view.elementArrow.rotation = -90f
        }

        view.elementOverflow.setOnClickListener {
            val menu = PopupMenu(context, view.elementOverflow)
            menu.menuInflater.inflate(R.menu.menu_tag_options, menu.menu)
            if (value.element.tagName() == "head" || value.element.tagName() == "body") {
                menu.menu.findItem(R.id.action_tag_remove).isVisible = false
            }

            if (!value.element.isBlock) {
                menu.menu.findItem(R.id.action_tag_add).isVisible = false
            }

            menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_tag_add -> {
                        val rootView = View.inflate(context, R.layout.dialog_element_add, null)

                        val dialog = AlertDialog.Builder(context)
                                .setTitle("Add to " + value.element.tagName())
                                .setView(rootView)
                                .setPositiveButton("ADD", null)
                                .setNegativeButton("CANCEL", null)
                                .create()

                        dialog.show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            if (!rootView.nameText.text.toString().isEmpty()) {
                                tView.addNode(node, TreeNode(TagTreeItem(value.element.appendElement(rootView.nameText.text.toString()).text(rootView.textText.text.toString()))))
                                dialog.dismiss()
                            } else {
                                rootView.nameText.error = "Please enter a name for the element."
                            }
                        }
                        return@OnMenuItemClickListener true
                    }
                    R.id.action_tag_edit -> {
                        val editElement = value.element
                        val infoView = View.inflate(context, R.layout.dialog_element_info, null)
                        val adapter = AttrsAdapter(context, editElement)
                        val manager = LinearLayoutManager(context)
                        infoView.elementAttrs.layoutManager = manager
                        infoView.elementAttrs.addItemDecoration(DividerItemDecoration(context, manager.orientation))
                        infoView.elementAttrs.setHasFixedSize(true)
                        infoView.elementAttrs.adapter = adapter
                        infoView.elementTag.text = editElement.tagName()
                        infoView.tagEdit.setOnClickListener {
                            val viewElement = View.inflate(context, R.layout.dialog_input_single, null)
                            viewElement.inputText.hint = "Value"
                            viewElement.inputText.setSingleLine(true)
                            viewElement.inputText.maxLines = 1
                            viewElement.inputText.setText(editElement.tagName())

                            val elementDialog = AlertDialog.Builder(context)
                                    .setTitle("Change element tag")
                                    .setView(viewElement)
                                    .setPositiveButton("CHANGE", null)
                                    .setNegativeButton("CANCEL", null)
                                    .create()

                            elementDialog.show()
                            elementDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                                if (!viewElement.inputText.text.toString().isEmpty()) {
                                    editElement.tagName(viewElement.inputText.text.toString())
                                    infoView.elementTag.text = viewElement.inputText.text.toString()
                                    val parent = node.parent
                                    tView.removeNode(node)
                                    tView.addNode(parent, TreeNode(TagTreeItem(value.element)))
                                    elementDialog.dismiss()
                                } else {
                                    viewElement.inputText.error = "Please enter a name for the element."
                                }
                            }
                        }

                        infoView.elementText.text = if (editElement.ownText().isEmpty()) "empty" else editElement.ownText()
                        infoView.textEdit.setOnClickListener {
                            val viewElement = View.inflate(context, R.layout.dialog_input_single, null)
                            viewElement.inputText.hint = "Value"
                            viewElement.inputText.setSingleLine(true)
                            viewElement.inputText.maxLines = 1
                            viewElement.inputText.setText(editElement.ownText())

                            AlertDialog.Builder(context)
                                    .setTitle("Change element text")
                                    .setView(viewElement)
                                    .setPositiveButton("CHANGE") { _, _ ->
                                        editElement.text(viewElement.inputText.text.toString())
                                        infoView.elementText.text = viewElement.inputText.text.toString()
                                    }
                                    .setNegativeButton("CANCEL", null)
                                    .show()
                        }

                        AlertDialog.Builder(context)
                                .setView(infoView)
                                .show()
                        return@OnMenuItemClickListener true
                    }
                    R.id.action_tag_remove -> {
                        tView.removeNode(node)
                        value.element.remove()
                        return@OnMenuItemClickListener true
                    }
                }

                true
            })

            menu.show()
        }

        return view
    }

    override fun toggle(active: Boolean) {
        view.elementArrow.animate().rotation((if (active) 0 else -90).toFloat()).duration = 150
    }

    class TagTreeItem(internal var element: Element)
}
