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

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.adapter.LogsAdapter
import io.geeteshk.hyper.util.Constants
import io.geeteshk.hyper.util.Prefs.defaultPrefs
import io.geeteshk.hyper.util.Prefs.get
import io.geeteshk.hyper.util.net.HyperServer
import io.geeteshk.hyper.util.net.NetworkUtils
import io.geeteshk.hyper.util.project.ProjectManager
import kotlinx.android.synthetic.main.activity_web.*
import kotlinx.android.synthetic.main.dialog_input_single.view.*
import kotlinx.android.synthetic.main.sheet_logs.view.*
import kotlinx.android.synthetic.main.sheet_web_settings.view.*
import kotlinx.android.synthetic.main.widget_toolbar.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*

class WebActivity : ThemedActivity() {

    private var jsLogs = ArrayList<ConsoleMessage>()

    private lateinit var localUrl: String
    private lateinit var localWithoutIndex: String


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        val project = intent.getStringExtra("name")
        NetworkUtils.server = HyperServer(project)
        super.onCreate(savedInstanceState)

        try {
            NetworkUtils.server!!.start()
        } catch (e: IOException) {
            Timber.e(e)
        }

        setContentView(R.layout.activity_web)
        val indexFile = ProjectManager.getIndexFile(project)
        var indexPath = indexFile!!.path
        indexPath = indexPath.replace(File(Constants.HYPER_ROOT + File.separator + project).path, "")

        toolbar.title = project
        setSupportActionBar(toolbar)
        webView.settings.javaScriptEnabled = true
        localUrl = if (NetworkUtils.server!!.wasStarted() && NetworkUtils.server!!.isAlive && NetworkUtils.ipAddress != null)
            "http://" + NetworkUtils.ipAddress + ":8080" + indexPath
        else
            intent.getStringExtra("localUrl")

