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
import android.view.View
import io.geeteshk.hyper.ui.adapter.ProjectAdapter
import io.geeteshk.hyper.util.project.ProjectManager
import io.geeteshk.hyper.util.snack
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import timber.log.Timber
import java.io.File
import java.lang.ref.WeakReference

class CloneTask internal constructor(context: WeakReference<Context>, view: WeakReference<View>, repo: File, private val projectAdapter: ProjectAdapter) : GitTask(context, view, repo, arrayOf("Cloning repository", "", "")) {

    init {
        id = 3
    }

    override fun doInBackground(vararg strings: String): Boolean? {
        try {
            Git.cloneRepository()
                    .setURI(strings[0])
                    .setDirectory(repo)
                    .setCredentialsProvider(UsernamePasswordCredentialsProvider(strings[1], strings[2]))
                    .setProgressMonitor(progressMonitor)
                    .call()
        } catch (e: GitAPIException) {
            Timber.e(e)
            rootView.get()?.snack(e.toString())
            return false
        }

        return true
    }

    override fun onPostExecute(aBoolean: Boolean?) {
        if (aBoolean!!) {
            if (!ProjectManager.isValid(repo.name)) {
                builder.setContentText("The repo was successfully cloned but it doesn't seem to be a Hyper project.")
            } else {
                projectAdapter.insert(repo.path.substring(repo.path.lastIndexOf("/") + 1, repo.path.length))
                builder.setContentText("Successfully cloned.")
            }
        } else {
            builder.setContentText("Unable to clone repo.")
        }

        builder.setProgress(0, 0, false)
        manager.notify(id, builder.build())
    }
}
