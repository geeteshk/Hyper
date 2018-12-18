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
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.adapter.ProjectAdapter
import io.geeteshk.hyper.git.GitWrapper
import io.geeteshk.hyper.util.*
import io.geeteshk.hyper.util.Prefs.defaultPrefs
import io.geeteshk.hyper.util.Prefs.get
import io.geeteshk.hyper.util.Prefs.set
import io.geeteshk.hyper.util.editor.ResourceHelper
import io.geeteshk.hyper.util.project.DataValidator
import io.geeteshk.hyper.util.project.ProjectManager
import io.geeteshk.hyper.util.ui.Styles
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_clone.view.*
import kotlinx.android.synthetic.main.dialog_create.view.*
import kotlinx.android.synthetic.main.dialog_import.view.*
import kotlinx.android.synthetic.main.widget_toolbar.*
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private var contents: Array<String>? = null
    private var contentsList: ArrayList<String>? = null
    private lateinit var projectAdapter: ProjectAdapter

    private var imageStream: InputStream? = null
    private lateinit var projectIcon: ImageView
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Styles.getThemeInt(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        prefs = defaultPrefs(this)
        contents = File(Constants.HYPER_ROOT).list { dir, name -> dir.isDirectory && name != ".git" && ProjectManager.isValid(name) }
        contentsList = if (contents != null) {
            ArrayList(Arrays.asList(*contents!!))
        } else {
            ArrayList()
        }

        DataValidator.removeBroken(contentsList!!)
        projectAdapter = ProjectAdapter(this, contentsList!!, coordinatorLayout, projectList)
        val layoutManager = LinearLayoutManager(this)
        projectList.layoutManager = layoutManager
        projectList.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        projectList.itemAnimator = DefaultItemAnimator()
        projectList.adapter = projectAdapter
        cloneButton.setOnClickListener {
            val choices = arrayOf("Create a new project", "Clone a repository", "Import an external project")
            AlertDialog.Builder(this@MainActivity)
                    .setTitle("Would you like to...")
                    .setAdapter(ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, choices)) { _, i ->
                        when (i) {
                            0 -> {
                                val rootView = View.inflate(this@MainActivity, R.layout.dialog_create, null)
                                rootView.typeSpinner.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, ProjectManager.TYPES)
                                rootView.typeSpinner.setSelection(prefs["type", 0]!!)
                                rootView.nameLayout.editText!!.setText(prefs["name", ""])
                                rootView.authorLayout.editText!!.setText(prefs["author", ""])
                                rootView.descLayout.editText!!.setText(prefs["description", ""])
                                rootView.keyLayout.editText!!.setText(prefs["keywords", ""])

                                projectIcon = rootView.faviconImage
                                rootView.defaultIcon.isChecked = true
                                rootView.defaultIcon.setOnCheckedChangeListener { _, isChecked ->
                                    if (isChecked) {
                                        projectIcon.setImageResource(R.drawable.ic_launcher)
                                        imageStream = null
                                    }
                                }

                                rootView.chooseIcon.setOnCheckedChangeListener { _, isChecked ->
                                    if (isChecked) {
                                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                                        intent.type = "image/*"
                                        startActivityForResult(intent, SELECT_ICON)
                                    }
                                }

                                val createDialog = AlertDialog.Builder(this@MainActivity)
                                        .setTitle("Create a new project")
                                        .setView(rootView)
                                        .setPositiveButton("CREATE", null)
                                        .setNegativeButton("CANCEL", null)
                                        .create()

                                createDialog.show()
                                createDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                                    if (DataValidator.validateCreate(this@MainActivity, rootView.nameLayout, rootView.authorLayout, rootView.descLayout, rootView.keyLayout)) {
                                        val name = rootView.nameLayout.editText!!.text.toString()
                                        val author = rootView.authorLayout.editText!!.text.toString()
                                        val description = rootView.descLayout.editText!!.text.toString()
                                        val keywords = rootView.keyLayout.editText!!.text.toString()
                                        val type = rootView.typeSpinner.selectedItemPosition

                                        prefs["name"] = name
                                        prefs["author"] = author
                                        prefs["description"] = description
                                        prefs["keywords"] = keywords
                                        prefs["type"] = type

                                        ProjectManager.generate(
                                                this@MainActivity,
                                                name,
                                                author,
                                                description,
                                                keywords,
                                                imageStream,
                                                projectAdapter,
                                                coordinatorLayout,
                                                type
                                        )

                                        createDialog.dismiss()
                                    }
                                }
                            }
                            1 -> {
                                val cloneView = View.inflate(this@MainActivity, R.layout.dialog_clone, null)
                                cloneView.cloneName.setText(prefs["clone_name", ""])
                                cloneView.cloneUrl.setText(prefs["remote", ""])
                                val cloneDialog = AlertDialog.Builder(this@MainActivity)
                                        .setTitle("Clone a repository")
                                        .setView(cloneView)
                                        .setPositiveButton("CLONE", null)
                                        .setNegativeButton(R.string.cancel, null)
                                        .create()

                                cloneDialog.show()
                                cloneDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                                    if (DataValidator.validateClone(this@MainActivity, cloneView.cloneName, cloneView.cloneUrl)) {
                                        var remoteStr = cloneView.cloneUrl.text.toString()
                                        if (!remoteStr.contains("://")) {
                                            remoteStr = "https://$remoteStr"
                                        }

                                        val cloneName = cloneView.cloneName.text.toString()
                                        prefs["clone_name"] = cloneName
                                        prefs["remote"] = remoteStr

                                        GitWrapper.clone(
                                                this@MainActivity,
                                                coordinatorLayout,
                                                File(Constants.HYPER_ROOT + File.separator + cloneName),
                                                projectAdapter,
                                                remoteStr,
                                                cloneView.cloneUsername.text.toString(),
                                                cloneView.clonePassword.text.toString()
                                        )

                                        cloneDialog.dismiss()
                                    }
                                }
                            }
                            2 -> {
                                val intent = Intent(Intent.ACTION_GET_CONTENT)
                                intent.type = "file/*"
                                intent.resolveActivity(packageManager)?.let {
                                    startActivityForResult(intent, IMPORT_PROJECT)
                                }
                            }
                        }
                    }
                    .show()
        }

        projectList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    cloneButton.show()
                }

                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && cloneButton.isShown) cloneButton.hide()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.setOnQueryTextListener(this)
        searchView.setOnCloseListener(this)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(settingsIntent, SETTINGS_CODE)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SELECT_ICON -> if (resultCode == Activity.RESULT_OK) {
                try {
                    val selectedImage = data!!.data
                    selectedImage?.let {
                        imageStream = this@MainActivity.contentResolver.openInputStream(selectedImage)
                        projectIcon.setImageBitmap(ResourceHelper.decodeUri(this@MainActivity, selectedImage))
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }

            }
            SETTINGS_CODE -> {
                val intent = Intent(this@MainActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            IMPORT_PROJECT -> if (resultCode == Activity.RESULT_OK) {
                val fileUri = data!!.data
                val file = File(fileUri.path)
                val rootView = View.inflate(this@MainActivity, R.layout.dialog_import, null)
                rootView.impTypeSpinner.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, ProjectManager.TYPES)
                rootView.impTypeSpinner.setSelection(prefs["type", 0]!!)

                rootView.impNameLayout.editText!!.setText(file.parentFile.name)
                rootView.impAuthorLayout.editText!!.setText(prefs["author", ""])
                rootView.impDescLayout.editText!!.setText(prefs["description", ""])
                rootView.impKeyLayout.editText!!.setText(prefs["keywords", ""])

                val createDialog = AlertDialog.Builder(this@MainActivity)
                        .setTitle("Import an external project")
                        .setIcon(R.drawable.ic_action_import)
                        .setView(rootView)
                        .setPositiveButton("IMPORT", null)
                        .setNegativeButton("CANCEL", null)
                        .create()

                createDialog.show()
                createDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    if (DataValidator.validateCreate(this@MainActivity, rootView.impNameLayout, rootView.impAuthorLayout, rootView.impDescLayout, rootView.impKeyLayout)) {
                        val name = rootView.impNameLayout.editText!!.text.toString()
                        val author = rootView.impAuthorLayout.editText!!.text.toString()
                        val description = rootView.impDescLayout.editText!!.text.toString()
                        val keywords = rootView.impKeyLayout.editText!!.text.toString()
                        val type = rootView.impTypeSpinner.selectedItemPosition

                        prefs["name"] = name
                        prefs["author"] = author
                        prefs["description"] = description
                        prefs["keywords"] = keywords
                        prefs["type"] = type

                        ProjectManager.importProject(
                                file.parentFile.path,
                                name,
                                author,
                                description,
                                keywords,
                                type,
                                projectAdapter,
                                coordinatorLayout
                        )

                        createDialog.dismiss()
                    }
                }
            }
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean = false

    override fun onQueryTextChange(newText: String): Boolean {
        contentsList = ArrayList(Arrays.asList(*contents!!))
        DataValidator.removeBroken(contentsList!!)
        val iterator = contentsList!!.iterator()
        while (iterator.hasNext()) {
            if (!iterator.next().toLowerCase(Locale.getDefault()).contains(newText)) {
                iterator.remove()
            }
        }

        projectAdapter = ProjectAdapter(this@MainActivity, contentsList!!, coordinatorLayout, projectList)
        projectList.adapter = projectAdapter
        return true
    }

    override fun onClose(): Boolean {
        contentsList = ArrayList(Arrays.asList(*contents!!))
        DataValidator.removeBroken(contentsList!!)
        projectAdapter = ProjectAdapter(this@MainActivity, contentsList!!, coordinatorLayout, projectList)
        projectList.adapter = projectAdapter
        return false
    }

    companion object {

        val SELECT_ICON = 100
        val SETTINGS_CODE = 101
        private val IMPORT_PROJECT = 102
    }
}
