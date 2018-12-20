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
import io.geeteshk.hyper.util.snack
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import timber.log.Timber
import java.io.File
import java.lang.ref.WeakReference

class PullTask internal constructor(context: WeakReference<Context>, view: WeakReference<View>, repo: File, values: Array<String>) : GitTask(context, view, repo, values) {

    init {
        id = 5
    }

    override fun doInBackground(vararg params: String): Boolean? {
        val git = GitWrapper.getGit(rootView.get()!!, repo)
        git?.let {
            try {
                git.pull()
                        .setRemote(params[0])
                        .setCredentialsProvider(UsernamePasswordCredentialsProvider(params[1], params[2]))
                        .setProgressMonitor(progressMonitor)
                        .call()
            } catch (e: GitAPIException) {
                Timber.e(e)
                rootView.get()?.snack(e.toString())
                return false
            }

            return true
        }

        return false
    }
}
