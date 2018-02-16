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

package io.geeteshk.hyper.activity

import android.app.Activity
import android.app.ActivityManager
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Spinner
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.adapter.FileAdapter
import io.geeteshk.hyper.adapter.GitLogsAdapter
import io.geeteshk.hyper.fragment.EditorFragment
import io.geeteshk.hyper.fragment.ImageFragment
import io.geeteshk.hyper.git.GitWrapper
import io.geeteshk.hyper.helper.*
import io.geeteshk.hyper.helper.Prefs.defaultPrefs
import io.geeteshk.hyper.helper.Prefs.get
import io.geeteshk.hyper.widget.holder.FileTreeHolder
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.dialog_diff.view.*
import kotlinx.android.synthetic.main.dialog_git_branch.view.*
import kotlinx.android.synthetic.main.dialog_input_single.view.*
import kotlinx.android.synthetic.main.dialog_pull.view.*
import kotlinx.android.synthetic.main.dialog_push.view.*
import kotlinx.android.synthetic.main.item_git_status.view.*
import kotlinx.android.synthetic.main.sheet_about.view.*
import kotlinx.android.synthetic.main.sheet_logs.view.*
import kotlinx.android.synthetic.main.widget_toolbar.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

class ProjectActivity : AppCompatActivity() {

    private var openFiles: ArrayList<String>? = null

    private lateinit var fileSpinner: Spinner
    private lateinit var fileAdapter: ArrayAdapter<String>

    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var projectName: String
    private lateinit var projectDir: File
    private lateinit var indexFile: File
    private lateinit var rootNode: TreeNode
    private lateinit var treeView: AndroidTreeView
    private lateinit var props: Array<String?>
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        projectName = intent.getStringExtra("project")
        projectDir = File(Constants.HYPER_ROOT + File.separator + projectName)
        indexFile = ProjectManager.getIndexFile(projectName)!!