        localWithoutIndex = localUrl.substring(0, localUrl.length - 10)
        webView.loadUrl(localUrl)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                loadingProgress.progress = newProgress
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                jsLogs.add(consoleMessage)
                return true
            }

            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                AlertDialog.Builder(this@WebActivity)
                        .setTitle("Alert")
                        .setMessage(message)
                        .setPositiveButton("OK") { _, _ -> result.confirm() }
                        .setCancelable(false)
                        .show()

                return true
            }

            override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
                AlertDialog.Builder(this@WebActivity)
                        .setTitle("Confirm")
                        .setMessage(message)
                        .setPositiveButton("OK") { _, _ -> result.confirm() }
                        .setNegativeButton("CANCEL") { _, _ -> result.cancel() }
                        .setCancelable(false)
                        .show()

                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, localUrl: String) {
                super.onPageFinished(view, localUrl)
                webView.animate().alpha(1F)
            }
        }

        toolbar.subtitle = localUrl
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkUtils.server!!.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_web, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val prefs = defaultPrefs(this)
        when (item.itemId) {
            R.id.refresh -> {
                webView.animate().alpha(0F)
                webView.reload()
                return true
            }
            R.id.user_agent -> {
                val selectedI = IntArray(1)
                val current = webView.settings.userAgentString
                val agents = LinkedList(Arrays.asList(*Constants.USER_AGENTS))
                if (!agents.contains(current)) agents.add(0, current)
                val parsedAgents = NetworkUtils.parseUAList(agents)
                AlertDialog.Builder(this)
                        .setTitle("Change User Agent")
                        .setSingleChoiceItems(parsedAgents.toTypedArray(), parsedAgents.indexOf(NetworkUtils.parseUA(current))) { _, i -> selectedI[0] = i }
                        .setPositiveButton("UPDATE") { _, _ -> webView.settings.userAgentString = agents[selectedI[0]] }
                        .setNeutralButton("RESET") { _, _ -> webView.settings.userAgentString = null }
                        .setNegativeButton("CUSTOM") { _, _ ->
                            val rootView = View.inflate(this@WebActivity, R.layout.dialog_input_single, null)
                            rootView.inputText.hint = "Custom agent string"
                            rootView.inputText.setText(current)
                            AlertDialog.Builder(this@WebActivity)
                                    .setTitle("Custom User Agent")
                                    .setView(rootView)
                                    .setPositiveButton("UPDATE") { _, _ -> webView.settings.userAgentString = rootView.inputText.text.toString() }
                                    .setNegativeButton(R.string.cancel, null)
                                    .show()
                        }
                        .show()
                return true
            }
            R.id.web_browser -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(localUrl))
                startActivity(intent)
                return true
            }
            R.id.web_logs -> {
                val layoutLog = View.inflate(this, R.layout.sheet_logs, null)
                val darkTheme = prefs["dark_theme", false]!!
                if (darkTheme) {
                    layoutLog.setBackgroundColor(-0xcccccd)
                }

                val manager = LinearLayoutManager(this)
                val adapter = LogsAdapter(localWithoutIndex, jsLogs, darkTheme)

                layoutLog.logsList.layoutManager = manager
                layoutLog.logsList.addItemDecoration(DividerItemDecoration(this@WebActivity, manager.orientation))
                layoutLog.logsList.adapter = adapter

                val dialogLog = BottomSheetDialog(this)
                dialogLog.setContentView(layoutLog)
                dialogLog.show()
                return true
            }
            R.id.web_settings -> {
                val layout = View.inflate(this, R.layout.sheet_web_settings, null)
                if (prefs["dark_theme", false]!!) {
                    layout.setBackgroundColor(-0xcccccd)
                }

                layout.allowContentAccess.isChecked = webView.settings.allowContentAccess
                layout.allowFileAccess.isChecked = webView.settings.allowFileAccess
                layout.blockNetworkImage.isChecked = webView.settings.blockNetworkImage
                layout.blockNetworkLoads.isChecked = webView.settings.blockNetworkLoads
                layout.builtInZoomControls.isChecked = webView.settings.builtInZoomControls
                layout.database.isChecked = webView.settings.databaseEnabled
                layout.displayZoomControls.isChecked = webView.settings.displayZoomControls
                layout.domStorage.isChecked = webView.settings.domStorageEnabled
                layout.jsCanOpenWindows.isChecked = webView.settings.javaScriptCanOpenWindowsAutomatically
                layout.jsEnabled.isChecked = webView.settings.javaScriptEnabled
                layout.loadOverview.isChecked = webView.settings.loadWithOverviewMode
                layout.imageLoad.isChecked = webView.settings.loadsImagesAutomatically
                layout.wideView.isChecked = webView.settings.useWideViewPort

                layout.allowContentAccess.setOnCheckedChangeListener { _, isChecked -> webView.settings.allowContentAccess = isChecked }
                layout.allowFileAccess.setOnCheckedChangeListener { _, isChecked -> webView.settings.allowFileAccess = isChecked }
                layout.blockNetworkImage.setOnCheckedChangeListener { _, isChecked -> webView.settings.blockNetworkImage = isChecked }
                layout.blockNetworkLoads.setOnCheckedChangeListener { _, isChecked -> webView.settings.blockNetworkLoads = isChecked }
                layout.builtInZoomControls.setOnCheckedChangeListener { _, isChecked -> webView.settings.builtInZoomControls = isChecked }
                layout.database.setOnCheckedChangeListener { _, isChecked -> webView.settings.databaseEnabled = isChecked }
                layout.displayZoomControls.setOnCheckedChangeListener { _, isChecked -> webView.settings.displayZoomControls = isChecked }
                layout.domStorage.setOnCheckedChangeListener { _, isChecked -> webView.settings.domStorageEnabled = isChecked }
                layout.jsCanOpenWindows.setOnCheckedChangeListener { _, isChecked -> webView.settings.javaScriptCanOpenWindowsAutomatically = isChecked }
                layout.jsEnabled.setOnCheckedChangeListener { _, isChecked -> webView.settings.javaScriptEnabled = isChecked }
                layout.loadOverview.setOnCheckedChangeListener { _, isChecked -> webView.settings.loadWithOverviewMode = isChecked }
                layout.imageLoad.setOnCheckedChangeListener { _, isChecked -> webView.settings.loadsImagesAutomatically = isChecked }
                layout.wideView.setOnCheckedChangeListener { _, isChecked -> webView.settings.useWideViewPort = isChecked }


                if (Build.VERSION.SDK_INT >= 16) {
                    layout.allowFileAccessFromFileUrls.isChecked = webView.settings.allowFileAccessFromFileURLs
                    layout.allowUniversalAccessFromFileUrls.isChecked = webView.settings.allowUniversalAccessFromFileURLs
                    layout.allowFileAccessFromFileUrls.setOnCheckedChangeListener { _, isChecked -> webView.settings.allowFileAccessFromFileURLs = isChecked }
                    layout.allowUniversalAccessFromFileUrls.setOnCheckedChangeListener { _, isChecked -> webView.settings.allowUniversalAccessFromFileURLs = isChecked }
                } else {
                    layout.allowFileAccessFromFileUrls.visibility = View.GONE
                    layout.allowUniversalAccessFromFileUrls.visibility = View.GONE
                }

                val dialog = BottomSheetDialog(this)
                dialog.setContentView(layout)
                dialog.show()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
