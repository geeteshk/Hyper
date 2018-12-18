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

package io.geeteshk.hyper.git

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.BatchingProgressMonitor
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import timber.log.Timber
import java.io.File

class FetchTask internal constructor(context: Context, view: View, repo: File, values: Array<String>) : GitTask(context, view, repo, values) {

    init {
        id = 5
    }

    override fun doInBackground(vararg params: String): Boolean? {
        val git = GitWrapper.getGit(rootView, repo)
        git?.let {
            try {
                git.fetch()
                        .setCheckFetchedObjects(true)
                        .setRemote(params[0])
                        .setCredentialsProvider(UsernamePasswordCredentialsProvider(params[1], params[2]))
                        .setProgressMonitor(object : BatchingProgressMonitor() {
                            override fun onUpdate(taskName: String, workCurr: Int) {

                            }

                            override fun onEndTask(taskName: String, workCurr: Int) {

                            }

                            override fun onUpdate(taskName: String, workCurr: Int, workTotal: Int, percentDone: Int) {
                                publishProgress(taskName, percentDone.toString(), workCurr.toString(), workTotal.toString())
                            }

                            override fun onEndTask(taskName: String, workCurr: Int, workTotal: Int, percentDone: Int) {
                                publishProgress(taskName, workCurr.toString(), workTotal.toString())
                            }
                        })
                        .call()
            } catch (e: GitAPIException) {
                Timber.e(e)
                Snackbar.make(rootView, e.toString(), Snackbar.LENGTH_LONG).show()
                return false
            }

            return true
        }

        return false
    }
}