        setTheme(Styles.getThemeInt(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        prefs = defaultPrefs(this)
        if (intent.hasExtra("files")) {
            openFiles = intent.getStringArrayListExtra("files")
        } else {
            openFiles = ArrayList()
            openFiles!!.add(indexFile.path)
        }

        props = HTMLParser.getProperties(projectName)
        fileSpinner = Spinner(this)
        fileAdapter = FileAdapter(this, openFiles!!)
        fileSpinner.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        fileSpinner.adapter = fileAdapter
        toolbar.addView(fileSpinner)
        fileSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.editorFragment, getFragment(openFiles!![position]))
                        .commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.action_drawer_open, R.string.action_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerOpened(drawerView: View) {
                props = HTMLParser.getProperties(projectName)
                headerTitle.text = props[0]
                headerDesc.text = props[1]
            }

            override fun onDrawerClosed(drawerView: View) {

            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
        drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

        rootNode = TreeNode.root()
        setupFileTree(rootNode, projectDir)
        treeView = AndroidTreeView(this@ProjectActivity, rootNode)
        treeView.setDefaultAnimation(true)
        treeView.setDefaultViewHolder(FileTreeHolder::class.java)
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle)
        treeView.setDefaultNodeClickListener { node, value ->
            val item = value as FileTreeHolder.FileTreeItem
            if (node.isLeaf && item.file.isFile) {
                if (openFiles!!.contains(item.file.path)) {
                    setFragment(item.file.path, false)
                    drawerLayout.closeDrawers()
                } else {
                    if (!ProjectManager.isBinaryFile(item.file)) {
                        setFragment(item.file.path, true)
                        drawerLayout.closeDrawers()
                    } else if (ProjectManager.isImageFile(item.file)) {
                        setFragment(item.file.path, true)
                        drawerLayout.closeDrawers()
                    } else {
                        Snackbar.make(drawerLayout, R.string.not_text_file, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        treeView.setDefaultNodeLongClickListener { node, value ->
            val item = value as FileTreeHolder.FileTreeItem
            when (item.file.name) {
                "index.html" -> false
                else -> {
                    AlertDialog.Builder(this@ProjectActivity)
                            .setTitle(getString(R.string.delete) + " " + item.file.name + "?")
                            .setPositiveButton(R.string.delete) { _, _ ->
                                val delete = booleanArrayOf(true, false)
                                val file = item.file.name
                                val parent = node.parent
                                treeView.removeNode(node)
                                removeFragment(item.file.path)

                                val snackbar = Snackbar.make(
                                        drawerLayout,
                                        "Deleted $file.",
                                        Snackbar.LENGTH_LONG
                                )

                                snackbar.setAction("UNDO") {
                                    delete[0] = false
                                    snackbar.dismiss()
                                }

                                snackbar.addCallback(object : Snackbar.Callback() {
                                    override fun onDismissed(snackbar: Snackbar?, event: Int) {
                                        super.onDismissed(snackbar, event)
                                        if (!delete[1]) {
                                            if (delete[0]) {
                                                if (item.file.isDirectory) {
                                                    try {
                                                        FileUtils.deleteDirectory(item.file)
                                                    } catch (e: IOException) {
                                                        Log.e(TAG, e.toString())
                                                    }

                                                } else {
                                                    if (!item.file.delete()) {
                                                        Log.e(TAG, "Failed to delete " + item.file.path)
                                                    }
                                                }
                                            } else {
                                                treeView.addNode(parent, node)
                                            }

                                            delete[1] = true
                                        }
                                    }
                                })

                                snackbar.show()
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()

                    true
                }
            }
        }

        fileBrowser.addView(treeView.view)
        headerBackground.setBackgroundResource(MATERIAL_BACKGROUNDS[(Math.random() * 8).toInt()])
        headerIcon.setImageBitmap(ProjectManager.getFavicon(this@ProjectActivity, projectName))
        headerTitle.text = props[0]
        headerDesc.text = props[1]

        rootOverflow.setOnClickListener {
            val menu = PopupMenu(this@ProjectActivity, rootOverflow)
            menu.menuInflater.inflate(R.menu.menu_file_options, menu.menu)
            menu.menu.findItem(R.id.action_copy).isVisible = false
            menu.menu.findItem(R.id.action_cut).isVisible = false
            menu.menu.findItem(R.id.action_rename).isVisible = false
            menu.menu.findItem(R.id.action_paste).isEnabled = Clipboard.instance.currentFile != null
            menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_new_file -> {
                        val newFileRootView = View.inflate(this@ProjectActivity, R.layout.dialog_input_single, null)
                        newFileRootView.inputText.setHint(R.string.file_name)
                        val newFileDialog = AlertDialog.Builder(this@ProjectActivity)
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
                                val newFile = File(projectDir, fileStr)
                                try {
                                    FileUtils.writeStringToFile(newFile, "\n", Charset.defaultCharset())
                                } catch (e: IOException) {
                                    Log.e(TAG, e.toString())
                                    Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show()
                                }

                                Snackbar.make(drawerLayout, "Created $fileStr.", Snackbar.LENGTH_SHORT).show()
                                val newFileNode = TreeNode(FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(newFile), newFile, drawerLayout))
                                rootNode.addChild(newFileNode)
                                treeView.setRoot(rootNode)
                                treeView.addNode(rootNode, newFileNode)
                            }
                        }

                        return@OnMenuItemClickListener true
                    }
                    R.id.action_new_folder -> {
                        val newFolderRootView = View.inflate(this@ProjectActivity, R.layout.dialog_input_single, null)
                        newFolderRootView.inputText.setHint(R.string.folder_name)

                        val newFolderDialog = AlertDialog.Builder(this@ProjectActivity)
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
                                val newFolder = File(projectDir, folderStr)
                                try {
                                    FileUtils.forceMkdir(newFolder)
                                } catch (e: IOException) {
                                    Log.e(TAG, e.toString())
                                    Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show()
                                }

                                Snackbar.make(drawerLayout, "Created $folderStr.", Snackbar.LENGTH_SHORT).show()
                                val newFolderNode = TreeNode(FileTreeHolder.FileTreeItem(R.drawable.ic_folder, newFolder, drawerLayout))
                                rootNode.addChild(newFolderNode)
                                treeView.setRoot(rootNode)
                                treeView.addNode(rootNode, newFolderNode)
                            }
                        }

                        return@OnMenuItemClickListener true
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
                                        Log.e(TAG, e.toString())
                                        Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show()
                                    }

                                } else {
                                    try {
                                        FileUtils.copyFileToDirectory(currentFile, projectDir)
                                    } catch (e: Exception) {
                                        Log.e(TAG, e.toString())
                                        Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show()
                                    }

                                }

                                Snackbar.make(drawerLayout, "Successfully copied " + currentFile.name + ".", Snackbar.LENGTH_SHORT).show()
                                val copyFile = File(projectDir, currentFile.name)
                                val copyNode = TreeNode(FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(copyFile), copyFile, currentItem.view))
                                rootNode.addChild(copyNode)
                                treeView.setRoot(rootNode)
                                treeView.addNode(rootNode, copyNode)
                            }
                            Clipboard.Type.CUT -> {
                                if (currentFile!!.isDirectory) {
                                    try {
                                        FileUtils.moveDirectoryToDirectory(currentFile, projectDir, false)
                                    } catch (e: Exception) {
                                        Log.e(TAG, e.toString())
                                        Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show()
                                    }

                                } else {
                                    try {
                                        FileUtils.moveFileToDirectory(currentFile, projectDir, false)
                                    } catch (e: Exception) {
                                        Log.e(TAG, e.toString())
                                        Snackbar.make(drawerLayout, e.toString(), Snackbar.LENGTH_SHORT).show()
                                    }

                                }

                                Snackbar.make(drawerLayout, "Successfully moved " + currentFile.name + ".", Snackbar.LENGTH_SHORT).show()
                                Clipboard.instance.currentFile = null
                                val cutFile = File(projectDir, currentFile.name)
                                val cutNode = TreeNode(FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(cutFile), cutFile, currentItem.view))
                                rootNode.addChild(cutNode)
                                treeView.setRoot(rootNode)
                                treeView.addNode(rootNode, cutNode)
                                treeView.removeNode(Clipboard.instance.currentNode)
                            }
                        }
                        return@OnMenuItemClickListener true
                    }
                }

                false
            })

            menu.show()
        }

        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = 0x00000000
            val description = ActivityManager.TaskDescription(projectName, ProjectManager.getFavicon(this@ProjectActivity, projectName))
            this.setTaskDescription(description)
        }
    }

    private fun setupFileTree(root: TreeNode?, f: File?) {
        val files = f!!.listFiles { _, name -> !name.startsWith(".") }

        for (file in files) {
            if (file.isDirectory) {
                val folderNode = TreeNode(FileTreeHolder.FileTreeItem(R.drawable.ic_folder, file, drawerLayout))
                setupFileTree(folderNode, file)
                root!!.addChild(folderNode)
            } else {
                val fileNode = TreeNode(FileTreeHolder.FileTreeItem(ResourceHelper.getIcon(file), file, drawerLayout))
                root!!.addChild(fileNode)
            }
        }
    }

    private fun removeFragment(file: String) {
        openFiles!!.remove(file)
        fileAdapter.remove(file)
        fileAdapter.notifyDataSetChanged()
    }

    private fun setFragment(file: String, add: Boolean) {
        if (add) {
            fileAdapter.add(file)
            fileAdapter.notifyDataSetChanged()
        }

        fileSpinner.setSelection(fileAdapter.getPosition(file), true)
        supportFragmentManager.beginTransaction()
                .replace(R.id.editorFragment, getFragment(file))
                .commit()
    }

    fun getFragment(title: String): Fragment {
        val bundle = Bundle()
        bundle.putInt("position", fileAdapter.count)
        bundle.putString("location", title)
        return if (ProjectManager.isImageFile(File(title))) {
            Fragment.instantiate(this, ImageFragment::class.java.name, bundle)
        } else {
            Fragment.instantiate(this, EditorFragment::class.java.name, bundle)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isGitRepo = File(projectDir, ".git").exists() && File(projectDir, ".git").isDirectory
        var canCommit = false
        var canCheckout = false
        var hasRemotes = false
        val isHtml = (fileSpinner.selectedItem as String).endsWith(".html")
        if (isGitRepo) {
            canCommit = GitWrapper.canCommit(drawerLayout, projectDir)
            canCheckout = GitWrapper.canCheckout(drawerLayout, projectDir)
            hasRemotes = GitWrapper.getRemotes(drawerLayout, projectDir) != null && GitWrapper.getRemotes(drawerLayout, projectDir)!!.size > 0
        }

        menu.findItem(R.id.action_view).isEnabled = isHtml
        menu.findItem(R.id.action_git_add).isEnabled = isGitRepo
        menu.findItem(R.id.action_git_commit).isEnabled = canCommit
        menu.findItem(R.id.action_git_push).isEnabled = hasRemotes
        menu.findItem(R.id.action_git_pull).isEnabled = hasRemotes
        menu.findItem(R.id.action_git_log).isEnabled = isGitRepo
        menu.findItem(R.id.action_git_diff).isEnabled = isGitRepo
        menu.findItem(R.id.action_git_status).isEnabled = isGitRepo
        menu.findItem(R.id.action_git_branch).isEnabled = isGitRepo
        menu.findItem(R.id.action_git_remote).isEnabled = isGitRepo
        menu.findItem(R.id.action_git_branch_checkout).isEnabled = canCheckout

        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_project, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_run -> {
                val runIntent = Intent(this@ProjectActivity, WebActivity::class.java)
                runIntent.putExtra("url", "file:///" + indexFile.path)
                runIntent.putExtra("name", projectName)
                startActivity(runIntent)
                return true
            }
            R.id.action_view -> {
                val viewIntent = Intent(this@ProjectActivity, ViewActivity::class.java)
                viewIntent.putExtra("html_path", openFiles!![fileSpinner.selectedItemPosition])
                startActivityForResult(viewIntent, VIEW_CODE)
                return true
            }
            R.id.action_import_file -> {
                val fontIntent = Intent(Intent.ACTION_GET_CONTENT)
                fontIntent.type = "file/*"
                fontIntent.resolveActivity(packageManager)?.let {
                    startActivityForResult(fontIntent, IMPORT_FILE)
                }
                return true
            }
            R.id.action_about -> {
                showAbout()
                return true
            }
            R.id.action_git_init -> {
                GitWrapper.init(this@ProjectActivity, projectDir, drawerLayout)
                return true
            }
            R.id.action_git_add -> {
                GitWrapper.add(drawerLayout, projectDir)
                return true
            }
            R.id.action_git_commit -> {
                val view = View.inflate(this@ProjectActivity, R.layout.dialog_input_single, null)
                view.inputText.setHint(R.string.commit_message)

                val commitDialog = AlertDialog.Builder(this@ProjectActivity)
                        .setTitle(R.string.git_commit)
                        .setView(view)
                        .setCancelable(false)
                        .setPositiveButton(R.string.git_commit, null)
                        .setNegativeButton(R.string.cancel) { dialogInterface, _ -> dialogInterface.cancel() }
                        .create()

                commitDialog.show()
                commitDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    if (!view.inputText.text.toString().isEmpty()) {
                        GitWrapper.commit(this@ProjectActivity, drawerLayout, projectDir, view.inputText.text.toString())
                        commitDialog.dismiss()
                    } else {
                        view.inputText.error = getString(R.string.commit_message_empty)
                    }
                }
                return true
            }
            R.id.action_git_push -> {
                val pushView = View.inflate(this@ProjectActivity, R.layout.dialog_push, null)
                pushView.pushSpinner.adapter = ArrayAdapter(this@ProjectActivity, android.R.layout.simple_list_item_1, GitWrapper.getRemotes(drawerLayout, projectDir)!!)
                AlertDialog.Builder(this@ProjectActivity)
                        .setTitle("Push changes")
                        .setView(pushView)
                        .setPositiveButton("PUSH") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                            GitWrapper.push(this@ProjectActivity, drawerLayout, projectDir, pushView.pushSpinner.selectedItem as String, booleanArrayOf(pushView.dryRun.isChecked, pushView.force.isChecked, pushView.thin.isChecked, pushView.tags.isChecked), pushView.pushUsername.text.toString(), pushView.pushPassword.text.toString())
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()

                return true
            }
            R.id.action_git_pull -> {
                val pullView = View.inflate(this@ProjectActivity, R.layout.dialog_pull, null)
                pullView.remotesSpinner.adapter = ArrayAdapter(this@ProjectActivity, android.R.layout.simple_list_item_1, GitWrapper.getRemotes(drawerLayout, projectDir)!!)
                AlertDialog.Builder(this@ProjectActivity)
                        .setTitle("Push changes")
                        .setView(pullView)
                        .setPositiveButton("PULL") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                            GitWrapper.pull(this@ProjectActivity, drawerLayout, projectDir, pullView.remotesSpinner.selectedItem as String, pullView.pullUsername.text.toString(), pullView.pullPassword.text.toString())
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()

                return true
            }
            R.id.action_git_log -> {
                val commits = GitWrapper.getCommits(drawerLayout, projectDir)
                val layoutLog = View.inflate(this, R.layout.sheet_logs, null)
                if (prefs["dark_theme", false]!!) {
                    layoutLog.setBackgroundColor(-0xcccccd)
                }

                val manager = LinearLayoutManager(this)
                val adapter = GitLogsAdapter(this@ProjectActivity, commits)

                layoutLog.logsList.layoutManager = manager
                layoutLog.logsList.adapter = adapter

                val dialogLog = BottomSheetDialog(this)
                dialogLog.setContentView(layoutLog)
                dialogLog.show()
                return true
            }
            R.id.action_git_diff -> {
                val chosen = intArrayOf(-1, -1)
                val commitsToDiff = GitWrapper.getCommits(drawerLayout, projectDir)
                val commitNames = arrayOfNulls<CharSequence>(commitsToDiff!!.size)
                for (i in commitNames.indices) {
                    commitNames[i] = commitsToDiff[i].shortMessage
                }

                AlertDialog.Builder(this@ProjectActivity)
                        .setTitle("Choose first commit")
                        .setSingleChoiceItems(commitNames, -1) { dialogInterface, i ->
                            dialogInterface.cancel()
                            chosen[0] = i
                            AlertDialog.Builder(this@ProjectActivity)
                                    .setTitle("Choose second commit")
                                    .setSingleChoiceItems(commitNames, -1) { dialogIface, i2 ->
                                        dialogIface.cancel()
                                        chosen[1] = i2
                                        val string = GitWrapper.diff(drawerLayout, projectDir, commitsToDiff[chosen[0]].id, commitsToDiff[chosen[1]].id)
                                        val rootView = View.inflate(this@ProjectActivity, R.layout.dialog_diff, null)
                                        rootView.diffView.setDiffText(string!!)

                                        AlertDialog.Builder(this@ProjectActivity)
                                                .setView(rootView)
                                                .show()
                                    }
                                    .show()
                        }
                        .show()

                return true
            }
            R.id.action_git_status -> {
                val status = View.inflate(this, R.layout.item_git_status, null)
                if (prefs["dark_theme", false]!!) {
                    status.setBackgroundColor(-0xcccccd)
                }

                GitWrapper.status(drawerLayout, projectDir, status.conflict, status.added, status.changed, status.missing, status.modified, status.removed, status.uncommitted, status.untracked, status.untrackedFolders)
                val dialogStatus = BottomSheetDialog(this)
                dialogStatus.setContentView(status)
                dialogStatus.show()
                return true
            }
            R.id.action_git_branch_new -> {
                val branchView = View.inflate(this@ProjectActivity, R.layout.dialog_git_branch, null)
                branchView.checkout.setText(R.string.checkout)

                val branchDialog = AlertDialog.Builder(this@ProjectActivity)
                        .setTitle("New branch")
                        .setView(branchView)
                        .setPositiveButton(R.string.create, null)
                        .setNegativeButton(R.string.cancel, null)
                        .create()

                branchDialog.show()
                branchDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    if (!branchView.branchName.text.toString().isEmpty()) {
                        GitWrapper.createBranch(this@ProjectActivity, drawerLayout, projectDir, branchView.branchName.text.toString(), branchView.checkout.isChecked)
                        branchDialog.dismiss()
                    } else {
                        branchView.branchName.error = getString(R.string.branch_name_empty)
                    }
                }
                return true
            }
            R.id.action_git_branch_remove -> {
                val branchesList = GitWrapper.getBranches(drawerLayout, projectDir)
                val itemsMultiple = arrayOfNulls<CharSequence>(branchesList!!.size)
                for (i in itemsMultiple.indices) {
                    itemsMultiple[i] = branchesList[i].name
                }

                val checkedItems = BooleanArray(itemsMultiple.size)
                val toDelete = ArrayList<String>()

                AlertDialog.Builder(this)
                        .setMultiChoiceItems(itemsMultiple, checkedItems) { _, i, b ->
                            if (b) {
                                toDelete.add(itemsMultiple[i].toString())
                            } else {
                                toDelete.remove(itemsMultiple[i].toString())
                            }
                        }
                        .setPositiveButton(R.string.delete) { dialogInterface, _ ->
                            GitWrapper.deleteBranch(drawerLayout, projectDir, *toDelete.toTypedArray())
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton(R.string.close, null)
                        .setTitle("Delete branches")
                        .show()

                return true
            }
            R.id.action_git_branch_checkout -> {
                val branches = GitWrapper.getBranches(drawerLayout, projectDir)
                var checkedItem = -1
                val items = arrayOfNulls<CharSequence>(branches!!.size)
                for (i in items.indices) {
                    items[i] = branches[i].name
                }

                for (i in items.indices) {
                    val branch = GitWrapper.getCurrentBranch(drawerLayout, projectDir)
                    branch?.let {
                        if (branch == items[i]) {
                            checkedItem = i
                        }
                    }
                }

                AlertDialog.Builder(this)
                        .setSingleChoiceItems(items, checkedItem) { dialogInterface, i ->
                            dialogInterface.dismiss()
                            GitWrapper.checkout(this@ProjectActivity, drawerLayout, projectDir, branches[i].name)
                        }
                        .setNegativeButton(R.string.close, null)
                        .setTitle("Checkout branch")
                        .show()

                return true
            }
            R.id.action_git_remote -> {
                val remoteIntent = Intent(this@ProjectActivity, RemotesActivity::class.java)
                remoteIntent.putExtra("project_file", projectDir.path)
                startActivity(remoteIntent)
                return true
            }
            R.id.action_analyze -> {
                val analyzeIntent = Intent(this@ProjectActivity, AnalyzeActivity::class.java)
                analyzeIntent.putExtra("project_file", projectDir.path)
                startActivity(analyzeIntent)
                return true
            }
        }

        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IMPORT_FILE -> if (resultCode == Activity.RESULT_OK) {
                val fileUri = data!!.data
                val view = View.inflate(this@ProjectActivity, R.layout.dialog_input_single, null)
                view.inputText.setHint(R.string.file_name)

                val dialog = AlertDialog.Builder(this)
                        .setTitle(R.string.name)
                        .setView(view)
                        .setCancelable(false)
                        .setPositiveButton(R.string.import_not_java, null)
                        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
                        .create()

                dialog.show()
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    if (view.inputText.text.toString().isEmpty()) {
                        view.inputText.error = "Please enter a name"
                    } else {
                        dialog.dismiss()
                        if (ProjectManager.importFile(this@ProjectActivity, projectName, fileUri!!, view.inputText.text.toString())) {
                            Snackbar.make(drawerLayout, R.string.file_success, Snackbar.LENGTH_SHORT).show()
                        } else {
                            Snackbar.make(drawerLayout, R.string.file_fail, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
            VIEW_CODE -> if (resultCode == Activity.RESULT_OK) {
                val intent = Intent(this@ProjectActivity, ProjectActivity::class.java)
                intent.putExtras(getIntent().extras)
                intent.addFlags(getIntent().flags)
                intent.putStringArrayListExtra("files", openFiles)
                startActivity(intent)
                finish()
            }
        }

        setupFileTree(rootNode, projectDir)
        treeView.setRoot(rootNode)
    }

    private fun showAbout() {
        props = HTMLParser.getProperties(projectName)
        val layout = View.inflate(this@ProjectActivity, R.layout.sheet_about, null)

        layout.projName.text = props[0]
        layout.projAuthor.text = props[1]
        layout.projDesc.text = props[2]
        layout.projKey.text = props[3]

        if (prefs["dark_theme", false]!!) {
            layout.setBackgroundColor(-0xcccccd)
        }

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(layout)
        dialog.show()
    }

    companion object {

        private val TAG = ProjectActivity::class.java.simpleName
        private val VIEW_CODE = 99
        private val IMPORT_FILE = 101
        private val MATERIAL_BACKGROUNDS = intArrayOf(R.drawable.material_bg_1, R.drawable.material_bg_2, R.drawable.material_bg_3, R.drawable.material_bg_4, R.drawable.material_bg_5, R.drawable.material_bg_6, R.drawable.material_bg_7, R.drawable.material_bg_8)
    }
}
