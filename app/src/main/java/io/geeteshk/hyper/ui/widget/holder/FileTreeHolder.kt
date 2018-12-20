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

package io.geeteshk.hyper.ui.widget.holder

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.snackbar.Snackbar
import com.unnamed.b.atv.model.TreeNode
import io.geeteshk.hyper.R
import io.geeteshk.hyper.util.editor.Clipboard
import io.geeteshk.hyper.util.editor.ResourceHelper
import io.geeteshk.hyper.util.snack
import kotlinx.android.synthetic.main.dialog_input_single.view.*
import kotlinx.android.synthetic.main.item_file_browser.view.*
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

class FileTreeHolder(context: Context) : TreeNode.BaseNodeViewHolder<FileTreeHolder.FileTreeItem>(context) {


    override fun createNodeView(node: TreeNode, value: FileTreeItem): View {
        val view = View.inflate(context, R.layout.item_file_browser, null)
        view.fileBrowserName.text = value.file.name
        ResourceHelper.setIcon(view.fileBrowserIcon, value.file, 0xFF448AFF.toInt())

        if (node.isLeaf) {
            view.fileBrowserArrow.visibility = View.GONE
        }

        if (!node.isExpanded) {
            view.fileBrowserArrow.rotation = -90F
        }

        view.fileBrowserOptions.setOnClickListener {
            val file = File(value.file.path)
            val menu = PopupMenu(context, view.fileBrowserOptions)
            menu.menuInflater.inflate(R.menu.menu_file_options, menu.menu)
            if (file.isFile) {
                menu.menu.findItem(R.id.action_new).isVisible = false
                menu.menu.findItem(R.id.action_paste).isVisible = false
                if (file.name == "index.html") {
                    menu.menu.findItem(R.id.action_rename).isVisible = false
                }
            } else {
                menu.menu.findItem(R.id.action_paste).isEnabled = Clipboard.instance.currentFile != null
            }

            menu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_new_file -> {
                        val newFileRootView = View.inflate(context, R.layout.dialog_input_single, null)
                        newFileRootView.inputText.setHint(R.string.file_name)

                        val newFileDialog = AlertDialog.Builder(context)
                                .setTitle("New file")
                                .setView(newFileRootView)
                                .setPositiveButton(R.string.create, null)
                                .setNegativeButton(R.string.cancel, null)
                                .create()

                        newFileDialog.show()
                        newFileDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            if (newFileRootView.inputText.text.toString().isEmpty()) {
                                newFileRootView.inputText.error = "Please enter a file name"
                            } else {
                                newFileDialog.dismiss()
                                val fileStr = newFileRootView.inputText.text.toString()
                                val newFile = File(file, fileStr)
                                try {
                                    FileUtils.writeStringToFile(newFile, "\n", Charset.defaultCharset())
                                } catch (e: IOException) {
                                    Timber.e(e)
                                    value.view.snack(e.toString(), Snackbar.LENGTH_SHORT)
                                }

                                value.view.snack("Created $fileStr.", Snackbar.LENGTH_SHORT)
                                val newFileNode = TreeNode(FileTreeItem(newFile, value.view))
                                node.addChild(newFileNode)
                                view.fileBrowserArrow.visibility = View.VISIBLE
                                tView.expandNode(node)
                            }
                        }

                        true
                    }

                    R.id.action_new_folder -> {
                        val newFolderRootView = View.inflate(context, R.layout.dialog_input_single, null)
                        newFolderRootView.inputText.setHint(R.string.folder_name)

                        val newFolderDialog = AlertDialog.Builder(context)
                                .setTitle("New folder")
                                .setView(newFolderRootView)
                                .setPositiveButton(R.string.create, null)
                                .setNegativeButton(R.string.cancel, null)
                                .create()

                        newFolderDialog.show()
                        newFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            if (newFolderRootView.inputText.text.toString().isEmpty()) {
                                newFolderRootView.inputText.error = "Please enter a folder name"
                            } else {
                                newFolderDialog.dismiss()
                                val folderStr = newFolderRootView.inputText.text.toString()
                                val newFolder = File(file, folderStr)
                                try {
                                    FileUtils.forceMkdir(newFolder)
                                } catch (e: IOException) {
                                    Timber.e(e)
                                    value.view.snack(e.toString(), Snackbar.LENGTH_SHORT)
                                }

                                value.view.snack("Created $folderStr.", Snackbar.LENGTH_SHORT)
                                val newFolderNode = TreeNode(FileTreeItem(newFolder, value.view))
                                node.addChild(newFolderNode)
                                view.fileBrowserArrow.visibility = View.VISIBLE
                                tView.expandNode(node)
                            }
                        }

                        true
                    }

