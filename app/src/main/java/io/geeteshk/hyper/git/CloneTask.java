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

package io.geeteshk.hyper.git;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

import io.geeteshk.hyper.adapter.ProjectAdapter;
import io.geeteshk.hyper.helper.ProjectManager;

public class CloneTask extends GitTask {

    private static final String TAG = CloneTask.class.getSimpleName();
    private ProjectAdapter mAdapter;

    public CloneTask(Context context, View view, File repo, ProjectAdapter adapter) {
        super(context, view, repo, new String[]{"Cloning repository", "", ""});
        mAdapter = adapter;
        id = 3;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        try {
            Git.cloneRepository()
                    .setURI(strings[0])
                    .setDirectory(mRepo)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(strings[1], strings[2]))
                    .setProgressMonitor(new BatchingProgressMonitor() {
                        @Override
                        protected void onUpdate(String taskName, int workCurr) {

                        }

                        @Override
                        protected void onEndTask(String taskName, int workCurr) {

                        }

                        @Override
                        protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
                            publishProgress(taskName, String.valueOf(percentDone), String.valueOf(workCurr), String.valueOf(workTotal));
                        }

                        @Override
                        protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
                            publishProgress(taskName, String.valueOf(workCurr), String.valueOf(workTotal));
                        }
                    })
                    .call();
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(mView, e.toString(), Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            if (!ProjectManager.isValid(mRepo.getName())) {
                mBuilder.setContentText("The repo was successfully cloned but it doesn't seem to be a Hyper project.");
            } else {
                mAdapter.insert(mRepo.getPath().substring(mRepo.getPath().lastIndexOf("/") + 1, mRepo.getPath().length()));
                mBuilder.setContentText("Successfully cloned.");
            }
        } else {
            mBuilder.setContentText("Unable to clone repo.");
        }

        mBuilder.setProgress(0, 0, false);
        mManager.notify(id, mBuilder.build());
    }
}
