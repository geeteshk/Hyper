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

public class PushTask extends GitTask {

    private static final String TAG = PushTask.class.getSimpleName();
    private boolean[] gitOptions;

    PushTask(Context context, View view, File repo, String[] values, boolean[] options) {
        super(context, view, repo, values);
        gitOptions = options;
        id = 6;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Git git = GitWrapper.getGit(rootView, repo);
        if (git != null) {
            try {
                if (gitOptions[3]) {
                    git.push()
                            .setRemote(params[0])
                            .setDryRun(gitOptions[0])
                            .setForce(gitOptions[1])
                            .setThin(gitOptions[2])
                            .setPushTags()
                            .setCredentialsProvider(new UsernamePasswordCredentialsProvider(params[1], params[2]))
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
                } else {
                    git.push()
                            .setRemote(params[0])
                            .setDryRun(gitOptions[0])
                            .setForce(gitOptions[1])
                            .setThin(gitOptions[2])
                            .setCredentialsProvider(new UsernamePasswordCredentialsProvider(params[1], params[2]))
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
                }
            } catch (GitAPIException e) {
                Log.e(TAG, e.toString());
                Snackbar.make(rootView, e.toString(), Snackbar.LENGTH_LONG).show();
                return false;
            }

            return true;
        }

        return false;
    }
}