                    R.id.action_rename -> {
                        val renameRootView = View.inflate(context, R.layout.dialog_input_single, null)
                        renameRootView.inputText.setHint(R.string.new_name)
                        renameRootView.inputText.setText(value.file.name)

                        val renameDialog = AlertDialog.Builder(context)
                                .setTitle("Rename " + value.file.name)
                                .setView(renameRootView)
                                .setPositiveButton("RENAME", null)
                                .setNegativeButton(R.string.cancel, null)
                                .create()

                        renameDialog.show()
                        renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            if (renameRootView.inputText.text.toString().isEmpty()) {
                                renameRootView.inputText.error = "Please enter a name"
                            } else {
                                renameDialog.dismiss()
                                val renameStr = renameRootView.inputText.text.toString()
                                val rename = File(file.path.replace(file.name, renameStr))
                                if (!file.isDirectory) {
                                    try {
                                        FileUtils.moveFile(file, rename)
                                    } catch (e: IOException) {
                                        Timber.e(e)
                                        value.view.snack(e.toString(), Snackbar.LENGTH_SHORT)
                                    }

                                } else {
                                    try {
                                        FileUtils.moveDirectory(file, rename)
                                    } catch (e: IOException) {
                                        Timber.e(e)
                                        value.view.snack(e.toString(), Snackbar.LENGTH_SHORT)
                                    }

                                }

                                value.view.snack("Renamed ${value.file.name} to $renameStr.", Snackbar.LENGTH_SHORT)
                                value.file = rename
                                view.fileBrowserName.text = value.file.name
                                ResourceHelper.setIcon(view.fileBrowserIcon, value.file, 0xFF448AFF.toInt())
                            }
                        }

                        true
                    }

                    R.id.action_copy -> {
                        Clipboard.instance.currentFile = file
                        Clipboard.instance.currentNode = node
                        Clipboard.instance.type = Clipboard.Type.COPY
                        value.view.snack("${value.file.name} selected to be copied.", Snackbar.LENGTH_SHORT)
                        true
                    }

                    R.id.action_cut -> {
                        Clipboard.instance.currentFile = file
                        Clipboard.instance.currentNode = node
                        Clipboard.instance.type = Clipboard.Type.CUT
                        value.view.snack("${value.file.name} selected to be moved.", Snackbar.LENGTH_SHORT)
                        true
                    }

                    R.id.action_paste -> {
                        val currentFile = Clipboard.instance.currentFile
                        val currentNode = Clipboard.instance.currentNode
                        val currentItem = currentNode!!.value as FileTreeItem
                        when (Clipboard.instance.type) {
                            Clipboard.Type.COPY -> {
                                if (currentFile!!.isDirectory) {
                                    try {
                                        FileUtils.copyDirectoryToDirectory(currentFile, file)
                                    } catch (e: Exception) {
                                        Timber.e(e)
                                        value.view.snack(e.toString(), Snackbar.LENGTH_SHORT)
                                    }

                                } else {
                                    try {
                                        FileUtils.copyFileToDirectory(currentFile, file)
                                    } catch (e: Exception) {
                                        Timber.e(e)
                                        value.view.snack(e.toString(), Snackbar.LENGTH_SHORT)
                                    }

                                }

                                value.view.snack("Successfully copied ${currentFile.name}.", Snackbar.LENGTH_SHORT)
                                val copyFile = File(file, currentFile.name)
                                val copyNode = TreeNode(FileTreeItem(copyFile, currentItem.view))
                                node.addChild(copyNode)
                                view.fileBrowserArrow!!.visibility = View.VISIBLE
                                tView.expandNode(node)
                            }

                            Clipboard.Type.CUT -> {
                                if (currentFile!!.isDirectory) {
                                    try {
                                        FileUtils.moveDirectoryToDirectory(currentFile, file, false)
                                    } catch (e: Exception) {
                                        Timber.e(e)
                                        value.view.snack(e.toString(), Snackbar.LENGTH_SHORT)
                                    }

                                } else {
                                    try {
                                        FileUtils.moveFileToDirectory(currentFile, file, false)
                                    } catch (e: Exception) {
                                        Timber.e(e)
                                        value.view.snack(e.toString(), Snackbar.LENGTH_SHORT)
                                    }

                                }

                                value.view.snack("Successfully moved ${currentFile.name}.", Snackbar.LENGTH_SHORT)
                                Clipboard.instance.currentFile = null
                                val cutFile = File(file, currentFile.name)
                                val cutNode = TreeNode(FileTreeItem(cutFile, currentItem.view))
                                node.addChild(cutNode)
                                view.fileBrowserArrow!!.visibility = View.VISIBLE
                                tView.expandNode(node)
                                tView.removeNode(Clipboard.instance.currentNode)
                            }
                        }

                        true
                    }
                    else -> false
                }
            }

            menu.show()
        }

        return view
    }

    override fun toggle(active: Boolean) {
        view.fileBrowserArrow.animate().rotation((if (active) 0 else -90).toFloat()).duration = 150
    }

    class FileTreeItem(var file: File, var view: View)
}
