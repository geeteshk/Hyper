package io.geeteshk.hyper.ui.widget

import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.snackbar.Snackbar
import com.unnamed.b.atv.model.TreeNode
import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.widget.holder.FileTreeHolder
import io.geeteshk.hyper.util.editor.Clipboard
import io.geeteshk.hyper.util.snack
import io.geeteshk.hyper.util.string
import kotlinx.android.synthetic.main.dialog_input_single.view.*
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

class RootOverflowPopupMenu(private var context: Context, view: View,
                            private var drawerLayout: DrawerLayout, private var rootNode: TreeNode,
                            private var treeView: ProjectTreeView, private var projectDir: File)
    : PopupMenu(context, view), PopupMenu.OnMenuItemClickListener {

    init {
        menuInflater.inflate(R.menu.menu_file_options, menu)
        menu.findItem(R.id.action_copy).isVisible = false
        menu.findItem(R.id.action_cut).isVisible = false
        menu.findItem(R.id.action_rename).isVisible = false
        menu.findItem(R.id.action_paste).isEnabled = Clipboard.instance.currentFile != null

        setOnMenuItemClickListener(this)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
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
                    if (newFileRootView.inputText.string().isEmpty()) {
                        newFileRootView.inputText.error = "Please enter a file name"
                    } else {
                        newFileDialog.dismiss()
                        val fileStr = newFileRootView.inputText.string()
                        val newFile = File(projectDir, fileStr)
                        try {
                            FileUtils.writeStringToFile(newFile, "\n", Charset.defaultCharset())
                        } catch (e: IOException) {
                            Timber.e(e)
                            drawerLayout.snack(e.toString(), Snackbar.LENGTH_SHORT)
                        }

                        drawerLayout.snack("Created $fileStr.", Snackbar.LENGTH_SHORT)
                        val newFileNode = TreeNode(FileTreeHolder.FileTreeItem(newFile, drawerLayout))
                        rootNode.addChild(newFileNode)
                        treeView.setRoot(rootNode)
                        treeView.addNode(rootNode, newFileNode)
                    }
                }
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
                    if (newFolderRootView.inputText.string().isEmpty()) {
                        newFolderRootView.inputText.error = "Please enter a folder name"
                    } else {
                        newFolderDialog.dismiss()
                        val folderStr = newFolderRootView.inputText.string()
                        val newFolder = File(projectDir, folderStr)
                        try {
                            FileUtils.forceMkdir(newFolder)
                        } catch (e: IOException) {
                            Timber.e(e)
                            drawerLayout.snack(e.toString(), Snackbar.LENGTH_SHORT)
                        }

                        drawerLayout.snack("Created $folderStr.", Snackbar.LENGTH_SHORT)
                        val newFolderNode = TreeNode(FileTreeHolder.FileTreeItem(newFolder, drawerLayout))
                        rootNode.addChild(newFolderNode)
                        treeView.setRoot(rootNode)
                        treeView.addNode(rootNode, newFolderNode)
                    }
                }
            }

            R.id.action_paste -> {
                val currentFile = Clipboard.instance.currentFile
                val currentNode = Clipboard.instance.currentNode
                val currentItem = currentNode?.value as FileTreeHolder.FileTreeItem
                when (Clipboard.instance.type) {
                    Clipboard.Type.COPY -> {
                        if (currentFile!!.isDirectory) {
                            try {
                                FileUtils.copyDirectoryToDirectory(currentFile, projectDir)
                            } catch (e: Exception) {
                                Timber.e(e)
                                drawerLayout.snack(e.toString(), Snackbar.LENGTH_SHORT)
                            }

                        } else {
                            try {
                                FileUtils.copyFileToDirectory(currentFile, projectDir)
                            } catch (e: Exception) {
                                Timber.e(e)
                                drawerLayout.snack(e.toString(), Snackbar.LENGTH_SHORT)
                            }

                        }

                        drawerLayout.snack("Successfully copied ${currentFile.name}.", Snackbar.LENGTH_SHORT)
                        val copyFile = File(projectDir, currentFile.name)
                        val copyNode = TreeNode(FileTreeHolder.FileTreeItem(copyFile, currentItem.view))
                        rootNode.addChild(copyNode)
                        treeView.setRoot(rootNode)
                        treeView.addNode(rootNode, copyNode)
                    }
                    Clipboard.Type.CUT -> {
                        if (currentFile!!.isDirectory) {
                            try {
                                FileUtils.moveDirectoryToDirectory(currentFile, projectDir, false)
                            } catch (e: Exception) {
                                Timber.e(e)
                                drawerLayout.snack(e.toString(), Snackbar.LENGTH_SHORT)
                            }

                        } else {
                            try {
                                FileUtils.moveFileToDirectory(currentFile, projectDir, false)
                            } catch (e: Exception) {
                                Timber.e(e)
                                drawerLayout.snack(e.toString(), Snackbar.LENGTH_SHORT)
                            }

                        }

                        drawerLayout.snack("Successfully moved {currentFile.name}.", Snackbar.LENGTH_SHORT)
                        Clipboard.instance.currentFile = null
                        val cutFile = File(projectDir, currentFile.name)
                        val cutNode = TreeNode(FileTreeHolder.FileTreeItem(cutFile, currentItem.view))
                        rootNode.addChild(cutNode)
                        treeView.setRoot(rootNode)
                        treeView.addNode(rootNode, cutNode)
                        treeView.removeNode(Clipboard.instance.currentNode)
                    }
                }
            }

            else -> return false
        }

        return true
    }
}