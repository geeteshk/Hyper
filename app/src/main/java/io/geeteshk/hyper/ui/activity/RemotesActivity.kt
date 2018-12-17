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

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.adapter.RemotesAdapter
import io.geeteshk.hyper.git.GitWrapper
import io.geeteshk.hyper.util.ui.Styles
import kotlinx.android.synthetic.main.activity_remotes.*
import kotlinx.android.synthetic.main.dialog_remote_add.view.*
import kotlinx.android.synthetic.main.widget_toolbar.*
import java.io.File

class RemotesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Styles.getThemeInt(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remotes)
        setSupportActionBar(toolbar)

        val repo = File(intent.getStringExtra("project_file"))
        val remotesAdapter = RemotesAdapter(this, remotesLayout, repo)
        val layoutManager = LinearLayoutManager(this)

        val dividerItemDecoration = DividerItemDecoration(remotesList.context,
                layoutManager.orientation)
        remotesList.addItemDecoration(dividerItemDecoration)
        remotesList.layoutManager = layoutManager
        remotesList.adapter = remotesAdapter

        newRemote.setOnClickListener {
            val cloneView = View.inflate(this@RemotesActivity, R.layout.dialog_remote_add, null)

            AlertDialog.Builder(this@RemotesActivity)
                    .setTitle("Add remote")
                    .setView(cloneView)
                    .setPositiveButton(R.string.git_add) { _, _ ->
                        GitWrapper.addRemote(remotesLayout, repo, cloneView.remoteAddName.text.toString(), cloneView.remoteAddUrl.text.toString())
                        remotesAdapter.add(cloneView.remoteAddName.text.toString(), cloneView.remoteAddUrl.text.toString())
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
        }

        remotesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    newRemote.show()
                }

                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && newRemote.isShown) newRemote.hide()
            }
        })
    }
}
